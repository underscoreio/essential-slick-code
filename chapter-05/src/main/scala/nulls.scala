import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.JdbcProfile

// Code relating to 5.3.1 "Nullable Columns"

object NullExample extends App {

  trait Profile {
    val profile: JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.api._

    case class User(name: String, email: Option[String] = None, id: Long = 0L)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id    = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def name  = column[String]("name")
      def email = column[Option[String]]("email")

      def * = (name, email, id).mapTo[User]
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)
  }

  class Schema(val profile: JdbcProfile) extends Tables with Profile

  val schema = new Schema(slick.jdbc.H2Profile)

  import schema._, profile.api._

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  val db = Database.forConfig("chapter05")

  val program = for {
    _ <- users.schema.create
    daveId <- insertUser += User("Dave", Some("dave@example.org"))
    halId  <- insertUser += User("HAL")
    elena  <- insertUser += User("Elena", Some("elena@example.org"))
    folks  <- users.result
  } yield folks


  println("\nUsers with optional email addresses:")
  exec(program).foreach { println }

  println("\nUsers sorted with NULLs last")
  val sortingOnNullableExample = users.sortBy { _.name.asc.nullsLast }.result
  exec(sortingOnNullableExample).foreach { println }

}
