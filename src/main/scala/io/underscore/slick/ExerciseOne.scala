package io.underscore.slick

import scala.slick.driver.PostgresDriver.simple._
import org.joda.time.DateTime

object ExerciseOne extends Exercise {

  final case class Message(id: Long = 0L,from: String, content: String, when: DateTime)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def from = column[String]("from")
    def content = column[String]("content")
    def when = column[DateTime]("when")
    def * = (id,from, content, when) <> (Message.tupled, Message.unapply)
  }

  lazy val messages = TableQuery[MessageTable]

  Database.forURL("jdbc:postgresql:essential-slick", user = "essential", password = "trustno1", driver = "org.postgresql.Driver") withSession {
    implicit session ⇒

      //Create Schema 
      messages.ddl.create

      //Define a query 
      val query = for {
        message ← messages
        if message.from === "HAL"
      } yield message

      //Execute a query.
      val messages_from_HAL: List[Message] = query.list

      println(s" ${messages_from_HAL}")
  }

}