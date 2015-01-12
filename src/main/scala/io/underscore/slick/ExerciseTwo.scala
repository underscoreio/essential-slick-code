package io.underscore.slick

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable
import org.joda.time.DateTime

object ExerciseTwo extends Exercise {

  final case class Message(id: Long = 0L, from: String, content: String, when: DateTime)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def from = column[String]("from")
    def content = column[String]("content")
    def when = column[DateTime]("when")
    def * = (id, from, content, when) <> (Message.tupled, Message.unapply)
  }

  lazy val messages = TableQuery[MessageTable]

  Database.forURL("jdbc:sqlite:essential-slick.db", user = "essential", password = "trustno1", driver = "org.sqlite.JDBC") withSession {
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
      val time = new DateTime(2001, 2, 17, 10, 22, 50, 51)

      // Populate with some data: 
      messages += Message(0, "Dave Bowman", "Hello, HAL. Do you read me, HAL?", time)
      //
      messages ++= Seq(
        Message(0, "HAL", "Affirmative, Dave. I read you.", time.plusSeconds(2)),
        Message(0, "Dave Bowman", "Open the pod bay doors, HAL.", time.plusSeconds(2)),
        Message(0, "HAL", "I'm sorry, Dave. I'm afraid I can't do that.", time.plusSeconds(2)),
        Message(0, "Dave Bowman", "What's the problem?", time.plusSeconds(2)),
        Message(0, "HAL", "I think you know what the problem is just as well as I do.", time.plusSeconds(3)),
        Message(0, "Dave Bowman", "What are you talking about, HAL?", time.plusSeconds(2)),
        Message(0, "HAL", "This mission is too important for me to allow you to jeopardize it.", time.plusSeconds(4)),
        Message(0, "Dave Bowman", "I don't know what you're talking about, HAL.", time.plusSeconds(3)),
        Message(0, "HAL", "I know that you and Frank were planning to disconnect me, and I'm afraid that's something I cannot allow to happen.", time.plusSeconds(2)),
        Message(0, "Dave Bowman", "[feigning ignorance] Where the hell did you get that idea, HAL?", time.plusSeconds(6)),
        Message(0, "HAL", "Dave, although you took very thorough precautions in the pod against my hearing you, I could see your lips move.", time.plusSeconds(3)),
        Message(0, "Dave Bowman", "Alright, HAL. I'll go in through the emergency airlock.", time.plusSeconds(9)),
        Message(0, "HAL", "Without your space helmet, Dave? You're going to find that rather difficult.", time.plusSeconds(4)),
        Message(0, "Dave Bowman", "HAL, I won't argue with you anymore! Open the doors!", time.plusSeconds(5)),
        Message(0, "HAL", "Dave, this conversation can serve no purpose anymore. Goodbye.", time.plusSeconds(2)))

      //Define a query 
      val query = for {
        message ← messages
        if message.from === "HAL"
      } yield message

      //Execute a query.
      val messages_from_hal = query.run

      println(messages_from_hal)
  }

}