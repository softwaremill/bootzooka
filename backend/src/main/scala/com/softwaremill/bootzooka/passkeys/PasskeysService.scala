package com.softwaremill.bootzooka.passkeys

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
import com.webauthn4j.data.attestation.authenticator.AttestedCredentialData
import com.webauthn4j.data.attestation.authenticator.AAGUID
import com.webauthn4j.data.attestation.authenticator.COSEKey
import cats.implicits._
import com.softwaremill.bootzooka.infrastructure.Doobie._
import com.softwaremill.bootzooka.logging.FLogging
import com.softwaremill.bootzooka.util._
import com.softwaremill.tagging.@@
import com.softwaremill.bootzooka.user.User
import com.webauthn4j.WebAuthnManager

class PasskeysService(
    passkeyModel: PasskeysModel, 
    webAuthnManager: WebAuthnManager,
    objectConverter: ObjectConverter,
    config: PasskeysConfig) extends FLogging {
  
      def registerPasskey(id: Id @@ User, origin: String, attestationObject: Array[Int], clientDataJSON: Array[Int],
                      clientExtensionJSON: String, transports: List[String]): ConnectionIO[Unit]  = {
    println(s"Got new registerpasskey ${id}; ${attestationObject}; ${clientDataJSON}; ${clientExtensionJSON}; " +
      s"${transports}")

      for {
        authenticator <- validateNewPasskey(origin, attestationObject.map(_.toByte), clientDataJSON.map(_.toByte), 
    clientExtensionJSON, transports)
        // _ <- authenticator.
        _ <- logger.info[ConnectionIO](s"Got authenticator: ${authenticator}")
        _ <- serializeAuthenticator(id, authenticator)

      } yield()
  }

  private def validateNewPasskey(originHeader: String, attestationObject: Array[Byte], clientDataJSON: Array[Byte],
                                clientExtensionJSON: String, transports: List[String]): ConnectionIO[Authenticator] = {

    // Server properties
    // ogarnąć róne porty
    // val origin: Origin = Origin.create(originHeader)
    val origin = Origin.create(config.origin)
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
        registrationData = webAuthnManager.validate(registrationData, registrationParameters);
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

  def serializeAuthenticator(id: Id @@ User, authenticator: Authenticator): ConnectionIO[Passkey] = {
    // serialize
    val attestedCredentialData = authenticator.getAttestedCredentialData()

    val serializedAaguid = attestedCredentialData.getAaguid().toString()
    val serializedCoseKey = objectConverter.getJsonConverter().writeValueAsString(attestedCredentialData.getCOSEKey())
    val serializedCredentialId = attestedCredentialData.getCredentialId()

    val serializedCounter = authenticator.getCounter()

    println(s"""
      serializedAaguid = ${serializedAaguid}
      serializedCoseKey = ${serializedCoseKey}
      serializedCredentialId = ${serializedCredentialId}
      serializedCounter = ${serializedCounter}
    """)

    val passkey = Passkey(id, serializedAaguid, serializedCoseKey, serializedCredentialId, serializedCounter)

    for {
        _ <- passkeyModel.insert(passkey)
    } yield (passkey)
  }

  def deserializeAuthenticator(passkey: Passkey): ConnectionIO[Authenticator] = {
    val deserializedAaaguid = new AAGUID(passkey.aaguid)
    val deserializedCoseKey = objectConverter.getJsonConverter().readValue(passkey.coseKey, classOf[COSEKey])
    val deserializedCredentialId = passkey.credentialId

    val deserializedAttestedCredentialData = new AttestedCredentialData(deserializedAaaguid, deserializedCredentialId, deserializedCoseKey)

    val deserializedAuthenticator = new AuthenticatorImpl(deserializedAttestedCredentialData, null, passkey.counter)

    println(s"""
      deserializedAaaguid = ${deserializedAaaguid}
      deserializedCoseKey = ${deserializedCoseKey}
      deserializedCredentialId = ${deserializedCredentialId}
      deserializedAttestedCredentialData = ${deserializedAttestedCredentialData}
      deserializedAuthenticator = ${deserializedAuthenticator}
    """)

    deserializedAuthenticator.asInstanceOf[Authenticator].pure[ConnectionIO]
  }

  // def validatePasskey() = {
  //   webAuthnManager.getAuthenticationDataValidator().
  // }
}
