import javax.servlet.ServletContext

import com.mongodb.MongoClient
import com.softwaremill.bootzooka.Beans
import com.softwaremill.bootzooka.rest._
import com.softwaremill.bootzooka.rest.swagger.{BootzookaSwagger, SwaggerServlet}
import net.liftweb.mongodb.MongoDB
import net.liftweb.util.DefaultConnectionIdentifier
import org.scalatra.{LifeCycle, ScalatraServlet}

/**
 * This is the ScalatraBootstrap bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle with Beans {

  implicit val swagger = new BootzookaSwagger

  override def init(context: ServletContext) {
    MongoDB.defineDb(DefaultConnectionIdentifier, new MongoClient, "bootzooka")

    def mountServlet(servlet: ScalatraServlet with Mappable) {
      servlet match {
        case s: SwaggerMappable => context.mount(s, s.fullMappingPath, s.name)
        case _ => context.mount(servlet, servlet.fullMappingPath)
      }
    }

    mountServlet(new UsersServlet(userService))
    mountServlet(new PasswordRecoveryServlet(passwordRecoveryService, userService))
    mountServlet(new SwaggerServlet)

    context.setAttribute("bootzooka", this)
  }

  override def destroy(context: ServletContext) {

  }
}
