import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import org.joda.time.DateTime

import ChatSchema._

import java.sql.Timestamp

object UpdateExample extends App {

  val schema = new Schema(slick.jdbc.H2Profile)
  import schema._, profile.api._

  val db = Database.forConfig("chapter07")

  // Helper method for running a query in this example file
  def exec[T](program: DBIO[T]): T =
    Await.result(db.run(program), 2 seconds)

  try {

    exec(populate)

    //
    // Simple update example:
    //

    val char = "!"
    val query = sqlu"""UPDATE "message" SET "content" = CONCAT("content", $char)"""
    val excalimedRows = exec(query)
    println(s"Exclamation mark added to $excalimedRows rows")


    //
    // Using DateTime as an example of a custom type
    //

    import slick.jdbc.SetParameter

    implicit val SetDateTime = SetParameter[DateTime](
       (dt, pp) => pp >> new Timestamp(dt.getMillis)
       // or if you prefer...
       //(dt, pp) => pp.setTimestamp(new Timestamp(dt.getMillis))
     )

    val now = sqlu"""UPDATE "message" SET "ts" = ${DateTime.now}"""
    val rowsChanged = exec(now)
    println(s"$rowsChanged rows changed")

  } finally db.close
}
