package chapter04

import java.sql.Timestamp

import scala.slick.lifted.MappedTo

import org.joda.time.DateTime
import org.joda.time.DateTimeZone._

final case class Id[A](value:Long) extends AnyVal with MappedTo[Long]

object ImplicitJoinsExample extends App {

  trait Profile {
    val profile: scala.slick.driver.JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.simple._

    case class User(id: Option[Id[UserTable]], name: String, email: Option[String] = None)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id    = column[Id[UserTable]]("id", O.AutoInc, O.PrimaryKey)
      def name  = column[String]("name")
      def email = column[Option[String]]("email")

      def *     = (id.?, name, email) <> (User.tupled, User.unapply)
    }

    lazy val users  = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    case class Room(title: String, id: Id[RoomTable] = Id[RoomTable](0L))

    class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
      def id    = column[Id[RoomTable]]("id", O.PrimaryKey, O.AutoInc)
      def title = column[String]("title")
      def *     = (title, id) <> (Room.tupled, Room.unapply)
    }

    lazy val rooms = TableQuery[RoomTable]
    lazy val insertRoom = rooms returning rooms.map(_.id)

    case class Occupant(roomId: Id[RoomTable], userId: Id[UserTable])

    class OccupantTable(tag: Tag) extends Table[Occupant](tag, "occupant") {
      def roomId = column[Id[RoomTable]]("room")
      def userId = column[Id[UserTable]]("user")
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

    case class Message(senderId: Id[UserTable],
                       content: String,
                       ts: DateTime,
                       id: Id[MessageTable] = Id(0L),
                       toId: Option[Id[UserTable]] = None,
                       roomId: Option[Id[RoomTable]] = None,
                       readBy: Int)

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id       = column[Id[MessageTable]]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[Id[UserTable]]("sender")
      def content  = column[String]("content")
      def toId     = column[Option[Id[UserTable]]]("to")
      def roomId   = column[Option[Id[RoomTable]]]("room")
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

  import schema._, profile.simple._

  def db = Database.forURL("jdbc:h2:mem:chapter04", driver = "org.h2.Driver")

  db.withSession {
    implicit session =>

      (users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).create
      //(users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).createStatements.foreach(println)

      // A few users:
      val daveId:  Id[UserTable] = insertUser += User(None, "Dave", Some("dave@example.org"))
      val halId:   Id[UserTable] = insertUser += User(None, "HAL")
      val elenaId: Id[UserTable] = insertUser += User(None, "Elena", Some("elena@example.org"))
      val frankId: Id[UserTable] = insertUser += User(None, "Frank", Some("frank@example.org"))

      // rooms:
      val airLockId: Id[RoomTable] = insertRoom += Room("Air Lock")
      val podId:     Id[RoomTable] = insertRoom += Room("Pod")

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
        Message(daveId, "Hello, HAL. Do you read me, HAL?", airLockConversation, Id(0L), None, Some(airLockId), 1),
        Message(halId, "Affirmative, Dave. I read you.", airLockConversation plusSeconds 2, Id(0L), None, Some(airLockId), 1),
        Message(daveId, "Open the pod bay doors, HAL.", airLockConversation plusSeconds 4, Id(0L), None, Some(airLockId), 1),
        Message(halId, "I'm sorry, Dave. I'm afraid I can't do that.", airLockConversation plusSeconds 6, Id(0L), None, Some(airLockId), 1))

      val podConversation = new DateTime(2001, 2, 16, 20, 55, 0)

      messages ++= Seq(
        Message(frankId, "Well, whaddya think?", podConversation, Id(0L), None, Some(podId), 2),
        Message(daveId, "I'm not sure, what do you think?", podConversation plusSeconds 4, Id(0L), None, Some(podId), 2))

      /*
      val rubbishDoesntCompile = for {
        message <- messages
        user    <- message.sender
        room    <- message.room
        if user.id       === airLockId &&
          room.id        === daveId &&
          message.roomId === room.id
      } yield message

[info] Compiling 1 Scala source to /Users/jonoabroad/developer/books/essential-slick-code/chapter-04/target/scala-2.11/classes...
/Users/jonoabroad/developer/books/essential-slick-code/chapter-04/src/main/scala/chapter04/implicit_joins.scala:146: Error typechecking MappedTo expansion: class type required but chapter04.Id[_ >: chapter04.ImplicitJoinsExample.schema.RoomTable with chapter04.ImplicitJoinsExample.schema.UserTable <: chapter04.ImplicitJoinsExample.schema.profile.Table[_ >: chapter04.ImplicitJoinsExample.schema.Room with chapter04.ImplicitJoinsExample.schema.User <: Product with Serializable]] found
[error] /Users/jonoabroad/developer/books/essential-slick-code/chapter-04/src/main/scala/chapter04/implicit_joins.scala:146: Cannot perform option-mapped operation
[error]       with type: (chapter04.Id[chapter04.ImplicitJoinsExample.schema.UserTable], chapter04.Id[chapter04.ImplicitJoinsExample.schema.RoomTable]) => R
[error]   for base type: (chapter04.Id[chapter04.ImplicitJoinsExample.schema.UserTable], chapter04.Id[chapter04.ImplicitJoinsExample.schema.UserTable]) => Boolean
[error]         if user.id       === airLockId &&
[error]                          ^
[error] /Users/jonoabroad/developer/books/essential-slick-code/chapter-04/src/main/scala/chapter04/implicit_joins.scala:143: type mismatch;

      val rubbish = Id(1234)

      val rubbishDoesntCompile = for {
        message <- messages
        user    <- message.sender
        room    <- message.room
        if user.id       === rubbish &&
          room.id        === airLockId &&
          message.roomId === room.id
      } yield message


      */

      //implicit join
      val q = for {
        msg <- messages
        usr <- msg.sender
      } yield (usr.name, msg.content)

      val q1 = for {
        msg <- messages
        usr <- users
        if usr.id === msg.senderId
      } yield (usr.name, msg.content)


      val davesMessagesUsingFK = for {
        message <- messages
        user    <- message.sender
        room    <- message.room
        if user.id       === daveId &&
          room.id        === airLockId &&
          message.roomId === room.id
      } yield message


      val davesMessages = for {
            message <- messages
            user    <- users
            room    <- rooms
            if message.senderId === user.id &&
               message.roomId   === room.id &&
               user.id          === daveId  &&
               room.id          === airLockId
         } yield message

      //explicit join
      lazy val leftJoinJoin = messages.
        leftJoin(users).
        leftJoin(rooms).
        on { case ((m, u), r) => m.senderId === u.id && m.roomId === r.id }.
        filter { case ((m, u), r) => u.id === daveId && r.id === airLockId }.
        map { case ((m, u), r) => m }

      lazy val left = messages.
        leftJoin(users).on(_.senderId === _.id).
        leftJoin(rooms).on{ case ((m,u),r) => m.roomId === r.id}.
        filter { case ((m, u), r) => u.id === daveId && r.id === airLockId }.
        map { case ((m, u), r) => m }

      lazy val right = for {
        ((msgs, usrs), rms) <- messages rightJoin users on (_.senderId === _.id) rightJoin rooms on { case ((m,u),r) =>  m.roomId === r.id}
        if usrs.id === daveId && rms.id === airLockId && rms.id === msgs.roomId
      } yield msgs

      lazy val inner = for {
        ((msgs, usrs), rms) <- messages innerJoin users on (_.senderId === _.id) innerJoin rooms on (_._1.roomId === _.id)
        if usrs.id === daveId && rms.id === airLockId && rms.id.? === msgs.roomId
      } yield msgs

      /* H2 doesn't support FULL OUTER JOINS at the time of writing.
      lazy val outer = for {
        (msg, usr) <- messages outerJoin users on (_.senderId.? === _.id.?)
      } yield msg -> usr
      */


      lazy val userRooms = for {
        ((u, o), r) <- users.
          rightJoin(occupants).
          rightJoin(rooms).
          on { case ((u, o), r) => u.id === o.userId && r.id === o.roomId }
      } yield (u.name, r.title)

      lazy val firstAndLastMessage = messages.filter(_.senderId === daveId).groupBy { _ => true }.map {
        case (_, group) => (group.map(_.id).max, group.map(_.id).min)
      }

      val zip = for {
       (u,r) <-  users zip rooms
     } yield u.name -> r.title


     List(left,right,inner).foreach{ q =>
       println(q.selectStatement)
       println(q.list.mkString("\n","\n","\n"))
     }


  }

}