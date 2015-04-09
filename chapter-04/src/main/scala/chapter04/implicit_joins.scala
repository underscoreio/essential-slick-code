package chapter04

import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.DateTimeZone._

object PKs {
  import scala.slick.lifted.MappedTo
  case class UserPK(value: Long) extends AnyVal with MappedTo[Long]
  case class RoomPK(value: Long) extends AnyVal with MappedTo[Long]
  case class OccupantPK(value: Long) extends AnyVal with MappedTo[Long]
  case class MessagePK(value: Long) extends AnyVal with MappedTo[Long]
}

object ImplicitJoinsExample extends App {

  trait Profile {
    val profile: scala.slick.driver.JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import PKs._
    import profile.simple._

    case class User(id: Option[UserPK], name: String, email: Option[String] = None)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id    = column[UserPK]("id", O.AutoInc, O.PrimaryKey)
      def name  = column[String]("name")
      def email = column[Option[String]]("email")

      def *     = (id.?, name, email) <> (User.tupled, User.unapply)
    }

    lazy val users  = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    case class Room(title: String, id: RoomPK = RoomPK(0L))

    class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
      def id    = column[RoomPK]("id", O.PrimaryKey, O.AutoInc)
      def title = column[String]("title")
      def *     = (title, id) <> (Room.tupled, Room.unapply)
    }

    lazy val rooms = TableQuery[RoomTable]
    lazy val insertRoom = rooms returning rooms.map(_.id)

    case class Occupant(roomId: RoomPK, userId: UserPK)

    class OccupantTable(tag: Tag) extends Table[Occupant](tag, "occupant") {
      def roomId = column[RoomPK]("room")
      def userId = column[UserPK]("user")
      def pk     = primaryKey("occ_room_user_pk", (roomId, userId))
      def user   = foreignKey("occ_user_fk", userId, users)(_.id)
      def room   = foreignKey("occ_room_fk", roomId, rooms)(_.id)
      def *      = (roomId, userId) <> (Occupant.tupled, Occupant.unapply)
    }

    lazy val occupants = TableQuery[OccupantTable]

    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime, UTC))

    case class Message(senderId: UserPK,
                       content: String,
                       ts: DateTime,
                       id: MessagePK = MessagePK(0L),
                       toId: Option[UserPK] = None,
                       roomId: Option[RoomPK] = None,
                       readBy: Int)

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id       = column[MessagePK]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[UserPK]("sender")
      def content  = column[String]("content")
      def toId     = column[Option[UserPK]]("to")
      def roomId   = column[Option[RoomPK]]("room")
      def ts       = column[DateTime]("ts")
      def readBy   = column[Int]("readBy")
      def *        = (senderId, content, ts, id, toId, roomId, readBy) <> (Message.tupled, Message.unapply)

      def sender   = foreignKey("msg_sender_fk", senderId, users)(_.id)
      def to       = foreignKey("msg_to_fk", toId, users)(_.id)
      def room     = foreignKey("msg_room_fk", roomId, rooms)(_.id)
    }

    lazy val messages = TableQuery[MessageTable]

  }

  class Schema(val profile: scala.slick.driver.JdbcProfile) extends Tables with Profile

  val schema = new Schema(scala.slick.driver.H2Driver)

  import schema._, profile.simple._, PKs._

  def db = Database.forURL("jdbc:h2:mem:chapter04", driver = "org.h2.Driver")

  db.withSession {
    implicit session =>

      (users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).create
      //(users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).createStatements.foreach(println)

      // A few users:
      val daveId: UserPK  = insertUser += User(None, "Dave", Some("dave@example.org"))
      val halId: UserPK   = insertUser += User(None, "HAL")
      val elenaId: UserPK = insertUser += User(None, "Elena", Some("elena@example.org"))
      val frankId: UserPK = insertUser += User(None, "Frank", Some("frank@example.org"))

      // rooms:
      val airLockId: RoomPK = insertRoom += Room("Air Lock")
      val podId: RoomPK     = insertRoom += Room("Pod")

      // Put Dave in the Room:
      occupants ++= List(Occupant(airLockId, daveId),
        Occupant(airLockId, halId),
        Occupant(podId, daveId),
        Occupant(podId, frankId),
        Occupant(podId, halId))

      // Insert the conversation, which took place in Feb, 2001:
      val airLockConversation = new DateTime(2001, 2, 17, 10, 22, 50)

      //add some messages to the room.
      messages ++= Seq(
        Message(daveId, "Hello, HAL. Do you read me, HAL?", airLockConversation, MessagePK(0L), None, Some(airLockId), 1),
        Message(halId, "Affirmative, Dave. I read you.", airLockConversation plusSeconds 2, MessagePK(0L), None, Some(airLockId), 1),
        Message(daveId, "Open the pod bay doors, HAL.", airLockConversation plusSeconds 4, MessagePK(0L), None, Some(airLockId), 1),
        Message(halId, "I'm sorry, Dave. I'm afraid I can't do that.", airLockConversation plusSeconds 6, MessagePK(0L), None, Some(airLockId), 1))

      val podConversation = new DateTime(2001, 2, 16, 20, 55, 0)

      messages ++= Seq(
        Message(frankId, "Well, whaddya think?", podConversation, MessagePK(0L), None, Some(podId), 2),
        Message(daveId, "I'm not sure, what do you think?", podConversation plusSeconds 4, MessagePK(0L), None, Some(podId), 2))

      //implicit join
      val davesMessages = for {
        message <- messages
        user    <- message.sender
        room    <- message.room
        if user.id       === daveId &&
          room.id        === airLockId &&
          message.roomId === room.id
      } yield message

      val altDavesMessages = for {
        message <- messages
        if message.senderId === daveId &&
          message.roomId    === airLockId
      } yield message

      //explicit join
      lazy val left = messages.
        leftJoin(users).
        leftJoin(rooms).
        on { case ((m, u), r) => m.senderId === u.id && m.roomId === r.id }.
        filter { case ((m, u), r) => u.id === daveId && r.id === airLockId }.
        map { case ((m, u), r) => m }

      lazy val right = for {
        ((msgs, usrs), rms) <- messages rightJoin users on (_.senderId === _.id) rightJoin rooms on (_._1.roomId === _.id)
        if usrs.id === daveId && rms.id === airLockId && rms.id === msgs.roomId
      } yield msgs

      lazy val inner = for {
        ((msgs, usrs), rms) <- messages innerJoin users on (_.senderId === _.id) leftJoin rooms on (_._1.roomId === _.id)
        if usrs.id === daveId && rms.id === airLockId && rms.id.? === msgs.roomId
      } yield msgs

      /* H2 doesn't support FULL OUTER JOINS at the time of writing.
      lazy val outer = for {
        (msg, usr) <- messages outerJoin users on (_.senderId.? === _.id.?)
      } yield msg -> usr
      */

      lazy val y = for {
        (m1, u) <- messages leftJoin users on (_.senderId === _.id)
        (m2, r) <- messages leftJoin rooms on (_.roomId === _.id)
        if m1.id === m2.id && u.id === daveId && r.id === airLockId && r.id.? === m1.roomId
      } yield m1

      lazy val z = messages.
        leftJoin(users).
        leftJoin(rooms).
        on { case ((m, u), r) => m.senderId === u.id && m.roomId === r.id }.
        filter { case ((m, u), r) => u.id === daveId && r.id === airLockId }.
        map { case ((m, u), r) => m }

      //      lazy val left = for {
      //        (usrs, occ) <- users leftJoin occupants on (_.id === _.userId)
      //      } yield usrs.name -> occ.roomId.?
      //
      //      lazy val right = for {
      //        (usrs, occ) <- users rightJoin occupants on (_.id === _.userId)
      //      } yield usrs.name -> occ.roomId

      lazy val userRooms = for {
        ((u, o), r) <- users.
          rightJoin(occupants).
          rightJoin(rooms).
          on { case ((u, o), r) => u.id === o.userId && r.id === o.roomId }
      } yield (u.name, r.title)

      lazy val firstAndLastMessage = messages.filter(_.senderId === daveId).groupBy { _ => true }.map {
        case (_, group) => (group.map(_.id).max, group.map(_.id).min)
      }

      //http://stackoverflow.com/questions/27049646/how-to-select-max-min-in-same-query-in-slick/27055250#27055250
      //println(firstAndLastMessage.selectStatement)
      //println(firstAndLastMessage.list.mkString("\n","\n","\n"))

      //println(userRooms.selectStatement)

      //println(userRooms.list.mkString("\n","\n","\n"))

      //      println(davesMessages.list.mkString("\n", "\n", "\n"))
      //      println(left.selectStatement)
      //      println(left.list.mkString("\n", "\n", "\n"))
      //      println(right.selectStatement)
      //      println(right.list.mkString("\n", "\n", "\n"))
      //      println(inner.selectStatement)
      //      println(inner.list.mkString("\n", "\n", "\n"))

  }

}