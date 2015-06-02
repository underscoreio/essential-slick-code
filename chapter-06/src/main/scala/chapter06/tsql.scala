package chapter06

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

// To validate tsql during compilation we supply
// database connection details via an annotation:
import slick.backend.StaticDatabaseConfig
@StaticDatabaseConfig("file:src/main/resources/application.conf#tsql")
object TsqlExample extends App {

  import slick.driver.H2Driver.api._

  // Simplified Schema...

  final case class Message(content: String, id: Long = 0L)

  val testData = Seq(
    Message("Hello, HAL. Do you read me, HAL?"),
    Message("Affirmative, Dave. I read you."),
    Message("Open the pod bay doors, HAL."),
    Message("I'm sorry, Dave. I'm afraid I can't do that.")
  )

  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content")
    def * = (content, id) <> (Message.tupled, Message.unapply)
  }

  lazy val messages = TableQuery[MessageTable]

  // Compile-time type checked query:
  val query: DBIO[Seq[String]] =
    tsql""" select "content" from "message" """


  // Execute the query:
  val prog = for {
    _   <- messages.schema.create
    _   <- messages ++= testData
    msg <- query
} yield msg

  val db = Database.forConfig("chapter06")
  val future = db.run(prog).map { _ foreach println }
  Await.result(future, 2 seconds)
  db.close
}