import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.JdbcProfile

// Code relating to 5.3.5 "Foreign Keys"

object ForeignKeyExample extends App {

  trait Profile {
    val profile: JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.api._

    //
    // Users in the user table have an id which will
    // be used as a foreign key in the message table.
    //
    case class User(name: String, id: Long = 0L)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id   = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def name = column[String]("name")

      def * = (name, id).mapTo[User]
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    //
    // The message table, in which we represent the sender as
    // the key in the user table (rather than a String name
    // we've used up until this point)
    //
    case class Message(
        senderId: Long,
        content:  String,
        id:       Long = 0L)

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id       = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[Long]("sender")
      def content  = column[String]("content")

      def * = (senderId, content, id).mapTo[Message]

      //
      // Establish a FK relation:
      //
      def sender = foreignKey("sender_fk", senderId, users)(_.id, onDelete=ForeignKeyAction.Cascade)
    }

    lazy val messages = TableQuery[MessageTable]

    // The schema for both tables:
    lazy val ddl = users.schema ++ messages.schema
  }


  class Schema(val profile: JdbcProfile) extends Tables with Profile

  val schema = new Schema(slick.jdbc.H2Profile)

  import schema._, profile.api._

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  val db = Database.forConfig("chapter05")

  val initalise =
    for {
    _      <- ddl.create
    halId  <- insertUser += User("HAL")
    daveId <- insertUser += User("Dave")
    count  <- messages ++= Seq(
               Message(daveId, "Hello, HAL. Do you read me, HAL?"),
               Message(halId,  "Affirmative, Dave. I read you."),
               Message(daveId, "Open the pod bay doors, HAL."),
               Message(halId,  "I'm sorry, Dave. I'm afraid I can't do that.")
             )
  } yield count

  // A simple join using the foreign key:
  val join = for {
    msg <- messages
    usr <- msg.sender
  } yield (usr.name, msg.content)

  // Set up the database:
  exec(initalise)

  println("\nResult of foreign key join:")
  println(exec(join.result))

  // If we delete a user, that user's messages will be
  // deleted because the foreign key has been configured
  // to CASCADE
  println("\nMessages after deleting the user Dave:")
  val deleteDave = for {
    daveId       <- users.filter(_.name === "Dave").map(_.id).result.headOption
    rowsAffected <- messages.filter(_.senderId === daveId).delete
  } yield rowsAffected
  exec(deleteDave)
  println(exec(messages.result))
}
