import com.mongodb.casbah.{MongoDB, MongoConnection}
import java.util.concurrent.{ScheduledExecutorService, TimeUnit, Executors}
import org.json4s.{DefaultFormats, Formats}
import pl.softwaremill.bootstrap.dao._
import pl.softwaremill.bootstrap.rest.{UsersServlet, EntriesServlet, UptimeServlet}
import org.scalatra._
import javax.servlet.ServletContext
import pl.softwaremill.bootstrap.service.config.BootstrapConfiguration
import pl.softwaremill.bootstrap.service.schedulers.{DummyEmailSendingService, EmailSendingService, ProductionEmailSendingService}
import pl.softwaremill.bootstrap.service.user.{RegistrationDataValidator, UserService}
import pl.softwaremill.bootstrap.service.EntryService

/**
 * This is the ScalatraBootstrap bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle {

  val PREFIX = "/rest"
  val MONGO_DB_KEY: String = "MONGO_DB"
  val SCHEDULER_KEY: String = "SCHEDULER"

  override def init(context: ServletContext) {

    val factory = createDAOsFactory(context)
    val emailSendingService = createEmailSendingService

    val scheduler = Executors.newScheduledThreadPool(4)
    context.put(SCHEDULER_KEY, scheduler)
    scheduler.scheduleAtFixedRate(emailSendingService, 30, 30, TimeUnit.SECONDS)

    val userService = new UserService(factory.userDAO, new RegistrationDataValidator())
    val entryService = new EntryService(factory.entryDAO, factory.userDAO)

    // Mount one or more servlets
    context.mount(new EntriesServlet(entryService, userService), PREFIX + "/entries")
    context.mount(new UptimeServlet, PREFIX + "/uptime")
    context.mount(new UsersServlet(userService), PREFIX + "/users")
  }

  def createDAOsFactory(context: ServletContext): StorageFactory = {
    if (System.getProperty("withInMemory") != null) {
      new InMemoryFactory
    }
    else {
      implicit val mongoConn = MongoConnection()("bootstrap")
      context.put(MONGO_DB_KEY, mongoConn)
      new MongoFactory
    }
  }

  def createEmailSendingService: EmailSendingService = {
    if (BootstrapConfiguration.smtpHost.isEmpty == false) {
      new ProductionEmailSendingService
    }
    else {
      new DummyEmailSendingService
    }
  }

  override def destroy(context: ServletContext) {
    context.get(MONGO_DB_KEY).foreach(_.asInstanceOf[MongoDB].underlying.getMongo.close())
    context.get(SCHEDULER_KEY).foreach(_.asInstanceOf[ScheduledExecutorService].shutdownNow())
  }

}
