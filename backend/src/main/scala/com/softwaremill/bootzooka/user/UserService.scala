package com.softwaremill.bootzooka.user

import cats.MonadError
import cats.implicits._
import com.softwaremill.bootzooka._
import com.softwaremill.bootzooka.email.{EmailData, EmailScheduler, EmailTemplates}
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.logging.FLogging
import com.softwaremill.bootzooka.security.{ApiKey, ApiKeyService}
import com.softwaremill.bootzooka.util._
import com.softwaremill.tagging.@@
import com.webauthn4j.WebAuthnManager

import scala.concurrent.duration.Duration
import com.webauthn4j.data.client.Origin
import com.webauthn4j.server.ServerProperty
import com.webauthn4j.data.RegistrationData
import com.webauthn4j.data.RegistrationParameters
import com.webauthn4j.data.RegistrationRequest
import com.webauthn4j.validator.exception.ValidationException
import com.webauthn4j.converter.exception.DataConversionException
import com.webauthn4j.authenticator.AuthenticatorImpl
import com.webauthn4j.authenticator.Authenticator
import com.webauthn4j.data.client.challenge.Challenge
import com.webauthn4j.converter.util.ObjectConverter
import com.webauthn4j.converter.AttestedCredentialDataConverter
import com.fasterxml.jackson.annotation.JsonProperty
import com.webauthn4j.data.attestation.statement.AttestationStatement
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonCreator
import com.webauthn4j.data.AuthenticatorTransport
import com.webauthn4j.data.extension.authenticator.AuthenticationExtensionsAuthenticatorOutputs
import com.webauthn4j.data.extension.authenticator.RegistrationExtensionAuthenticatorOutput
import com.webauthn4j.data.extension.client.AuthenticationExtensionsClientOutputs
import com.webauthn4j.data.extension.client.RegistrationExtensionClientOutput

