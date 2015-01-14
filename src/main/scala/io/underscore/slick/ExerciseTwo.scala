package io.underscore.slick

import java.sql.Timestamp
import java.util.Properties

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable

object ExerciseTwo extends Exercise {

  final case class Message(id: Long = 0L, from: String, content: String, when: Timestamp)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender = column[String]("sender")
    def content = column[String]("content")
    def ts = column[Timestamp]("ts")

    def * = (id, sender, content, ts) <> (Message.tupled, Message.unapply)
  }

  lazy val messages = TableQuery[MessageTable]

  val props = new Properties()
  props.setProperty("date_precision","SECONDS")
  
  
  Database.forURL("jdbc:sqlite:essential-slick.db", user = "essential", password = "trustno1", driver = "org.sqlite.JDBC",prop = props  ) withSession {
    implicit session ⇒

      //Create Schema 
      MTable.getTables(messages.baseTableRow.tableName).firstOption match {
        case None ⇒
          messages.ddl.create
        case Some(t) ⇒
          messages.ddl.drop
          messages.ddl.create
      }

      // Times taken from, offsets are a guess.    
      // http://www.mach25media.com/2001tl.html

    // Populate with some data: 
      messages += Message(0, "Dave Bowman", "Hello, HAL. Do you read me, HAL?",  new Timestamp(101, 1, 17, 10, 22, 50, 51))
      //
      messages ++= Seq(
        Message(0, "HAL", "Affirmative, Dave. I read you.", new Timestamp(101, 1, 17, 10, 22, 53, 51)),
        Message(0, "Dave Bowman", "Open the pod bay doors, HAL.",  new Timestamp(101, 1, 17, 10, 22, 56, 51)),
        Message(0, "HAL", "I'm sorry, Dave. I'm afraid I can't do that.",  new Timestamp(101, 1, 17, 10, 22, 59, 51)))

      //Define a query 
      val query = for {
        message ← messages
        if message.sender === "HAL"
      } yield message

      //Execute a query.
      val messages_from_hal = query.run
      
      
      println(messages_from_hal)
  }

}