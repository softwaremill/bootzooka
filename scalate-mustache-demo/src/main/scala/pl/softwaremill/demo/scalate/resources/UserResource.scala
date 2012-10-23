package pl.softwaremill.demo.scalate.resources

import com.sun.jersey.api.view.ImplicitProduces
import javax.ws.rs.{FormParam, POST, Path}
import javax.ws.rs.core.Response
import java.net.URI

@Path("/user")
@ImplicitProduces(Array("text/html;qs=5"))
class UserResource {

  def user = User("Lukasz")

  @POST
  @Path("/update")
  def update(@FormParam("name") name: String): Response = {
    println("New user: " + name)
    Response.seeOther(URI.create("/user")).build()
  }

}

case class User(name: String)
