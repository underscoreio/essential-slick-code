package chapter04

import java.sql.Timestamp
import scala.slick.lifted.MappedTo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone._
import scala.slick.backend._
import scala.slick.driver.JdbcDriver

final case class PI[A](value: Long) extends AnyVal with MappedTo[Long]

object PlainQueries extends App {

  trait Profile {
    val profile: scala.slick.driver.JdbcProfile
  }

  trait Tables {
    this: Profile ⇒

    import profile.simple._

    case class User(id: Option[PI[UserTable]], name: String, email: Option[String] = None)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id = column[PI[UserTable]]("id", O.AutoInc, O.PrimaryKey)
      def name = column[String]("name")
      def email = column[Option[String]]("email")

      def * = (id.?, name, email) <> (User.tupled, User.unapply)
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    case class Room(title: String, id: PI[RoomTable] = PI[RoomTable](0L))

    class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
      def id = column[PI[RoomTable]]("id", O.PrimaryKey, O.AutoInc)
      def title = column[String]("title")
      def * = (title, id) <> (Room.tupled, Room.unapply)
    }

    lazy val rooms = TableQuery[RoomTable]
    lazy val insertRoom = rooms returning rooms.map(_.id)

    case class Occupant(roomPI: PI[RoomTable], userPI: PI[UserTable])

    class OccupantTable(tag: Tag) extends Table[Occupant](tag, "occupant") {
      def roomPI = column[PI[RoomTable]]("room")
      def userPI = column[PI[UserTable]]("user")
      def pk = primaryKey("occ_room_user_pk", (roomPI, userPI))
      def user = foreignKey("occ_user_fk", userPI, users)(_.id)
      def room = foreignKey("occ_room_fk", roomPI, rooms)(_.id)
      def * = (roomPI, userPI) <> (Occupant.tupled, Occupant.unapply)
    }

