package chapter03

import scala.slick.driver.H2Driver.simple._
import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC

object Example extends App {

  // Custom column mapping:
  implicit val jodaDateTimeType =
    MappedColumnType.base[DateTime, Timestamp](
      dt ⇒ new Timestamp(dt.getMillis),
      ts ⇒ new DateTime(ts.getTime, UTC))

  // Row representation:
  final case class Message(sender: String, content: String, ts: DateTime, id: Long = 0L)

  //tuple represenation of a user 
  //type  User = (String,Long)
  // case class representation of a user 
  final case class User(name: String, id: Long = 0L)
  
  
  // Schema:
  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender = column[String]("sender")
    def content = column[String]("content")
    def ts = column[DateTime]("ts")
    def * = (sender, content, ts, id) <> (Message.tupled, Message.unapply)
  }

//  // Table:
//  lazy val messages = TableQuery[MessageTable]
//
//  // Database connection details:
//  def db = Database.forURL("jdbc:h2:mem:chapter02", driver = "org.h2.Driver")
//
//  // Query execution:
//  db.withSession {
//    implicit session =>
//
//      // Create the table:
//      messages.ddl.create
//
//      // Insert the conversation, which took place in Feb, 2001:
//      val start = new DateTime(2001, 2, 17, 10, 22, 50)
//
//      messages ++= Seq(
//        Message("Dave", "Hello, HAL. Do you read me, HAL?", start),
//        Message("HAL", "Affirmative, Dave. I read you.", start plusSeconds 2),
//        Message("Dave", "Open the pod bay doors, HAL.", start plusSeconds 4),
//        Message("HAL", "I'm sorry, Dave. I'm afraid I can't do that.", start plusSeconds 6))
//
//      messages.iterator.foreach(println)
//
//  }

}