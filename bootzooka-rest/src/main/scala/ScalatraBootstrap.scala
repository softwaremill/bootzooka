import com.mongodb.Mongo
import java.util.concurrent.TimeUnit
import net.liftweb.mongodb.{DefaultMongoIdentifier, MongoDB}
import com.softwaremill.bootzooka.Beans
import com.softwaremill.bootzooka.rest._
import org.scalatra._
import javax.servlet.ServletContext

/**
 * This is the ScalatraBootstrap bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle with Beans {
  val Prefix = "/rest/"

  override def init(context: ServletContext) {
    MongoDB.defineDb(DefaultMongoIdentifier, new Mongo, "bootzooka")

    scheduler.scheduleAtFixedRate(emailSendingService, 60, 1, TimeUnit.SECONDS)

    context.mount(new EntriesServlet(entryService, userService, swagger), Prefix + EntriesServlet.MAPPING_PATH)
    context.mount(new UptimeServlet, Prefix + "uptime")
    context.mount(new UsersServlet(userService, swagger), Prefix + UsersServlet.MAPPING_PATH)
    context.mount(new PasswordRecoveryServlet(passwordRecoveryService, userService), Prefix + "passwordrecovery")

    context.mount(new SwaggerApiDoc(swagger), Prefix + "api-docs/*")

    context.put("bootzooka", this)
  }


  override def destroy(context: ServletContext) {
    scheduler.shutdownNow()
  }

}
