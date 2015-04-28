package chapter04

import java.sql.Timestamp

import scala.slick.driver.JdbcDriver
import scala.slick.lifted.MappedTo

import org.joda.time.DateTime
import org.joda.time.DateTimeZone._

object MessagingSchema {

  case class PK[A](value: Long) extends AnyVal with MappedTo[Long]

  trait Profile {
    val profile: scala.slick.driver.JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.simple._

    case class User(name: String, email: Option[String] = None, id: PK[UserTable] = PK(0))

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id    = column[PK[UserTable]]("id", O.AutoInc, O.PrimaryKey)
      def name  = column[String]("name")
      def email = column[Option[String]]("email")

      def * = (name, email, id) <> (User.tupled, User.unapply)
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    case class Room(title: String, id: PK[RoomTable] = PK(0L))

    class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
      def id    = column[PK[RoomTable]]("id", O.PrimaryKey, O.AutoInc)
      def title = column[String]("title")
      def * = (title, id) <> (Room.tupled, Room.unapply)
    }

    lazy val rooms = TableQuery[RoomTable]
    lazy val insertRoom = rooms returning rooms.map(_.id)

    case class Occupant(roomId: PK[RoomTable], userId: PK[UserTable])

    class OccupantTable(tag: Tag) extends Table[Occupant](tag, "occupant") {
      def roomId = column[PK[RoomTable]]("room")
      def userId = column[PK[UserTable]]("user")
      def pk   = primaryKey("occ_room_user_pk", (roomId, userId))
      def user = foreignKey("occ_user_fk", userId, users)(_.id)
      def room = foreignKey("occ_room_fk", roomId, rooms)(_.id)
      def * = (roomId, userId) <> (Occupant.tupled, Occupant.unapply)
    }

    lazy val occupants = TableQuery[OccupantTable]

    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime, UTC))

    case class Message(senderId: PK[UserTable],
                       content: String,
                       ts: DateTime,
                       roomId: Option[PK[RoomTable]] = None,
                       toId: Option[PK[UserTable]]   = None,
                       id: PK[MessageTable]          = PK(0L) )

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id       = column[PK[MessageTable]]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[PK[UserTable]]("sender")
      def content  = column[String]("content")
      def ts       = column[DateTime]("ts")
      def roomId   = column[Option[PK[RoomTable]]]("room") // in a particular room? or broadcast?
      def toId     = column[Option[PK[UserTable]]]("to")   // direct message?
      def * = (senderId, content, ts, roomId, toId, id) <> (Message.tupled, Message.unapply)

      def sender = foreignKey("msg_sender_fk", senderId, users)(_.id)
      def to     = foreignKey("msg_to_fk", toId, users)(_.id)
      def room   = foreignKey("msg_room_fk", roomId, rooms)(_.id)
    }

    lazy val messages = TableQuery[MessageTable]

    // Sample data set
    def populate(implicit session: Session): Unit = {

      // Print the schema:
      (users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).createStatements.foreach(println)

      // Execute the schema:
      (users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).create

      // A few users:
      val daveId:  PK[UserTable] = insertUser += User("Dave", Some("dave@example.org"))
      val halId:   PK[UserTable] = insertUser += User("HAL")
      val elenaId: PK[UserTable] = insertUser += User("Elena", Some("elena@example.org"))
      val frankId: PK[UserTable] = insertUser += User("Frank", Some("frank@example.org"))

      // rooms:
      val airLockId: PK[RoomTable] = insertRoom += Room("Air Lock")
      val podId:     PK[RoomTable] = insertRoom += Room("Pod")
      val brainId:   PK[RoomTable] = insertRoom += Room("Brain Room")

      // Put Dave in the Room:
      occupants ++= List(
        Occupant(airLockId, daveId),
        Occupant(airLockId, halId),
        Occupant(podId, daveId),
        Occupant(podId, frankId),
        Occupant(podId, halId) )

      // Insert the conversation, which took place in Feb, 2001:
      val airLockConversation = new DateTime(2001, 2, 17, 10, 22, 50)

      // Add some messages to the air lock room:
      messages ++= Seq(
        Message(daveId, "Hello, HAL. Do you read me, HAL?",             airLockConversation,               Some(airLockId)),
        Message(halId,  "Affirmative, Dave. I read you.",               airLockConversation plusSeconds 2, Some(airLockId)),
        Message(daveId, "Open the pod bay doors, HAL.",                 airLockConversation plusSeconds 4, Some(airLockId)),
        Message(halId,  "I'm sorry, Dave. I'm afraid I can't do that.", airLockConversation plusSeconds 6, Some(airLockId)))


      // A few messages in the Pod:
      val podConversation = new DateTime(2001, 2, 16, 20, 55, 0)

      messages ++= Seq(
        Message(frankId, "Well, whaddya think?", podConversation, Some(podId)),
        Message(daveId, "I'm not sure, what do you think?", podConversation plusSeconds 4, Some(podId)))
        
      // And private (direct messages)
      messages ++= Seq(
        Message(frankId, "Are you thinking what I'm thinking?", podConversation, Some(podId), toId=Some(daveId)),
        Message(daveId, "Maybe", podConversation plusSeconds 4, Some(podId), toId=Some(frankId)))
    }
  }

  class Schema(val profile: scala.slick.driver.JdbcProfile) extends Tables with Profile
  case class DB(driver: JdbcDriver, url: String, clazz: String)
}