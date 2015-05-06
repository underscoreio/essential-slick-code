package chapter06

import org.joda.time._

import scala.slick.backend._
import scala.slick.jdbc._
import scala.slick.jdbc.StaticQuery.interpolation

import ChatSchema._

object PlainSelectExample extends App {

  val schema = new Schema(scala.slick.driver.H2Driver)
  import schema._, profile.simple._
  def db = Database.forURL("jdbc:h2:mem:chapter06", driver="org.h2.Driver")

  db withSession { implicit session =>

    populate

    //
    // Simple select examples
    //

    val roomIdsQuery = sql"""select "id" from "room" """.as[Long]
    val roomIds = roomIdsQuery.list
    println(roomIds)

    val roomInfoQuery = sql""" select "id", "title" from "room" """.as[(Long,String)]
    val roomInfo = roomInfoQuery.list
    println(roomInfo)

    val t = "Pod"
    println(
     sql""" select "id", "title" from "room" where "title" = $t""".as[(Long,String)].firstOption
    )

    //
    // Get Result instance for DateTime
    //

    implicit val GetDateTime = GetResult[DateTime](r => new DateTime(r.nextTimestamp(), DateTimeZone.UTC))
    println(
      sql""" select "ts" from "message" """.as[DateTime].list
    )

    //
    // The following implicit declarations are needs for sql interpolation
    //

    implicit val GetUserId    = GetResult(r => Id[UserTable](r.nextLong))
    implicit val GetMessageId = GetResult(r => Id[MessageTable](r.nextLong))

    implicit val GetOptionalUserId = GetResult(r => r.nextLongOption.map(id => Id[UserTable](id)))
    implicit val GetOptionalRoomId = GetResult(r => r.nextLongOption.map(id => Id[RoomTable](id)))

    implicit val GetMessage = GetResult(r =>
       Message(senderId  = r.<<,
               content   = r.<<,
               ts        = r.<<,
               id        = r.<<,
               roomId    = r.<<?,
               toId      = r.<<?) )

    val results: List[Message] =
      sql""" select * from "message" """.as[Message].list

    results.foreach(result => println(result))

    //
    // Robert tables example
    //
    /*
    def lookup(email: String) =
      sql"""select "id" from "user" where "user"."email" = '#${email}' """

    // OK...
    println(
      lookup("dave@example.org").as[Long].firstOption
    )

    // Evil...
    lookup(""" ';DROP TABLE "user";--- """).as[Long].list

    // This will produce: .JdbcSQLException: Table "user" not found;
    println(
      lookup("dave@example.org").as[Long].firstOption
    )
    */

  }
}