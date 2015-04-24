package chapter04

import java.sql.Timestamp

import scala.slick.driver.JdbcDriver
import scala.slick.lifted.MappedTo

import org.joda.time.DateTime
import org.joda.time.DateTimeZone._


object MessagingSchema {

  final case class Id[A](value: Long) extends AnyVal with MappedTo[Long]

  trait Profile {
    val profile: scala.slick.driver.JdbcProfile
  }

  trait Tables {
    this: Profile ⇒

    import profile.simple._

    case class User(id: Option[Id[UserTable]], name: String, email: Option[String] = None)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id = column[Id[UserTable]]("id", O.AutoInc, O.PrimaryKey)
      def name = column[String]("name")
      def email = column[Option[String]]("email")

      def * = (id.?, name, email) <> (User.tupled, User.unapply)
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    case class Room(id: Option[Id[RoomTable]],title: String)

    class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
      def id = column[Id[RoomTable]]("id", O.PrimaryKey, O.AutoInc)
      def title = column[String]("title")
      def * = (id.?, title) <> (Room.tupled, Room.unapply)
    }

    lazy val rooms = TableQuery[RoomTable]
    lazy val insertRoom = rooms returning rooms.map(_.id)

    case class Occupant(roomId: Id[RoomTable], userId: Id[UserTable])

    class OccupantTable(tag: Tag) extends Table[Occupant](tag, "occupant") {
      def roomId = column[Id[RoomTable]]("room")
      def userId = column[Id[UserTable]]("user")
      def pk = primaryKey("occ_room_user_pk", (roomId, userId))
      def user = foreignKey("occ_user_fk", userId, users)(_.id)
      def room = foreignKey("occ_room_fk", roomId, rooms)(_.id)
      def * = (roomId, userId) <> (Occupant.tupled, Occupant.unapply)
    }

    lazy val occupants = TableQuery[OccupantTable]

    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt ⇒ new Timestamp(dt.getMillis),
        ts ⇒ new DateTime(ts.getTime, UTC))

    case class Message(senderId: Id[UserTable],
                       content: String,
                       ts: DateTime,
                       id: Id[MessageTable] = Id(0L),
                       toId: Option[Id[UserTable]] = None,
                       roomId: Option[Id[RoomTable]] = None,
                       readBy: Int)

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id = column[Id[MessageTable]]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[Id[UserTable]]("sender")
      def content = column[String]("content")
      def toId = column[Option[Id[UserTable]]]("to")
      def roomId = column[Option[Id[RoomTable]]]("room")
      def ts = column[DateTime]("ts")
      def readBy = column[Int]("readBy")
      def * = (senderId, content, ts, id, toId, roomId, readBy) <> (Message.tupled, Message.unapply)

      def sender = foreignKey("msg_sender_fk", senderId, users)(_.id)
      def to = foreignKey("msg_to_fk", toId, users)(_.id)
      def room = foreignKey("msg_room_fk", roomId, rooms)(_.id)
    }

    lazy val messages = TableQuery[MessageTable]

  }

  class Schema(val profile: scala.slick.driver.JdbcProfile) extends Tables with Profile
  case class DB(driver: JdbcDriver, url: String, clazz: String)
}