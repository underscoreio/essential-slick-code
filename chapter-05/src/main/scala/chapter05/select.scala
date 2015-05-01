package chapter05


import org.joda.time._

import scala.slick.backend._
import scala.slick.driver.JdbcDriver.backend.{ Database ⇒ DDB }
import scala.slick.jdbc._
import scala.slick.jdbc.StaticQuery.interpolation

import ChatSchema._


object PlainSelectExample extends App {

  //DB_CLOSE_DELAY=-1 keeps in the memory database live across the life of the JVM
  val dbURL   = "jdbc:h2:mem:chapter05;DB_CLOSE_DELAY=-1;"
  val dbDriver = "org.h2.Driver"

  val schema = new Schema(scala.slick.driver.H2Driver)
  import schema._, profile.simple._
  def db = Database.forURL(dbURL,dbDriver)

  db withSession { implicit session =>
    populate

    //Simple select example:
    sql"""select "room" from "message" """.as[Long].foreach { print }
    //with custom types:
    sql"""select "room" from "message" """.as[Id[RoomTable]].foreach { print }

    //with
    val base = sql"""select "senderId" from "message"""".as[Long]
    val extend = base + """WHERE contents like """ +? "%Dave%"

    //Extensive example

    // No type checking, we can pass garbage in, BE CAREFUL
     val daveId:Id[RoomTable]    = Id(1)
     val airLockId               = 1

      val plainSQL = sql"""
                    select *
                    from "message" inner join "user" on "message"."sender" = "user"."id"
                                   inner join "room" on "message"."room"   = "room"."id"
                    where "user"."id" = ${daveId} and "room"."id" = ${airLockId}"""

      //Custom result type
      val results = plainSQL.as[Message].list

      results.foreach(result => println(result))


        //      Robert tables example
      //      def userByEmail(email:String) = sql"""select * from "user" where "user"."email" = '#${email}'"""
      //
      //      val ohDear = userByEmail("""';DROP TABLE "user";--- """).as[User].list
      //
      //      results.foreach(result => println(result))
      //
      //      sql"""select * from "user" """.as[User].list.foreach(result => println(result))
  }
  }
