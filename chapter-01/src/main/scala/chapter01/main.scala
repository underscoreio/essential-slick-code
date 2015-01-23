package chapter01

import scala.slick.driver.H2Driver.simple._
import java.sql.Timestamp

object Example extends App {

 // Row representation:
  final case class Message(sender: String, content: String, ts: Timestamp, id: Long = 0L)

  // Schema:
  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender  = column[String]("sender")
    def content = column[String]("content")
    def ts      = column[Timestamp]("ts")
    def * = (sender, content, ts, id) <> (Message.tupled, Message.unapply)
  }

  // Table:
  lazy val messages = TableQuery[MessageTable]

  // Database connection details:
  def db = Database.forURL("jdbc:h2:mem:chapter01", driver="org.h2.Driver")

  // Query execution:
  db.withSession {
    implicit session =>

      // Create the table:
      messages.ddl.create

      // Insert data:
      val start = 98240532000L

      messages ++= Seq(
        Message("Dave", "Hello, HAL. Do you read me, HAL?", new Timestamp(start)),
        Message("HAL",  "Affirmative, Dave. I read you.", new Timestamp(start + 2000)),
        Message("Dave", "Open the pod bay doors, HAL.", new Timestamp(start + 4000)),
        Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that.", new Timestamp(start + 6000))
      )

      // Our first query:
      val halSays = for {
        message <- messages
        if message.sender === "HAL"
      } yield message

      val results = halSays.run
      println(results)
  }
}