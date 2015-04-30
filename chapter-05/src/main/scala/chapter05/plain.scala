package chapter05
package plain

import org.joda.time._

import scala.slick.backend._
import scala.slick.driver.JdbcDriver.backend.{ Database ⇒ DDB }
import scala.slick.jdbc._
import ChatSchema._
object PlainQueries extends App {

  val dbURL   = "jdbc:h2:mem:chapter05;DB_CLOSE_DELAY=-1 "
  val dbDriver = "org.h2.Driver"

  val schema = new Schema(scala.slick.driver.H2Driver)
  import schema._, profile.simple._
  def db = Database.forURL(dbURL,dbDriver)

  //Create and populate the database as we have done in the previous chapters
  db.withSession {
    implicit session =>
     populate
  }

  import scala.slick.driver.JdbcDriver.backend.{ Database ⇒ DDB }
  import Database.dynamicSession
  import scala.slick.jdbc.{ StaticQuery ⇒ Q }

  DDB.forURL(dbURL,dbDriver) withDynSession {
    import Q.interpolation

     val daveId:Id[UserTable]    = Id(1)
     val airLockId:Id[UserTable] = Id(1)

      val plainSQL = sql"""
                    select *
                    from "message" inner join "user" on "message"."sender" = "user"."id"
                                   inner join "room" on "message"."room"   = "room"."id"
                    where "user"."id" = ${daveId} and "room"."id" = ${airLockId}"""

      val results = plainSQL.as[Message].list

      results.foreach(result => println(result))


  }
}



//  def printStatements: DB ⇒ Unit = { info ⇒
//    val schema = new Schema(info.driver)
//
//    import schema._, profile.simple._
//
//    def db = Database.forURL(info.url, driver = info.clazz)
//
//    db withSession {
//      implicit session ⇒
//        (users.ddl ++ rooms.ddl ++ occupants.ddl ++ messages.ddl).create
//    }
//
//    import scala.slick.driver.JdbcDriver.backend.{ Database ⇒ DDB }
//    import Database.dynamicSession
//    import scala.slick.jdbc.{ GetResult, StaticQuery ⇒ Q }
//    println("============================================")
//    println("============= Plain SQL ====================")
//    println("============================================")
//    DDB.forURL(info.url, driver = info.clazz) withDynSession {
//      import Q.interpolation
//
//      val dave  = User("Dave",  Some("dave@example.org"))
//      val hal   = User("HAL",   Some("hal@example.org"))
//      val elena = User("Elena", Some("elena@example.org"))
//      val frank = User("Frank", Some("frank@example.org"))
//
//      val airLock = Room("Air Lock")
//      val pod     = Room("Pod")
//

//
//      def insertU(u: User):Option[Id[UserTable]]  = {
//        val insert                                   = sqlu""" insert into "user" values (${u.id}, ${u.name}, ${u.email})""".firstOption
//        lazy val empty:Option[Id[UserTable]]         = { println(s"Insert failed for $u");None}
//        lazy val getId: Int => Option[Id[UserTable]] = _ =>  sql"""select "id" from "user" where "email" = ${u.email}""".as[Id[UserTable]].firstOption
//
//        insert.fold(empty)(getId)
//      }
//
//
//      def insertR(r: Room):Option[Id[RoomTable]]  = {
//        val insert:Option[Int]                      = (Q.u + "insert into \"room\" values (" +? r.id + "," +? r.title + ")").firstOption
//        lazy val empty:Option[Id[RoomTable]]        = { println(s"Insert failed for $r");None}
//        lazy val getId:Int => Option[Id[RoomTable]] = _ => sql"""select "id" from "room" where "title" = ${r.title}""".as[Id[RoomTable]].firstOption
//        insert.fold(empty)(getId)
//      }
//
//
//      val daveId  = insertU(dave)
//      val halId   = insertU(hal)
//      val elenaId = insertU(elena)
//      val frankId = insertU(frank)
//
//      val airLockId = insertR(airLock)
//      val podId     = insertR(pod)
//
//      println(daveId)
//      println(halId)
//      println(elenaId)
//      println(frankId)
//      println(airLockId)
//      println(podId)
////
////      // Populate Rooms
////      occupants ++= List(Occupant(airLockId, daveId),
////        Occupant(airLockId, halId),
////        Occupant(podId, daveId),
////        Occupant(podId, frankId),
////        Occupant(podId, halId))
////
////      // Insert the conversation, which took place in Feb, 2001:
////      val airLockConversation = new DateTime(2001, 2, 17, 10, 22, 50)
////
////      //add some messages to the room.
////      messages ++= Seq(
////        Message(daveId, "Hello, HAL. Do you read me, HAL?", airLockConversation, Id(0L), None, Some(airLockId), 1),
////        Message(halId, "Affirmative, Dave. I read you.", airLockConversation plusSeconds 2, Id(0L), None, Some(airLockId), 1),
////        Message(daveId, "Open the pod bay doors, HAL.", airLockConversation plusSeconds 4, Id(0L), None, Some(airLockId), 1),
////        Message(halId, "I'm sorry, Dave. I'm afraid I can't do that.", airLockConversation plusSeconds 6, Id(0L), None, Some(airLockId), 1))
////
////      val podConversation = new DateTime(2001, 2, 16, 20, 55, 0)
////
////      messages ++= Seq(
////        Message(frankId, "Well, whaddya think?", podConversation, Id(0L), None, Some(podId), 2),
////        Message(daveId, "I'm not sure, what do you think?", podConversation plusSeconds 4, Id(0L), None, Some(podId), 2))
////
////      val davesAirLockMessages = sql"""select *
////              from "message" left outer join "user" on "message"."sender" = "user"."id"
////                             left outer join "room" on "message"."room"   = "room"."id"
////              where "user"."id" = ${daveId} and "room"."id" = ${airLockId}"""
////
////      davesAirLockMessages.as[Message].list.foreach { println }
//
//    }
//
//  }
//
//





