package io.underscore.slick

import java.sql.Timestamp
import java.util.Properties

import scala.slick.driver.SQLiteDriver.simple._
import scala.slick.jdbc.meta.MTable
import scala.slick.model.PrimaryKey


object ExerciseThree extends Exercise {

  final case class ColourShape(colour: Long, shape: String)

  final class ColourShapeTable(tag: Tag) extends Table[ColourShape](tag, "colour_shape") {
    def colour = column[Long]("colour")
    def shape = column[String]("shape")

    def pk = primaryKey("colour_shape_pk", (colour, shape))

    def * = (colour, shape) <> (ColourShape.tupled, ColourShape.unapply)

  }

  lazy val coluredShapes = TableQuery[ColourShapeTable]

  
  final case class User(id: Long, name: String)

  final class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id",O.AutoInc,O.PrimaryKey)
    def ts = column[Timestamp]("ts")
    def name = column[String]("name")

    def * = (id, name) <> (User.tupled, User.unapply)

  }

  lazy val users = TableQuery[UserTable]

  
  final case class Message(id: Long, sender: Long, to: Option[Long], content: String, ts: Timestamp)

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {

    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def senderId = column[Long]("sender")
    def sender = foreignKey("sender_fk", senderId, users)(_.id)
    def toId = column[Option[Long]]("to")
    def to = foreignKey("to_fk", toId, users)(_.id)
    def content = column[String]("content")
    def ts = column[Timestamp]("ts")

    def * = (id, senderId, toId, content, ts) <> (Message.tupled, Message.unapply)
  }

  lazy val messages = TableQuery[MessageTable]

  val props = new Properties()
  props.setProperty("date_precision","SECONDS")  
  
  Database.forURL("jdbc:sqlite:essential-slick.db", user = "essential", password = "trustno1", driver = "org.sqlite.JDBC", prop = props) withSession {
    implicit session ⇒
      val tables = messages :: users :: coluredShapes :: Nil

      for {
        table ← tables
      } {
        if (MTable.getTables(table.baseTableRow.tableName).firstOption.isDefined) {
          println(s"drop ${table.baseTableRow.tableName}")
          table.ddl.drop
        }
        table.ddl.create
      }

      val direct_message_query = for {
        message ← messages
        if message.toId.isDefined
      } yield message

      println(direct_message_query)

  }

}