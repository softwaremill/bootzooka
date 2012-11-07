package pl.softwaremill.bootstrap.auth

case class User(login: String, password: String)

object Users {

    val list = List(
        User("admin", "admin"),
        User("user", "user")
    )

}