class UserService(
    userModel: UserModel,
    webAuthnManager: WebAuthnManager,
    emailScheduler: EmailScheduler,
    emailTemplates: EmailTemplates,
    apiKeyService: ApiKeyService,
    idGenerator: IdGenerator,
    clock: Clock,
    config: UserConfig
) extends FLogging {


  private val LoginAlreadyUsed = "Login already in use!"
  private val EmailAlreadyUsed = "E-mail already in use!"
  private val IncorrectLoginOrPassword = "Incorrect login/email or password"

  def registerNewUser(login: String, email: String, password: String): ConnectionIO[ApiKey] = {
    val loginClean = login.trim()
    val emailClean = email.trim()

    def failIfDefined(op: ConnectionIO[Option[User]], msg: String): ConnectionIO[Unit] = {
      op.flatMap {
        case None    => ().pure[ConnectionIO]
        case Some(_) => Fail.IncorrectInput(msg).raiseError[ConnectionIO, Unit]
      }
    }

    def checkUserDoesNotExist(): ConnectionIO[Unit] = {
      failIfDefined(userModel.findByLogin(loginClean.lowerCased), LoginAlreadyUsed) >>
        failIfDefined(userModel.findByEmail(emailClean.lowerCased), EmailAlreadyUsed)
    }

    def doRegister(): ConnectionIO[ApiKey] = for {
      id <- idGenerator.nextId[ConnectionIO, User]()
      now <- clock.now[ConnectionIO]()
      user = User(id, loginClean, loginClean.lowerCased, emailClean.lowerCased, User.hashPassword(password), now)
      confirmationEmail = emailTemplates.registrationConfirmation(loginClean)
      _ <- logger.debug[ConnectionIO](s"Registering new user: ${user.emailLowerCased}, with id: ${user.id}")
      _ <- userModel.insert(user)
      _ <- emailScheduler(EmailData(emailClean, confirmationEmail))
      apiKey <- apiKeyService.create(user.id, config.defaultApiKeyValid)
    } yield apiKey

    for {
      _ <- UserValidator(Some(loginClean), Some(emailClean), Some(password)).as[ConnectionIO]
      _ <- checkUserDoesNotExist()
      apiKey <- doRegister()
    } yield apiKey
  }

  def findById(id: Id @@ User): ConnectionIO[User] = userOrNotFound(userModel.findById(id))

  def login(loginOrEmail: String, password: String, apiKeyValid: Option[Duration]): ConnectionIO[ApiKey] = {
    val loginOrEmailClean = loginOrEmail.trim()
    for {
      user <- userOrNotFound(userModel.findByLoginOrEmail(loginOrEmailClean.lowerCased))
      _ <- verifyPassword(user, password, validationErrorMsg = IncorrectLoginOrPassword)
      apiKey <- apiKeyService.create(user.id, apiKeyValid.getOrElse(config.defaultApiKeyValid))
    } yield apiKey
  }

  def changeUser(userId: Id @@ User, newLogin: String, newEmail: String): ConnectionIO[Unit] = {
    val newLoginClean = newLogin.trim()
    val newEmailClean = newEmail.trim()
    val newEmailLowerCased = newEmailClean.lowerCased

    def changeLogin(): ConnectionIO[Boolean] = {
      val newLoginLowerCased = newLoginClean.lowerCased
      userModel.findByLogin(newLoginLowerCased).flatMap {
        case Some(user) if user.id != userId           => Fail.IncorrectInput(LoginAlreadyUsed).raiseError[ConnectionIO, Boolean]
        case Some(user) if user.login == newLoginClean => false.pure[ConnectionIO]
        case _ =>
          for {
            _ <- validateLogin()
            _ <- logger.debug[ConnectionIO](s"Changing login for user: $userId, to: $newLoginClean")
            _ <- userModel.updateLogin(userId, newLoginClean, newLoginLowerCased)
          } yield true
      }
    }

    def validateLogin() =
      UserValidator(Some(newLoginClean), None, None).as[ConnectionIO]

    def changeEmail(): ConnectionIO[Boolean] = {
      userModel.findByEmail(newEmailLowerCased).flatMap {
        case Some(user) if user.id != userId => Fail.IncorrectInput(EmailAlreadyUsed).raiseError[ConnectionIO, Boolean]
        case Some(user) if user.emailLowerCased == newEmailLowerCased => false.pure[ConnectionIO]
        case _ =>
          for {
            _ <- validateEmail()
            _ <- logger.debug[ConnectionIO](s"Changing email for user: $userId, to: $newEmailClean")
            _ <- userModel.updateEmail(userId, newEmailLowerCased)
          } yield true
      }
    }

    def validateEmail() =
      UserValidator(None, Some(newEmailLowerCased), None).as[ConnectionIO]

    def doChange(): ConnectionIO[Boolean] = {
      for {
        loginUpdated <- changeLogin()
        emailUpdated <- changeEmail()
      } yield loginUpdated || emailUpdated
    }

    def sendMail(user: User): ConnectionIO[Unit] = {
      val confirmationEmail = emailTemplates.profileDetailsChangeNotification(user.login)
      emailScheduler(EmailData(user.emailLowerCased, confirmationEmail))
    }

    doChange().flatMap { anyUpdate =>
      if (anyUpdate) {
        findById(userId).flatMap(user => sendMail(user))
      } else {
        ().pure[ConnectionIO]
      }
    }
  }

  def changePassword(userId: Id @@ User, currentPassword: String, newPassword: String): ConnectionIO[Unit] = {
    def validateNewPassword() =
      UserValidator(None, None, Some(newPassword)).as[ConnectionIO]

    for {
      user <- userOrNotFound(userModel.findById(userId))
      _ <- verifyPassword(user, currentPassword, validationErrorMsg = "Incorrect current password")
      _ <- validateNewPassword()
      _ <- logger.debug[ConnectionIO](s"Changing password for user: $userId")
      _ <- userModel.updatePassword(userId, User.hashPassword(newPassword))
      confirmationEmail = emailTemplates.passwordChangeNotification(user.login)
      _ <- emailScheduler(EmailData(user.emailLowerCased, confirmationEmail))
    } yield ()
  }

  private def userOrNotFound(op: ConnectionIO[Option[User]]): ConnectionIO[User] = {
    op.flatMap {
      case Some(user) => user.pure[ConnectionIO]
      case None       => Fail.Unauthorized(IncorrectLoginOrPassword).raiseError[ConnectionIO, User]
    }
  }

  private def verifyPassword(user: User, password: String, validationErrorMsg: String): ConnectionIO[Unit] = {
    if (user.verifyPassword(password) == Verified) {
      ().pure[ConnectionIO]
    } else {
      Fail.Unauthorized(validationErrorMsg).raiseError[ConnectionIO, Unit]
    }
  }

  def registerPasskey(id: Id @@ User, origin: String, attestationObject: Array[Int], clientDataJSON: Array[Int],
                      clientExtensionJSON: String, transports: List[String]): ConnectionIO[Unit]  = {
    println(s"Got new registerpasskey ${id}; ${attestationObject}; ${clientDataJSON}; ${clientExtensionJSON}; " +
      s"${transports}")

      for {
        authenticator <- validateNewPasskey(origin, attestationObject.map(_.toByte), clientDataJSON.map(_.toByte), 
    clientExtensionJSON, transports)
        // _ <- authenticator.
        _ <- logger.info[ConnectionIO](s"Got authenticator: ${authenticator}")
        _ <- serializeAuthenticator(authenticator)

      } yield()
  }

  private def validateNewPasskey(originHeader: String, attestationObject: Array[Byte], clientDataJSON: Array[Byte],
                                clientExtensionJSON: String, transports: List[String]): ConnectionIO[Authenticator] = {

    // Server properties
    // ogarnąć róne porty
    // val origin: Origin = Origin.create(originHeader)
    val origin = Origin.create("https://bootzooka.internal:3000")
    val rpId: String = "bootzooka.internal" /* set rpId */;
    val challenge: Challenge = new Challenge() {
      def getValue(): Array[Byte] = Array(117, 61, 252, 231, 191, 49).map(_.toByte)
    }
    val tokenBindingId = null /* set tokenBindingId */;
    val serverProperty: ServerProperty = new ServerProperty(origin, rpId, challenge, tokenBindingId);

    // expectations
    val userVerificationRequired = false;
    val userPresenceRequired = true;

    import scala.jdk.CollectionConverters._

    val registrationRequest = new RegistrationRequest(attestationObject, clientDataJSON, clientExtensionJSON, 
      transports.toSet.asJava);
    val registrationParameters = new RegistrationParameters(serverProperty, userVerificationRequired, userPresenceRequired);
    var registrationData : RegistrationData = null;
    try {
        registrationData = webAuthnManager.parse(registrationRequest);
    } catch{ 
      case e: DataConversionException => throw e
    } 

    try {
        webAuthnManager.validate(registrationData, registrationParameters);
    } catch {
      case e: ValidationException =>
        // If you would like to handle WebAuthn data validation error, please catch ValidationException
        throw e;
    }

    // please persist Authenticator object, which will be used in the authentication process.
    return (new AuthenticatorImpl( // You may create your own Authenticator implementation to save friendly authenticator name
                    registrationData.getAttestationObject().getAuthenticatorData().getAttestedCredentialData(),
                    registrationData.getAttestationObject().getAttestationStatement(),
                    registrationData.getAttestationObject().getAuthenticatorData().getSignCount()
            ).asInstanceOf[Authenticator]).pure[ConnectionIO];
  }

  def serializeAuthenticator(authenticator: Authenticator): ConnectionIO[Unit] = {
    val objectConverter = new ObjectConverter()
    val attestedCredentialDataConverter = new AttestedCredentialDataConverter(objectConverter);

    // serialize
    val attestedCredentialDataSerialized = attestedCredentialDataConverter.convert(authenticator.getAttestedCredentialData())

    val envelope = new AttestationStatementEnvelope(authenticator.getAttestationStatement());
    val serializedAttestationStatement = objectConverter.getCborConverter().writeValueAsBytes(envelope);

    val serializedTransports = objectConverter.getJsonConverter().writeValueAsString(authenticator.getTransports());

    val serializedAuthenticatorExtensions = objectConverter.getCborConverter().writeValueAsBytes(authenticator.getAuthenticatorExtensions());

    val serializedClientExtensions = objectConverter.getJsonConverter().writeValueAsString(authenticator.getClientExtensions());
    
    println(s"""
      attestedCredentialDataSerialized = ${attestedCredentialDataSerialized}
      serializedAttestationStatement = ${serializedAttestationStatement}
      serializedTransports = ${serializedTransports}
      serializedAuthenticatorExtensions = ${serializedAuthenticatorExtensions}
      serializedClientExtensions = ${serializedClientExtensions}
    """)

    val deserializedAttestedCredential = attestedCredentialDataConverter.convert(attestedCredentialDataSerialized);

    val deserializedEnvelope = objectConverter.getCborConverter().readValue(
      serializedAttestationStatement, classOf[AttestationStatementEnvelope]);

    val deserializedAttestationStatement = deserializedEnvelope.getAttestationStatement();
    val deserializedAuthTransport = objectConverter.getJsonConverter().readValue(serializedTransports, classOf[Set[AuthenticatorTransport]])
    val deseiralizedAuthExtensions = objectConverter.getCborConverter().readValue(serializedAuthenticatorExtensions, classOf[AuthenticationExtensionsAuthenticatorOutputs[RegistrationExtensionAuthenticatorOutput]])

    val deserializedClientExt = objectConverter.getJsonConverter().readValue(serializedClientExtensions, classOf[AuthenticationExtensionsClientOutputs[RegistrationExtensionClientOutput]])

    println(s"""
      deserializedAttestedCredential = ${deserializedAttestedCredential}
      deserializedAttestationStatement = ${deserializedAttestationStatement}
      deserializedAuthTransport = ${deserializedAuthTransport}
      deseiralizedAuthExtensions = ${deseiralizedAuthExtensions}
      deserializedClientExt = ${deserializedClientExt}
    """)
    ().pure[ConnectionIO]
  }

  // def validatePasskey() = {
  //   webAuthnManager.getAuthenticationDataValidator().
  // }
}

