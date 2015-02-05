package chapter03

import java.sql.Timestamp

import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC

import scala.slick.driver.H2Driver.simple._
import scala.slick.collection.heterogenous.{ HList, HCons, HNil, Nat }
import scala.slick.collection.heterogenous.syntax._

object Example extends App {

  // Custom column mapping:
  implicit val jodaDateTimeType =
    MappedColumnType.base[DateTime, Timestamp](
      dt ⇒ new Timestamp(dt.getMillis),
      ts ⇒ new DateTime(ts.getTime, UTC))

  // Row representation:
  final case class Message(sender: Long,
                           content: String,
                           ts: DateTime,
                           to: Option[Long] = None,
                           id: Long = 0L)

  // Schema:
  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender = column[Long]("sender")
    def to = column[Option[Long]]("to")
    def content = column[String]("content")
    def ts = column[DateTime]("ts")
    def * = (sender, content, ts, to, id) <> (Message.tupled, Message.unapply)
  }

  // Table:
  lazy val messages = TableQuery[MessageTable]

  final case class User(name: String, id: Long = 0L)

  final class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("sender")
    def * = (name, id) <> (User.tupled, User.unapply)
  }

  lazy val users = TableQuery[UserTable]

  // Database connection details:
  def db = Database.forURL("jdbc:h2:mem:chapter02", driver = "org.h2.Driver")

  // Query execution:
  db.withSession {
    implicit session ⇒
      // Create the tables:
      val ddl = messages.ddl ++ users.ddl

      ddl.create

      // Insert the conversation, which took place in Feb, 2001:
      val start = new DateTime(2001, 2, 17, 10, 22, 50)

      users ++= Seq(User("Dave"), User("Hal"))

      val oDave = users.filter(_.name === "Dave").firstOption
      val oHAL = users.filter(_.name === "Hal").firstOption

      for {
        dave ← oDave
        hal ← oHAL
      } {
        messages ++= Seq(
          Message(dave.id, "Hello, HAL. Do you read me, HAL?", start),
          Message(hal.id, "Affirmative, Dave. I read you.", start plusSeconds 2),
          Message(dave.id, "Open the pod bay doors, HAL.", start plusSeconds 4),
          Message(hal.id, "I'm sorry, Dave. I'm afraid I can't do that.", start plusSeconds 6))
      }
      users.iterator.foreach(println)
      messages.iterator.foreach(println)
  }

}
object HListExample /* extends App */ {

  // Custom column mapping:
  implicit val jodaDateTimeType =
    MappedColumnType.base[DateTime, Timestamp](
      dt ⇒ new Timestamp(dt.getMillis),
      ts ⇒ new DateTime(ts.getTime, UTC))

  // Row representation:
  final case class Message(sender: Long, content: String, ts: DateTime, id: Long = 0L)

  // Schema:
  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender = column[Long]("sender")
    def content = column[String]("content")
    def ts = column[DateTime]("ts")
    def * = (sender, content, ts, id) <> (Message.tupled, Message.unapply)
  }

  type User = String :: Long :: HNil

  final class UserTable(tag: Tag) extends Table[User](tag, "user") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("sender")
    def * = name :: id :: HNil
  }

  // Table:
  lazy val messages = TableQuery[MessageTable]
  lazy val users = TableQuery[UserTable]

  // Database connection details:
  def db = Database.forURL("jdbc:h2:mem:chapter02", driver = "org.h2.Driver")

  // Query execution:
  db.withSession {
    implicit session ⇒

      // Create the tables:
      val ddl = messages.ddl ++ users.ddl

      ddl.create

      // Insert the conversation, which took place in Feb, 2001:
      val start = new DateTime(2001, 2, 17, 10, 22, 50)

      val dave = "Dave" :: 0L :: HNil
      val hal = "HAL" :: 0L :: HNil

      users ++= Seq(dave, hal)

      val oDave = users.filter(_.name === "Dave").firstOption
      val oHAL = users.filter(_.name === "HAL").firstOption

      for {
        dave ← oDave
        hal ← oHAL
      } {
        val index = Nat(1)
        val daveId = dave(index)
        val halId = hal(index)

        messages ++= Seq(
          Message(daveId, "Hello, HAL. Do you read me, HAL?", start),
          Message(halId, "Affirmative, Dave. I read you.", start plusSeconds 2),
          Message(daveId, "Open the pod bay doors, HAL.", start plusSeconds 4),
          Message(halId, "I'm sorry, Dave. I'm afraid I can't do that.", start plusSeconds 6))
      }

      users.iterator.foreach(println)
      messages.iterator.foreach(println)
  }

}