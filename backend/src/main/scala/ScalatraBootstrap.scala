import java.util.Locale
import javax.servlet.ServletContext

import com.softwaremill.bootzooka.Beans
import com.softwaremill.bootzooka.common.logging.AsyncErrorReportingLogAppender
import com.softwaremill.bootzooka.api._
import com.softwaremill.bootzooka.api.swagger.SwaggerServlet
import org.scalatra.{LifeCycle, ScalatraServlet}

/**
 * This is the ScalatraBootstrap bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle with Beans {

  override def init(context: ServletContext) {
    Locale.setDefault(Locale.US) // set default locale to prevent Scalatra from sending cookie expiration date in polish format :)

    // Initialize error reporting client.
    AsyncErrorReportingLogAppender(config, errorReporter).init()

    sqlDatabase.updateSchema()

    def mountServlet(servlet: ScalatraServlet with Mappable) {
      servlet match {
        case s: SwaggerMappable => context.mount(s, s.fullMappingPath, s.name)
        case _ => context.mount(servlet, servlet.fullMappingPath)
      }
    }

    mountServlet(new UsersServlet(userService))
    mountServlet(new PasswordRecoveryServlet(passwordRecoveryService, userService))
    mountServlet(new VersionServlet)
    mountServlet(new SwaggerServlet)

    context.setAttribute("appObject", this)
  }

  override def destroy(context: ServletContext) {
    sqlDatabase.close()
  }
}
