import slick.jdbc.JdbcProfile
import slick.lifted.MappedTo

import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC

import scala.concurrent.ExecutionContext.Implicits.global

object ChatSchema {

  case class Id[A](value: Long) extends AnyVal with MappedTo[Long]

  trait Profile {
    val profile: slick.jdbc.JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.api._

    case class User(id: Id[UserTable] = Id(0),name: String, email: Option[String] = None)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id    = column[Id[UserTable]]("id", O.AutoInc, O.PrimaryKey)
      def name  = column[String]("name")
      def email = column[Option[String]]("email")

      def * = (id,name, email).mapTo[User]
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    case class Room(title: String, id: Id[RoomTable] = Id(0L))

    class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
      def id    = column[Id[RoomTable]]("id", O.PrimaryKey, O.AutoInc)
      def title = column[String]("title")
      def * = (title, id).mapTo[Room]
    }

    lazy val rooms = TableQuery[RoomTable]
    lazy val insertRoom = rooms returning rooms.map(_.id)

    case class Occupant(roomId: Id[RoomTable], userId: Id[UserTable])

    class OccupantTable(tag: Tag) extends Table[Occupant](tag, "occupant") {
      def roomId = column[Id[RoomTable]]("room")
      def userId = column[Id[UserTable]]("user")
      def pk   = primaryKey("occ_room_user_pk", (roomId, userId))
      def user = foreignKey("occ_user_fk", userId, users)(_.id)
      def room = foreignKey("occ_room_fk", roomId, rooms)(_.id)
      def * = (roomId, userId).mapTo[Occupant]
    }

    lazy val occupants = TableQuery[OccupantTable]

    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime, UTC))

    case class Message(senderId: Id[UserTable],
                       content: String,
                       ts: DateTime,
                       roomId: Option[Id[RoomTable]] = None,
                       toId: Option[Id[UserTable]]   = None,
                       id: Id[MessageTable]          = Id(0L) )

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id       = column[Id[MessageTable]]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[Id[UserTable]]("sender")
      def content  = column[String]("content")
      def ts       = column[DateTime]("ts")
      def roomId   = column[Option[Id[RoomTable]]]("room") // in a particular room? or broadcast?
      def toId     = column[Option[Id[UserTable]]]("to")   // direct message?
      def * = (senderId, content, ts, roomId, toId, id).mapTo[Message]

      def sender = foreignKey("msg_sender_fk", senderId, users)(_.id)
      def to     = foreignKey("msg_to_fk", toId, users)(_.id.?)
      def room   = foreignKey("msg_room_fk", roomId, rooms)(_.id.?)
    }

    lazy val messages = TableQuery[MessageTable]

    def schema =
      (users.schema ++ rooms.schema ++ occupants.schema ++ messages.schema)

    // Sample data set
    def populate = for {

      _                      <- schema.create

      // A few users:
      daveId:  Id[UserTable] <- insertUser += User(name = "Dave", email = Some("dave@example.org"))
      halId:   Id[UserTable] <- insertUser += User(name = "HAL")
      elenaId: Id[UserTable] <- insertUser += User(name = "Elena",email = Some("elena@example.org"))
      frankId: Id[UserTable] <- insertUser += User(name = "Frank",email = Some("frank@example.org"))

      // rooms:
      airLockId: Id[RoomTable] <- insertRoom += Room("Air Lock")
      podId:     Id[RoomTable] <- insertRoom += Room("Pod")
      brainId:   Id[RoomTable] <- insertRoom += Room("Brain Room")

      // Put people in the rooms:
      _  <- occupants ++= List(
        Occupant(airLockId, daveId),
        Occupant(airLockId, halId),
        Occupant(podId, daveId),
        Occupant(podId, frankId),
        Occupant(podId, halId) )

      // Insert the conversation, which took place in Feb, 2001:
      airLockConversation = new DateTime(2001, 2, 17, 10, 22, 50)

      // Add some messages to the air lock room:
      _ <- messages ++= Seq(
        Message(daveId, "Hello, HAL. Do you read me, HAL?",             airLockConversation,               Some(airLockId)),
        Message(halId,  "Affirmative, Dave. I read you.",               airLockConversation plusSeconds 2, Some(airLockId)),
        Message(daveId, "Open the pod bay doors, HAL.",                 airLockConversation plusSeconds 4, Some(airLockId)),
        Message(halId,  "I'm sorry, Dave. I'm afraid I can't do that.", airLockConversation plusSeconds 6, Some(airLockId)))


      // A few messages in the Pod:
      podConversation = new DateTime(2001, 2, 16, 20, 55, 0)

      _ <- messages ++= Seq(
        Message(frankId, "Well, whaddya think?", podConversation, Some(podId)),
        Message(daveId, "I'm not sure, what do you think?", podConversation plusSeconds 4, Some(podId)))

      // And private (direct messages)
      _ <- messages ++= Seq(
        Message(frankId, "Are you thinking what I'm thinking?", podConversation plusSeconds 6, Some(podId), toId=Some(daveId)),
        Message(daveId, "Maybe", podConversation plusSeconds 8, Some(podId), toId=Some(frankId)))

    } yield ()

  }

  class Schema(val profile: JdbcProfile) extends Tables with Profile
  case class DB(profile: JdbcProfile, url: String, clazz: String)
}
