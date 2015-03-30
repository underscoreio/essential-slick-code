package chapter04

import  java.sql.Timestamp
import  org.joda.time.DateTime
import  org.joda.time.DateTimeZone._

object ImplicitJoinsExample extends App {

  trait Profile {
    val profile: scala.slick.driver.JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.simple._

    case class User(id: Option[Long], name: String, email: Option[String] = None)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id    = column[Long]("id", O.AutoInc, O.PrimaryKey)
      def name  = column[String]("name")
      def email = column[Option[String]]("email")

      def * = (id.?, name, email) <> (User.tupled, User.unapply)
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    case class Room(title: String, id: Long = 0L)

    class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
     def id    = column[Long]("id", O.PrimaryKey, O.AutoInc)
     def title = column[String]("title")
     def * = (title, id) <> (Room.tupled, Room.unapply)
    }

    lazy val rooms = TableQuery[RoomTable]
    lazy val insertRoom = rooms returning rooms.map(_.id)

    case class Occupant(roomId: Long, userId: Long)

    class OccupantTable(tag: Tag) extends Table[Occupant](tag, "occupant") {
      def roomId = column[Long]("room")
      def userId = column[Long]("user")

      def pk = primaryKey("occ_room_user_pk", (roomId, userId))

      def user = foreignKey("occ_user_fk", userId, users)(_.id)

      def room = foreignKey("occ_room_fk", roomId, rooms)(_.id)

      def * = (roomId, userId) <> (Occupant.tupled, Occupant.unapply)
    }

    lazy val occupants = TableQuery[OccupantTable]

    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime, UTC)
    )

    case class Message(senderId: Long, content: String, ts: DateTime, id: Long = 0L, toId:Option[Long], roomId:Option[Long])

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id       = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[Long]("sender")
      def content  = column[String]("content")
      def toId     = column[Option[Long]]("to")
      def roomId   = column[Option[Long]]("room")
      def ts       = column[DateTime]("ts")

      def * = (senderId, content, ts, id, toId,roomId) <> (Message.tupled, Message.unapply)

      def sender = foreignKey("msg_sender_fk", senderId, users)(_.id)
      def to     = foreignKey("msg_to_fk",     toId,     users)(_.id)
      def room   = foreignKey("msg_room_fk",   roomId,   rooms)(_.id)

    }

    lazy val messages = TableQuery[MessageTable]

  }

  class Schema(val profile: scala.slick.driver.JdbcProfile) extends Tables with Profile

  val schema = new Schema(scala.slick.driver.H2Driver)

  import schema._, profile.simple._

  def db = Database.forURL("jdbc:h2:mem:chapter04", driver = "org.h2.Driver")

  db.withSession {
    implicit session =>

      (users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).create

      // A few users:
      val daveId: Long = insertUser += User(None, "Dave", Some("dave@example.org"))
      val halId:  Long = insertUser += User(None, "HAL")
      val elena:  Long = insertUser += User(None, "Elena", Some("elena@example.org"))

      println( users.list )

      // A room:
      val airLockId: Long = insertRoom += Room("Air Lock")

      println(rooms.list)

      // Put Dave in the Room:
      occupants ++= List(Occupant(airLockId,daveId), Occupant(airLockId,halId))

      // Insert the conversation, which took place in Feb, 2001:
      val start = new DateTime(2001, 2, 17, 10, 22, 50)

      //add some messages to the room.
      messages ++= Seq(
        Message(daveId, "Hello, HAL. Do you read me, HAL?", start,0,None,Some(airLockId)),
        Message(halId,  "Affirmative, Dave. I read you.", start plusSeconds 2,0,None,Some(airLockId)),
        Message(daveId, "Open the pod bay doors, HAL.", start plusSeconds 4,0,None,Some(airLockId)),
        Message(halId,  "I'm sorry, Dave. I'm afraid I can't do that.", start plusSeconds 6,0,None,Some(airLockId)))


      //implicit join
      val davesMessages = for {
        message <- messages
        user  <- message.sender
        room <- message.room
        if user.id === daveId && room.id === airLockId && message.roomId === room.id
      } yield message

      println("Dave's Messages")
      println(davesMessages.list.mkString("\n"))
  }

}