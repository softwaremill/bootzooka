package com.softwaremill.bootzooka.user.worker

import java.time.{Instant, ZoneOffset}

import akka.actor.{Actor, Props}
import com.softwaremill.bootzooka.email.{EmailService, EmailTemplatingEngine}
import com.softwaremill.bootzooka.user.{BasicUserData, User, UserDao}
import com.softwaremill.bootzooka.utils.{ActorPerRequest, ActorPerRequestFactory, Utils}
import com.softwaremill.bootzooka.utils.http.PerRequest._

import scala.concurrent.Future

class UserRegistrator(userDao: UserDao, emailService: EmailService,
    emailTemplatingEngine: EmailTemplatingEngine) extends ActorPerRequestFactory {

  override def props = Props(new UserRegisterActor)

  class UserRegisterActor extends Actor with ActorPerRequest {

    import UserRegistrator._
    import context.dispatcher

    def receive = {
      case RegisterUser(login, email, password) =>
        val userRegisterFuture = UserRegisterValidator.validate(login, email, password).fold(
          msg => Future.successful(UserInvalidData(msg)),
          _ => registerValidData(login, email, password)
        )
        pipeToSender(sender(), userRegisterFuture)

      case AuthenticateUser(login, password) =>
        val f = userDao findByLoginOrEmail login map { userOpt =>
          userOpt filter (u => User.passwordsMatch(password, u)) map BasicUserData.fromUser
        } map {
          case Some(user) => UserAuthenticated(user)
          case None => UserRejected
        }
        pipeToSender(sender(), f)
    }

    def authenticate(login: String, nonEncryptedPassword: String): Future[Option[BasicUserData]] = {
      userDao.findByLoginOrEmail(login).map(userOpt =>
        userOpt.filter(u => User.passwordsMatch(nonEncryptedPassword, u)).map(BasicUserData.fromUser))
    }

    def checkUserExistence(login: String, email: String): Future[Either[Event, Unit]] = {
      val existingLoginFuture = userDao findByLowerCasedLogin login
      val existingEmailFuture = userDao findByEmail email

      for {
        existingLogin <- existingLoginFuture map (_.toLeft(()).left.map(_ => LoginIsTaken))
        existingEmail <- existingEmailFuture map (_.toLeft(()).left.map(_ => EmailIsTaken))
      } yield existingLogin.right.flatMap(_ => existingEmail)
    }

    def registerValidData(login: String, email: String, password: String) =
      checkUserExistence(login, email) flatMap {
        case Left(event) => Future.successful(event)
        case Right(_) =>
          val salt = Utils randomString 128
          val now = Instant.now().atOffset(ZoneOffset.UTC)
          userDao add User.withRandomUUID(login, email.toLowerCase, password, salt, now) map {
            case user =>
              val confirmationEmail = emailTemplatingEngine.registrationConfirmation(login)
              emailService.scheduleEmail(email, confirmationEmail)
              user
          } map (_ => UserRegistered)
      }
  }

}

object UserRegistrator {

  case object UserRegistered extends JustOK
  case object LoginIsTaken extends Conflict("Login already in use!")
  case object EmailIsTaken extends Conflict("E-mail already in use!")
  case class UserInvalidData(error: String) extends InvalidData(error)

  case class RegisterUser(login: String, email: String, password: String) extends Command {
    def withEscapedLogin = copy(login = Utils escapeHtml login)
  }

  case class UserAuthenticated(msg: BasicUserData) extends OK
  case object UserRejected extends Bad("User rejected")
  case class AuthenticateUser(login: String, password: String) extends Command

  object UserRegisterValidator {
    private val ValidationOk = Right(())
    val MinLoginLength = 3

    def validate(login: String, email: String, password: String): Either[String, Unit] =
      for {
        _ <- validLogin(login.trim).right
        _ <- validEmail(email.trim).right
        _ <- validPassword(password.trim).right
      } yield ()

    private def validLogin(login: String) = if (login.length >= MinLoginLength) ValidationOk else Left("Login is too short!")

    private val emailRegex = """^[a-zA-Z0-9\.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$""".r

    private def validEmail(email: String) = if (emailRegex.findFirstMatchIn(email).isDefined) ValidationOk else Left("Invalid e-mail!")

    private def validPassword(password: String) = if (password.nonEmpty) ValidationOk else Left("Password cannot be empty!")
  }

}

