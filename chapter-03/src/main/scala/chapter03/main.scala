package chapter03

import scala.slick.driver.H2Driver.simple._

object Example extends App {

  // Row representation:
  final case class Message(sender: String, content: String, id: Long = 0L)

  // Schema:
  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender  = column[String]("sender")
    def content = column[String]("content")
    def * = (sender, content, id) <> (Message.tupled, Message.unapply)
  }

  // Table:
  lazy val messages = TableQuery[MessageTable]

  // Database connection details:
  def db = Database.forURL("jdbc:h2:mem:chapter03", driver="org.h2.Driver")

  // Query execution:
  db.withSession {
    implicit session =>

      // Create the table:
      messages.ddl.create

      // Add some data:
      messages ++= Seq(
        Message("Dave", "Hello, HAL. Do you read me, HAL?"),
        Message("HAL",  "Affirmative, Dave. I read you."),
        Message("Dave", "Open the pod bay doors, HAL."),
        Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that.")
      )

      // Insert one, returning the ID:
      val id =
        (messages returning messages.map(_.id)) += Message("HAL", "I'm back")
      println(s"The ID inserted was: $id")

      // Update HAL's name:
      val rows = messages.filter(_.sender === "HAL").map(_.sender).update("HAL 9000")

      // Update HAL's name and message:
      val query =
        messages.
        filter(_.id === 4L).
        map(message => (message.sender, message.content))

      val rowsAffected = query.update("HAL 9000", "Sure, Dave. Come right in.")

      // Delete messages from HAL:
      // NB: will be zero rows affeced becase we've renamed HAL to HALL 9000
      messages.filter(_.sender === "HAL").delete

      // Current state of the database:
      println("\nState of the database:")
      messages.iterator.foreach(println)
  }
}