// Import the Slick interface for H2:
import scala.slick.driver.H2Driver.simple._

object Example extends App {

  // Case class representing a row in our table:
  final case class Message(
    sender: String,
    content: String,
    id: Long = 0L)

  // Helper method for creating test data:
  def freshTestData = Seq(
    Message("Dave", "Hello, HAL. Do you read me, HAL?"),
    Message("HAL",  "Affirmative, Dave. I read you."),
    Message("Dave", "Open the pod bay doors, HAL."),
    Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that.")
  )

  // Schema for the "message" table:
  final class MessageTable(tag: Tag)
      extends Table[Message](tag, "message") {

    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender  = column[String]("sender")
    def content = column[String]("content")

    def * = (sender, content, id) <>
      (Message.tupled, Message.unapply)
  }

  // Base query for querying the messages table:
  val messages = TableQuery[MessageTable]

  // An example query that selects a subset of messages:
  val halSays = messages.filter(_.sender === "HAL")

  // Create a permanent in-memory H2 database;
  def db = Database.forURL(
    url    = "jdbc:h2:mem:chat-database;DB_CLOSE_DELAY=-1",
    driver = "org.h2.Driver")

  // Connect to the database...
  db.withSession { implicit session =>
    // Create the "messages" table:
    println("Creating database table")
    messages.ddl.create

    // Create and insert the test data:
    println("\nInserting test data")
    messages ++= freshTestData

    // Run the test query and print the results:
    println("\nSelecting all messages:")
    messages.run.foreach(println)

    println("\nSelecting only messages from HAL:")
    halSays.run.foreach(println)
  }
}
