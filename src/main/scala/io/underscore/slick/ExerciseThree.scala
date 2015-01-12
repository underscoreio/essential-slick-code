package io.underscore.slick

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable
import org.joda.time.DateTime
import scala.slick.model.PrimaryKey

object ExerciseThree extends Exercise {

  final case class User(id: Long, name: String)

  final class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id, name) <> (User.tupled, User.unapply)

  }

  lazy val users = TableQuery[UserTable]

  final case class Message(id: Long, from: Long, to: Option[Long], content: String, when: DateTime)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def fromId = column[Long]("from")
    def from = foreignKey("from_fk", fromId, users)(_.id)
    def toId = column[Option[Long]]("to")
    def to = foreignKey("to_fk", toId, users)(_.id)
    def content = column[String]("content")
    def when = column[DateTime]("when")

    def * = (id, fromId, toId, content, when) <> (Message.tupled, Message.unapply)
  }

  lazy val messages = TableQuery[MessageTable]

  Database.forURL("jdbc:sqlite:essential-slick.db", user = "essential", password = "trustno1", driver = "org.sqlite.JDBC") withSession {
    implicit session ⇒
      val ddl = messages.ddl ++ users.ddl

      //      Create Schema 
      MTable.getTables(messages.baseTableRow.tableName).firstOption match {
        case None ⇒
          ddl.create
        case Some(t) ⇒
          ddl.drop
          ddl.create
      }

      val direct_message_query = for {
        message ← messages
        if message.toId.isDefined
      } yield message

  }

}