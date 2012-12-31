import com.mongodb.casbah.{ MongoDB, MongoConnection }
import java.util.concurrent.{ ScheduledExecutorService, TimeUnit, Executors }
import org.json4s.{ DefaultFormats, Formats }
import pl.softwaremill.bootstrap.dao._
import pl.softwaremill.bootstrap.rest.{ UsersServlet, EntriesServlet, UptimeServlet }
import org.scalatra._
import javax.servlet.ServletContext
import pl.softwaremill.bootstrap.service.config.BootstrapConfiguration
import pl.softwaremill.bootstrap.service.schedulers.{ DummyEmailSendingService, EmailSendingService, ProductionEmailSendingService }
import pl.softwaremill.bootstrap.service.templates.EmailTemplatingEngine
import pl.softwaremill.bootstrap.service.user.{ RegistrationDataValidator, UserService }
import pl.softwaremill.bootstrap.service.EntryService

/**
 * This is the ScalatraBootstrap bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle {

  val Prefix = "/rest"
  val MongoKey = "MONGO_DB"
  val SchedulerKey = "SCHEDULER"

  override def init(context: ServletContext) {

    val factory = createDAOsFactory(context)
    val emailSendingService = createEmailSendingService

    val scheduler = Executors.newScheduledThreadPool(4)
    context.put(SchedulerKey, scheduler)
    scheduler.scheduleAtFixedRate(emailSendingService, 60, 60, TimeUnit.SECONDS)

    val emailTemplatingEngine = new EmailTemplatingEngine

    val userService = new UserService(factory.userDAO, new RegistrationDataValidator(), emailSendingService, emailTemplatingEngine)
    val entryService = new EntryService(factory.entryDAO, factory.userDAO)

    // Mount one or more servlets
    context.mount(new EntriesServlet(entryService, userService), Prefix + "/entries")
    context.mount(new UptimeServlet, Prefix + "/uptime")
    context.mount(new UsersServlet(userService), Prefix + "/users")
  }

  def createDAOsFactory(context: ServletContext): StorageFactory = {
    if (System.getProperty("withInMemory") != null) {
      new InMemoryFactory
    } else {
      implicit val mongoConn = MongoConnection()("bootstrap")
      context.put(MongoKey, mongoConn)
      new MongoFactory
    }
  }

  def createEmailSendingService: EmailSendingService = {
    if (BootstrapConfiguration.smtpHost != null) {
      new ProductionEmailSendingService
    } else {
      new DummyEmailSendingService
    }
  }

  override def destroy(context: ServletContext) {
    context.get(MongoKey).foreach(_.asInstanceOf[MongoDB].underlying.getMongo.close())
    context.get(SchedulerKey).foreach(_.asInstanceOf[ScheduledExecutorService].shutdownNow())
  }

}
