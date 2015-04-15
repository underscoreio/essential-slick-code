package chapter04

import java.sql.Timestamp
import scala.slick.lifted.MappedTo
import org.joda.time.DateTime
import org.joda.time.DateTimeZone._
import scala.slick.backend._
import scala.slick.driver.JdbcDriver

final case class PK[A](value: Long) extends AnyVal with MappedTo[Long]

object DifferentDatabases extends App {

  trait Profile {
    val profile: scala.slick.driver.JdbcProfile
  }

  trait Tables {
    this: Profile ⇒

    import profile.simple._

    case class User(id: Option[PK[UserTable]], name: String, email: Option[String] = None)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id = column[PK[UserTable]]("id", O.AutoInc, O.PrimaryKey)
      def name = column[String]("name")
      def email = column[Option[String]]("email")

      def * = (id.?, name, email) <> (User.tupled, User.unapply)
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    case class Room(title: String, id: PK[RoomTable] = PK[RoomTable](0L))

    class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
      def id = column[PK[RoomTable]]("id", O.PrimaryKey, O.AutoInc)
      def title = column[String]("title")
      def * = (title, id) <> (Room.tupled, Room.unapply)
    }

    lazy val rooms = TableQuery[RoomTable]
    lazy val insertRoom = rooms returning rooms.map(_.id)

    case class Occupant(roomPK: PK[RoomTable], userPK: PK[UserTable])

    class OccupantTable(tag: Tag) extends Table[Occupant](tag, "occupant") {
      def roomPK = column[PK[RoomTable]]("room")
      def userPK = column[PK[UserTable]]("user")
      def pk = primaryKey("occ_room_user_pk", (roomPK, userPK))
      def user = foreignKey("occ_user_fk", userPK, users)(_.id)
      def room = foreignKey("occ_room_fk", roomPK, rooms)(_.id)
      def * = (roomPK, userPK) <> (Occupant.tupled, Occupant.unapply)
    }

    lazy val occupants = TableQuery[OccupantTable]

    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt ⇒ new Timestamp(dt.getMillis),
        ts ⇒ new DateTime(ts.getTime, UTC))

    case class Message(senderPK: PK[UserTable],
                       content: String,
                       ts: DateTime,
                       id: PK[MessageTable] = PK(0L),
                       toPK: Option[PK[UserTable]] = None,
                       roomPK: Option[PK[RoomTable]] = None,
                       readBy: Int)

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id = column[PK[MessageTable]]("id", O.PrimaryKey, O.AutoInc)
      def senderPK = column[PK[UserTable]]("sender")
      def content = column[String]("content")
      def toPK = column[Option[PK[UserTable]]]("to")
      def roomPK = column[Option[PK[RoomTable]]]("room")
      def ts = column[DateTime]("ts")
      def readBy = column[Int]("readBy")
      def * = (senderPK, content, ts, id, toPK, roomPK, readBy) <> (Message.tupled, Message.unapply)

      def sender = foreignKey("msg_sender_fk", senderPK, users)(_.id)
      def to = foreignKey("msg_to_fk", toPK, users)(_.id)
      def room = foreignKey("msg_room_fk", roomPK, rooms)(_.id)
    }

    lazy val messages = TableQuery[MessageTable]

  }

  class Schema(val profile: scala.slick.driver.JdbcProfile) extends Tables with Profile

  case class DB(driver: JdbcDriver, url: String, clazz: String)

  def printStatements: DB ⇒ Unit = { info ⇒
    val schema = new Schema(info.driver)

    import schema._, profile.simple._

    def db = Database.forURL(info.url, driver = info.clazz)

    db.withSession {
      implicit session ⇒

        lazy val left = messages.
          leftJoin(users).on(_.senderPK === _.id).
          leftJoin(rooms).on { case ((m, u), r) ⇒ m.roomPK === r.id }.
          filter { case ((m, u), r) ⇒ u.id === PK[UserTable](1L) && r.id === PK[RoomTable](1L) }.
          map { case ((m, u), r) ⇒ m }

        lazy val right = for {
          ((msgs, usrs), rms) ← messages rightJoin users on (_.senderPK === _.id) rightJoin rooms on { case ((m, u), r) ⇒ m.roomPK === r.id }
          if usrs.id === PK[UserTable](1) && rms.id === PK[RoomTable](1L) && rms.id === msgs.roomPK
        } yield msgs

        lazy val inner = for {
          ((msgs, usrs), rms) ← messages innerJoin users on (_.senderPK === _.id) innerJoin rooms on (_._1.roomPK === _.id)
          if usrs.id === PK[UserTable](1) && rms.id === PK[RoomTable](1L) && rms.id.? === msgs.roomPK
        } yield msgs

        List(left, right, inner).foreach { q ⇒ println(q.selectStatement) }

    }

  }

  val h2 = DB(scala.slick.driver.H2Driver, "jdbc:h2:mem:chapter04", "org.h2.Driver")
  val ms = DB(scala.slick.driver.MySQLDriver, "jdbc:mysql:database", "com.mysql.jdbc.Driver")
  val pg = DB(scala.slick.driver.PostgresDriver, "jdbc:postgresql:database", "org.postgresql.Driver")

  List(h2, ms, pg).foreach { db ⇒
    println("============================================")
    printStatements(db)
    println("============================================")
  }

}