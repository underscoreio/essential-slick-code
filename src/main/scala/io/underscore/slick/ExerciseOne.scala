package io.underscore.slick

import scala.slick.driver.PostgresDriver.simple._

object ExerciseOne extends App {

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
      messages.ddl.create

      //Define a query 
      val query = for {
        message ← messages
        if message.from === "jono"
      } yield message

      //Execute a query.
      val messages_from_jono: List[Message] = query.list

      println(s" ${messages_from_jono}")
  }

}