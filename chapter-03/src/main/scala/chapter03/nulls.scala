package chapter03

object NullExample extends App {

  trait Profile {
    val profile: scala.slick.driver.JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.simple._

    case class User(name: String, email: Option[String] = None, id: Long = 0L)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id    = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def name  = column[String]("name")
      def email = column[Option[String]]("email")

      def * = (name, email, id) <> (User.tupled, User.unapply)
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)
  }


  class Schema(val profile: scala.slick.driver.JdbcProfile) extends Tables with Profile

  val schema = new Schema(scala.slick.driver.H2Driver)

  import schema._, profile.simple._

  def db = Database.forURL("jdbc:h2:mem:chapter03", driver = "org.h2.Driver")

  db.withSession {
    implicit session =>

      users.ddl.create

      // Users:
      val daveId: Long = insertUser += User("Dave", Some("dave@example.org"))
      val halId:  Long = insertUser += User("HAL")
      val elena:  Long = insertUser += User("Elena", Some("elena@example.org"))

      val oe: Option[String] =  Some("dave@example.org")
      val e: String = "dave@example.org"


      def filterByEmail(email: Option[String]) =
        users.filter(u => u.email.isEmpty || u.email === email)

     println( filterByEmail(Some("elena@example.org")).run )
     //println( filterByEmail(None).run )

  }

}