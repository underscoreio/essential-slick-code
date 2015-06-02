package chapter06
/*
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import org.joda.time.DateTime

import ChatSchema._

import java.sql.Timestamp

object StaticExample extends App {

  val schema = new Schema(slick.driver.H2Driver)
  import schema._, profile.api._

  val db = Database.forConfig("chapter06")

  // Helper method for running a query in this example file
  def exec[T](program: DBIO[T]): T =
    Await.result(db.run(program), 2 seconds)

  try {

    exec(populate)

    //
    // Static Queries (Using + and +?)
    //

    import slick.jdbc.{StaticQuery => Q}

    val update = Q.u + """ DELETE FROM "message" """
    val pattern = "%!"

    val sensitive = update + """ WHERE "content" NOT LIKE """ +? pattern
    val sensitiveRows = exec(sensitive)
    println(s"Exclamation mark added sensitively to $sensitiveRows rows")

    //
    // Using DateTime as an example of a custom type
    //

    import slick.jdbc.SetParameter

    implicit val SetDateTime = SetParameter[DateTime](
       (dt, pp) => pp >> new Timestamp(dt.getMillis)
       // or if you prefer...
       //(dt, pp) => pp.setTimestamp(new Timestamp(dt.getMillis))
     )

    val now = sqlu"""UPDATE message SET "ts" = """ +? DateTime.now
    val rowsChanged = exec(now)
    println(s"$rowsChanged rows changed")

  } finally db.close
}
*/