    lazy val occupants = TableQuery[OccupantTable]

    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt ⇒ new Timestamp(dt.getMillis),
        ts ⇒ new DateTime(ts.getTime, UTC))

    case class Message(senderPI: PI[UserTable],
                       content: String,
                       ts: DateTime,
                       id: PI[MessageTable] = PI(0L),
                       toPI: Option[PI[UserTable]] = None,
                       roomPI: Option[PI[RoomTable]] = None,
                       readBy: Int)

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id = column[PI[MessageTable]]("id", O.PrimaryKey, O.AutoInc)
      def senderPI = column[PI[UserTable]]("sender")
      def content = column[String]("content")
      def toPI = column[Option[PI[UserTable]]]("to")
      def roomPI = column[Option[PI[RoomTable]]]("room")
      def ts = column[DateTime]("ts")
      def readBy = column[Int]("readBy")
      def * = (senderPI, content, ts, id, toPI, roomPI, readBy) <> (Message.tupled, Message.unapply)

      def sender = foreignKey("msg_sender_fk", senderPI, users)(_.id)
      def to = foreignKey("msg_to_fk", toPI, users)(_.id)
      def room = foreignKey("msg_room_fk", roomPI, rooms)(_.id)
    }

    lazy val messages = TableQuery[MessageTable]

  }

  class Schema(val profile: scala.slick.driver.JdbcProfile) extends Tables with Profile

  case class DB(driver: JdbcDriver, url: String, clazz: String)

  def printStatements: DB ⇒ Unit = { info ⇒
    val schema = new Schema(info.driver)

    import schema._, profile.simple._

    def db = Database.forURL(info.url, driver = info.clazz)

    db withSession {
      implicit session ⇒

        (users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).create
        (users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).createStatements.foreach { println }

    }

    import scala.slick.driver.JdbcDriver.backend.{ Database ⇒ DDB }
    import Database.dynamicSession
    import scala.slick.jdbc.{ GetResult, StaticQuery ⇒ Q }
    println("============================================")
    println("============= Plain SQL ====================")
    println("============================================")
    DDB.forURL(info.url, driver = info.clazz) withDynSession {
      import Q.interpolation

      val dave = User(None, "Dave", Some("dave@example.org"))
      val hal = User(None, "HAL")
      val elena = User(None, "Elena", Some("elena@example.org"))
      val frank = User(None, "Frank", Some("frank@example.org"))

      val airLock = Room("Air Lock")
      val pod = Room("Pod")

      //Can we pass kind of PI into an implicit?
      implicit val getUserIdResult = GetResult(r ⇒ PI[UserTable](r.nextLong()))
      implicit val getRoomIdResult = GetResult(r ⇒ PI[RoomTable](r.nextLong()))

      implicit val getMessageIdResult = GetResult(r ⇒ PI[MessageTable](r.nextLong()))
      implicit val getDateTime = GetResult(r ⇒ new DateTime(r.nextTimestamp(), UTC))

      implicit val getOptionalUserIdResult: GetResult[Option[PI[UserTable]]] = GetResult(r ⇒ r.nextLongOption().map(i ⇒ PI[UserTable](i)))
      implicit val getOptionalRoomIdResult: GetResult[Option[PI[RoomTable]]] = GetResult(r ⇒ r.nextLongOption().map(i ⇒ PI[RoomTable](i)))

      implicit val getMessage = GetResult(r ⇒ Message(senderPI = r.<<,
                                                      content   = r.<<,
                                                      ts        = r.<<,
                                                      id        = r.<<,
                                                      toPI      = r.<<?,
                                                      roomPI    = r.<<?,
                                                      readBy    = r.<<))

      def insertU(u: User) = sqlu""" insert into "user" values (${u.id}, ${u.name}, ${u.email})""".first
      def insertR(r: Room) = (Q.u + "insert into \"room\" values (" +? r.title +? ")").execute

      //Yes yes it's evil.
      def idU(u: User): PI[UserTable] = sql"""select id from "user" where email = ${u.email}""".as[PI[UserTable]].first
      def idR(r: Room): PI[RoomTable] = sql"""select id from "room" where title = ${r.title}""".as[PI[RoomTable]].first

      insertU(dave)
      insertU(hal)
      insertU(elena)
      insertU(frank)

      val davePI: PI[UserTable] = idU(dave)
      val halPI: PI[UserTable] = idU(hal)
      val elenaPI: PI[UserTable] = idU(elena)
      val frankPI: PI[UserTable] = idU(frank)

      insertR(airLock)
      insertR(pod)

      val airLockPI: PI[RoomTable] = idR(airLock)
      val podPI: PI[RoomTable] = idR(pod)

      // Populate Rooms
      occupants ++= List(Occupant(airLockPI, davePI),
        Occupant(airLockPI, halPI),
        Occupant(podPI, davePI),
        Occupant(podPI, frankPI),
        Occupant(podPI, halPI))

      // Insert the conversation, which took place in Feb, 2001:
      val airLockConversation = new DateTime(2001, 2, 17, 10, 22, 50)

      //add some messages to the room.
      messages ++= Seq(
        Message(davePI, "Hello, HAL. Do you read me, HAL?", airLockConversation, PI(0L), None, Some(airLockPI), 1),
        Message(halPI, "Affirmative, Dave. I read you.", airLockConversation plusSeconds 2, PI(0L), None, Some(airLockPI), 1),
        Message(davePI, "Open the pod bay doors, HAL.", airLockConversation plusSeconds 4, PI(0L), None, Some(airLockPI), 1),
        Message(halPI, "I'm sorry, Dave. I'm afraid I can't do that.", airLockConversation plusSeconds 6, PI(0L), None, Some(airLockPI), 1))

      val podConversation = new DateTime(2001, 2, 16, 20, 55, 0)

      messages ++= Seq(
        Message(frankPI, "Well, whaddya think?", podConversation, PI(0L), None, Some(podPI), 2),
        Message(davePI, "I'm not sure, what do you think?", podConversation plusSeconds 4, PI(0L), None, Some(podPI), 2))

      val davesAirLockMessages = sql"""select *
              from "message" left outer join "user" on "message"."sender" = "user"."id"
                             left outer join "room" on "message"."room"   = "room"."id"
              where "user"."id" = ${davePI} and "room"."id" = ${airLockPI}"""

      davesAirLockMessages.as[Message].list.foreach { println }

    }

  }

  val h2 = DB(scala.slick.driver.H2Driver, s"jdbc:h2:./chapter04_${System.currentTimeMillis()}", "org.h2.Driver")

  List(h2).foreach { db ⇒
    println("============================================")
    println("=============== Populate ===================")
    println("============================================")
    printStatements(db)
    println("============================================")
  }

}