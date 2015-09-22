import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import java.sql.Timestamp
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf


trait Profile {
  val profile  : JdbcProfile
}

trait Tables {
  // Self-type indicating that our tables must be mixed in with a Profile
  this: Profile =>

  // Whatever that Profile is, we import it as normal:
  import profile.api._

  implicit val jodaDateTimeType =
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.getMillis),
      ts => new DateTime(ts.getTime, UTC)
    )

  // Row and table definitions here as normal
  case class Message(sender: String, content: String, ts: DateTime, id: Long = 0L)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender  = column[String]("sender")
    def content = column[String]("content")
    def ts      = column[DateTime]("ts")
    def * = (sender, content, ts, id) <> (Message.tupled, Message.unapply)
  }

  object messages extends TableQuery( new MessageTable(_)) {
    val findBySender = this.findBy(_.sender)
    val numSenders   = this.map(_.sender).countDistinct
  }
  
}


object StructureExample extends App {

  // Bring all the components together:
  class Schema(val profile: JdbcProfile) extends Tables with Profile

  // A specific schema with a particular driver:
  val schema = new Schema(slick.driver.H2Driver)

  // Use the schema:
  import schema._, profile.api._

  val db = Database.forConfig("chapter04")

  // Insert the conversation, which took place in Feb, 2001:
  val start = new DateTime(2001,2,17, 10,22,50)

  val msgs = Seq(
      Message("Dave", "Hello, HAL. Do you read me, HAL?",             start),
      Message("HAL",  "Affirmative, Dave. I read you.",               start plusSeconds 2),
      Message("Dave", "Open the pod bay doors, HAL.",                 start plusSeconds 4),
      Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that.", start plusSeconds 6))

  val program = for {
    _ <- messages.schema.create
    _ <- messages ++= msgs
    c <- messages.numSenders.result
    } yield c

  val result =  Await.result(db.run(program), 2 seconds)

  println(s"Number of senders $result")



}