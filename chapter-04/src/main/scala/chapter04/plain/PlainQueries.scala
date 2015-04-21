package chapter04
package plain

import org.joda.time._

import scala.slick.backend._
import scala.slick.driver.JdbcDriver.backend.{ Database ⇒ DDB }
import scala.slick.jdbc._

import MessagingSchema._

object PlainQueries extends App {

  def printStatements: DB ⇒ Unit = { info ⇒
    val schema = new Schema(info.driver)

    import schema._, profile.simple._

    def db = Database.forURL(info.url, driver = info.clazz)

    db withSession {
      implicit session ⇒
        (users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).create
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

      //Can we pass kind of Id into an implicit?
      implicit val getUserIdResult = GetResult(r ⇒ Id[UserTable](r.nextLong()))
      implicit val getRoomIdResult = GetResult(r ⇒ Id[RoomTable](r.nextLong()))

      implicit val getMessageIdResult = GetResult(r ⇒ Id[MessageTable](r.nextLong()))
      implicit val getDateTime = GetResult(r ⇒ new DateTime(r.nextTimestamp(), DateTimeZone.UTC))

      implicit val getOptionalUserIdResult: GetResult[Option[Id[UserTable]]] = GetResult(r ⇒ r.nextLongOption().map(i ⇒ Id[UserTable](i)))
      implicit val getOptionalRoomIdResult: GetResult[Option[Id[RoomTable]]] = GetResult(r ⇒ r.nextLongOption().map(i ⇒ Id[RoomTable](i)))

      implicit object SetUserTablePk extends SetParameter[Id[UserTable]] {
        def apply(pk: Id[UserTable], pp: PositionedParameters) { pp.setLong(pk.value) }
      }

      implicit object SetRoomTablePk extends SetParameter[Id[RoomTable]] {
        def apply(pk: Id[RoomTable], pp: PositionedParameters) { pp.setLong(pk.value) }
      }

      implicit object SetUserTableALLTHETHINGS extends SetParameter[Id[UserTable]] {
        def apply(pk: Id[UserTable], pp: PositionedParameters) { pp.setLong(pk.value) }
      }

      implicit val getMessage = GetResult(r ⇒ Message(senderId = r.<<,
        content = r.<<,
        ts = r.<<,
        id = r.<<,
        toId = r.<<?,
        roomId = r.<<?,
        readBy = r.<<))

      def insertU(u: User) = sqlu""" insert into "user" values (${u.id}, ${u.name}, ${u.email})""".first
      def insertR(r: Room) = (Q.u + "insert into \"room\" values (" +? r.title +? ")").execute

      //Yes yes it's evil.
      def idU(u: User): Id[UserTable] = sql"""select id from "user" where email = ${u.email}""".as[Id[UserTable]].first
      def idR(r: Room): Id[RoomTable] = sql"""select id from "room" where title = ${r.title}""".as[Id[RoomTable]].first

      insertU(dave)
      insertU(hal)
      insertU(elena)
      insertU(frank)

      val daveId: Id[UserTable] = idU(dave)
      val halId: Id[UserTable] = idU(hal)
      val elenaId: Id[UserTable] = idU(elena)
      val frankId: Id[UserTable] = idU(frank)

      insertR(airLock)
      insertR(pod)

      val airLockId: Id[RoomTable] = idR(airLock)
      val podId: Id[RoomTable] = idR(pod)

      // Populate Rooms
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

      val davesAirLockMessages = sql"""select *
              from "message" left outer join "user" on "message"."sender" = "user"."id"
                             left outer join "room" on "message"."room"   = "room"."id"
              where "user"."id" = ${daveId} and "room"."id" = ${airLockId}"""

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