@JsonCreator
class AttestationStatementEnvelope(
   @JsonProperty("attStmt")
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
            property = "fmt"
    )
  val attestationStatement: AttestationStatement){

    @JsonProperty("fmt")
    def getFormat(): String = {
        return attestationStatement.getFormat();
    }

    def getAttestationStatement(): AttestationStatement = {
        return attestationStatement;
    }
}

object UserValidator {
  val MinLoginLength = 3
}

case class UserValidator(loginOpt: Option[String], emailOpt: Option[String], passwordOpt: Option[String]) {
  private val ValidationOk = Right(())

  private val emailRegex =
    """^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

  val result: Either[String, Unit] = {
    for {
      _ <- validateLogin(loginOpt)
      _ <- validateEmail(emailOpt)
      _ <- validatePassword(passwordOpt)
    } yield ()
  }

  def as[F[_]](implicit me: MonadError[F, Throwable]): F[Unit] =
    result.fold(msg => Fail.IncorrectInput(msg).raiseError[F, Unit], _ => ().pure[F])

  private def validateLogin(loginOpt: Option[String]): Either[String, Unit] =
    loginOpt.map(_.trim) match {
      case Some(login) =>
        if (login.length >= UserValidator.MinLoginLength) ValidationOk else Left("Login is too short!")
      case None => ValidationOk
    }

  private def validateEmail(emailOpt: Option[String]): Either[String, Unit] =
    emailOpt.map(_.trim) match {
      case Some(email) =>
        if (emailRegex.findFirstMatchIn(email).isDefined) ValidationOk else Left("Invalid e-mail format!")
      case None => ValidationOk
    }

  private def validatePassword(passwordOpt: Option[String]): Either[String, Unit] =
    passwordOpt.map(_.trim) match {
      case Some(password) =>
        if (password.nonEmpty) ValidationOk else Left("Password cannot be empty!")
      case None => ValidationOk
    }
}
