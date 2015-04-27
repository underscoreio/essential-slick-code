package chapter03

import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import scala.slick.driver.JdbcProfile

trait Profile {
  // Place holder for a specific profile
  val profile: scala.slick.driver.JdbcProfile
}

trait Tables {
  // Self-type indicating that our tables must be mixed in with a Profile
  this: Profile =>

  // Whatever that Profile is, we import it as normal:
  import profile.simple._

  implicit val jodaDateTimeType =
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.getMillis),
      ts => new DateTime(ts.getTime, UTC)
    )

  // Row representation:
  case class Message(sender: String, content: String, ts: DateTime, id: Long = 0L)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender  = column[String]("sender")
    def content = column[String]("content")
    def ts      = column[DateTime]("ts")
    def * = (sender, content, ts, id) <> (Message.tupled, Message.unapply)
  }

  lazy val messages = TableQuery[MessageTable]

  def numSenders = messages.map(_.sender).countDistinct
}


// Bring all the components together:
class Schema(val profile: JdbcProfile) extends Tables with Profile

object StructureExample extends App {

  // A specific schema with a particular driver:
  val schema = new Schema(scala.slick.driver.H2Driver)

  // Use the schema:
  import schema._, profile.simple._

  def db = Database.forURL("jdbc:h2:mem:chapter03", driver="org.h2.Driver")

  db.withSession {
    implicit session =>

      // Create the table:
      messages.ddl.create

      // Insert the conversation, which took place in Feb, 2001:
      val start = new DateTime(2001,2,17, 10,22,50)

      messages ++= Seq(
        Message("Dave", "Hello, HAL. Do you read me, HAL?",             start),
        Message("HAL",  "Affirmative, Dave. I read you.",               start plusSeconds 2),
        Message("Dave", "Open the pod bay doors, HAL.",                 start plusSeconds 4),
        Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that.", start plusSeconds 6)
      )

      println(numSenders.run)
  }
}