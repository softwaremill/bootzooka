import javax.servlet.ServletContext

import com.mongodb.{MongoClient, Mongo}
import com.softwaremill.bootzooka.Beans
import com.softwaremill.bootzooka.rest._
import net.liftweb.mongodb.MongoDB
import net.liftweb.util.DefaultConnectionIdentifier
import org.scalatra.LifeCycle

/**
 * This is the ScalatraBootstrap bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle with Beans {
  val Prefix = "/rest/"

  override def init(context: ServletContext) {
    MongoDB.defineDb(DefaultConnectionIdentifier, new MongoClient, "bootzooka")
    context.mount(new UsersServlet(userService), Prefix + UsersServlet.MAPPING_PATH)
    context.mount(new PasswordRecoveryServlet(passwordRecoveryService, userService), Prefix + "passwordrecovery")

    context.setAttribute("bootzooka", this)
  }

  override def destroy(context: ServletContext) {

  }
}
