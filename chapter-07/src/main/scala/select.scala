import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import org.joda.time._

import ChatSchema._

object SelectExample extends App {

  val schema = new Schema(slick.jdbc.H2Profile)
  import schema._, profile.api._

  val db = Database.forConfig("chapter07")

  // Helper method for running a query in this example file
  def exec[T](program: DBIO[T]): T =
    Await.result(db.run(program), 2 seconds)

  try {

    //
    // Three simple select examples
    //

    // 1 - select all room IDs

    val ids = exec {
      for {
        _     <- populate
        query  = sql"""select "id" from "room" """.as[Long]
        ids   <- query
      } yield ids
    }

    println(s"Room IDs: $ids")

    // 2 - Select room IDs and names

    val roomInfoQuery = sql""" select "id", "title" from "room" """.as[(Long,String)]
    val roomInfo = exec(roomInfoQuery)
    println(s"Room information: $roomInfo")

    // 3 - Select with a value substitution

    val t = "Pod"
    val podRoom = exec {
     sql""" select "id", "title" from "room" where "title" = $t""".as[(Long,String)].headOption
   }
   println(s"Pod room: $podRoom")

   //
   // Get Result instance for DateTime
   //

   import slick.jdbc.GetResult

   implicit val GetDateTime =
     GetResult[DateTime](r => new DateTime(r.nextTimestamp(), DateTimeZone.UTC))

   val timestamps = exec {
     sql""" select "ts" from "message" """.as[DateTime]
   }
   println(s"Timestamps: $timestamps")


   //
   // Longer Get Result example for the Message case class
   //

   implicit val GetUserId    =
     GetResult(r => Id[UserTable](r.nextLong))

   implicit val GetMessageId =
     GetResult(r => Id[MessageTable](r.nextLong))

   implicit val GetOptionalUserId =
     GetResult(r => r.nextLongOption.map(id => Id[UserTable](id)))

   implicit val GetOptionalRoomId =
     GetResult(r => r.nextLongOption.map(id => Id[RoomTable](id)))

   implicit val GetMessage = GetResult(r =>
      Message(senderId  = r.<<,
              content   = r.<<,
              ts        = r.<<,
              id        = r.<<,
              roomId    = r.<<?,
              toId      = r.<<?) )

   val results = exec {
     sql""" select * from "message" """.as[Message]
   }

   results.foreach(result => println(result))

/*

   //
   // Robert tables example
   // You probably don't want to run this
   //

   def lookup(email: String) =
     sql"""select "id" from "user" where "user"."email" = '#${email}' """

   // OK...
   println(
     exec(lookup("dave@example.org").as[Long].headOption)
   )

   // Evil...
   println(
     exec(lookup(""" ';DROP TABLE "user";--- """).as[Long])
   )

   // This will produce: .JdbcSQLException: Table "user" not found;
   println(
     exec(lookup("dave@example.org").as[Long].headOption)
   )
*/
  } finally db.close
}
