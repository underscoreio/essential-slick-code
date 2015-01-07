package io.underscore.slick

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.meta.MTable

object ExerciseTwo extends App {

  final case class Message(from: String, message: String, id: Long = 0L)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def from = column[String]("from")
    def message = column[String]("message")
    def * = (from, message, id) <> (Message.tupled, Message.unapply)
  }

  lazy val messages = TableQuery[MessageTable]

  Database.forURL("jdbc:postgresql:essential-slick", user = "essential", password = "trustno1", driver = "org.postgresql.Driver") withSession {
    implicit session ⇒

      //Create Schema 
      MTable.getTables(messages.baseTableRow.tableName).firstOption match {
        case None ⇒
          messages.ddl.create
        case Some(t) ⇒
          messages.ddl.drop
          messages.ddl.create
      }

      // Populate with some data: 
      messages += Message("Dave Bowman", "Hello, HAL. Do you read me, HAL?")

      messages ++= Seq(
        Message("HAL", "Affirmative, Dave. I read you."),
        Message("Dave Bowman", "Open the pod bay doors, HAL."),
        Message("HAL", "I'm sorry, Dave. I'm afraid I can't do that."),
        Message("Dave Bowman", "What's the problem?"),
        Message("HAL", "I think you know what the problem is just as well as I do."),
        Message("Dave Bowman", "What are you talking about, HAL?"),
        Message("HAL", "This mission is too important for me to allow you to jeopardize it."),
        Message("Dave Bowman", "I don't know what you're talking about, HAL."),
        Message("HAL", "I know that you and Frank were planning to disconnect me, and I'm afraid that's something I cannot allow to happen."),
        Message("Dave Bowman", "[feigning ignorance] Where the hell did you get that idea, HAL?"),
        Message("HAL", "Dave, although you took very thorough precautions in the pod against my hearing you, I could see your lips move."),
        Message("Dave Bowman", "Alright, HAL. I'll go in through the emergency airlock."),
        Message("HAL", "Without your space helmet, Dave? You're going to find that rather difficult."),
        Message("Dave Bowman", "HAL, I won't argue with you anymore! Open the doors!"),
        Message("HAL", "Dave, this conversation can serve no purpose anymore. Goodbye."))

      //Lets retry that query. 
      val query = for {
        message ← messages
        if message.from === "HAL"
      } yield message

      //Execute a query.
      val number_of_messages_from_HAL:Int = query.size.run

      println(s"There are ${number_of_messages_from_HAL} messages from HAL.")
  }

}