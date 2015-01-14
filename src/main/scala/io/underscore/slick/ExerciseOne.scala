package io.underscore.slick

import java.sql.Timestamp
import java.util.{ Date, Properties }
import scala.slick.driver.SQLiteDriver.simple._
import java.util.Calendar

object ExerciseOne extends App {

  final case class Message(id: Long, from: String, content: String, when: Timestamp)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender = column[String]("sender")
    def content = column[String]("content")
    def ts = column[Timestamp]("ts")
    def * = (id, sender, content, ts) <> (Message.tupled, Message.unapply)
  }

  lazy val messages = TableQuery[MessageTable]
  
  //Drive work around.
  val props = new Properties()
  props.setProperty("date_precision", "SECONDS")

  Database.forURL("jdbc:sqlite:essential-slick.db", driver = "org.sqlite.JDBC", prop = props) withSession {
    implicit session ⇒

      //Define a query 
      val halSays = for {
        message ← messages
        if message.sender === "HAL"
      } yield message

      val aternateHalSays = messages.filter(_.sender === "HAL")

      //Date based query
      val now = Calendar.getInstance()
      now.add(Calendar.MINUTE, -30)
      val recent: Timestamp = new Timestamp(now.getTimeInMillis())
     
      val recentMessages = halSays.filter(_.ts < recent)
      
      println(halSays.run)

  }

}