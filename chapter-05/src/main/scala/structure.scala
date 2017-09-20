import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.JdbcProfile

trait Profile {
  val profile : JdbcProfile
}

trait Tables {
  // Self-type indicating that our tables must be mixed in with a Profile
  this: Profile =>

  // Whatever that Profile is, we import it as normal:
  import profile.api._

  // Row and table definitions here as normal
  case class Message(sender: String, content: String, id: Long = 0L)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender  = column[String]("sender")
    def content = column[String]("content")
    def * = (sender, content, id).mapTo[Message]
  }

  object messages extends TableQuery( new MessageTable(_)) {
    def messagesFrom(name: String) = this.filter(_.sender === name)
    val numSenders = this.map(_.sender).distinct.length
  }

}


object StructureExample extends App {

  // Bring all the components together:
  class Schema(val profile: JdbcProfile) extends Tables with Profile

  // A specific schema with a particular driver:
  val schema = new Schema(slick.jdbc.H2Profile)

  // Use the schema:
  import schema._, profile.api._

  val db = Database.forConfig("chapter05")

  // Insert the conversation
  val msgs = Seq(
      Message("Dave", "Hello, HAL. Do you read me, HAL?"),
      Message("HAL",  "Affirmative, Dave. I read you."),
      Message("Dave", "Open the pod bay doors, HAL."),
      Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that.")
    )

  val program = for {
    _ <- messages.schema.create
    _ <- messages ++= msgs
    c <- messages.numSenders.result
    } yield c

  val result = Await.result(db.run(program), 2 seconds)
  println(s"Number of senders $result")
}
