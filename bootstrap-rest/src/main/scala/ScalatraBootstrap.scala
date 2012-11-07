import java.util.Date
import pl.softwaremill.bootstrap.rest.{UsersServlet, EntriesServlet, UptimeServlet}
import org.scalatra._
import javax.servlet.ServletContext

/**
 * This is the ScalatraBootstrap bootstrap file. You can use it to mount servlets or
 * filters. It's also a good place to put initialization code which needs to
 * run at application start (e.g. database configurations), and init params.
 */
class ScalatraBootstrap extends LifeCycle {

  val PREFIX = "/rest"

  override def init(context: ServletContext) {

    // Mount one or more servlets
    context.mount(new EntriesServlet, PREFIX + "/entries")
    context.mount(new UptimeServlet, PREFIX + "/uptime")
    context.mount(new UsersServlet, PREFIX + "/users")
  }

}
