import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.H2Profile.api._
import scala.util.Try

object Example extends App {

  // Row representation:
  final case class Message(sender: String, content: String, id: Long = 0L)

  // Schema:
  final class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
    def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def sender  = column[String]("sender")
    def content = column[String]("content")
    def * = (sender, content, id).mapTo[Message]
  }

  // Table:
  lazy val messages = TableQuery[MessageTable]

  // Database connection details:
  val db = Database.forConfig("chapter03")

  // Helper method for running a query in this example file:
  def exec[T](program: DBIO[T]): T =
    Await.result(db.run(program), 5000 milliseconds)


  def testData = Seq(
    Message("Dave", "Hello, HAL. Do you read me, HAL?"),
    Message("HAL",  "Affirmative, Dave. I read you."),
    Message("Dave", "Open the pod bay doors, HAL."),
    Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that."))

  def populate: DBIOAction[Option[Int], NoStream,Effect.All] =  {
    for {    
    //Drop table if it already exists, then create the table:
    _     <- messages.schema.drop.asTry andThen messages.schema.create
    // Add some data:
    count <- messages ++= testData
  } yield count
    
    
  }

  // Utility to print out what is in the database:
  def printCurrentDatabaseState() = {
    println("\nState of the database:")
    exec(messages.result.map(_.foreach(println)))
  }

  try {
    exec(populate)

    // -- INSERTS --

    // Insert one, returning the ID:
    val id = exec((messages returning messages.map(_.id)) += Message("HAL", "I'm back"))
    println(s"The ID inserted was: $id")

    // -- DELETES --

    // Delete messages from HAL:
    println("\nDeleting messages from HAL:")
    val rowsDeleted = exec(messages.filter(_.sender === "HAL").delete)
    println(s"Rows deleted: $rowsDeleted")

    // Repopulate the database:
    exec( messages ++= testData.filter(_.sender == "HAL") )

    printCurrentDatabaseState()

    // -- UPDATES --

    // Update HAL's name:
    val rows = exec(messages.filter(_.sender === "HAL").map(_.sender).update("HAL 9000"))

    // Update HAL's name and message:
    val query =
      messages.
        filter(_.id === 4L).
        map(message => (message.sender, message.content))

    val rowsAffected  = exec(query.update(("HAL 9000", "Sure, Dave. Come right in.")))

    // Action for updating multiple fields:
    exec {
      messages.
        filter(_.id === 4L).
        map(message => (message.sender, message.content)).
        update(("HAL 9000", "Sure, Dave. Come right in."))
      }

    // Using a clase class to update:
    case class NameText(name: String, text: String)
    val newValue = NameText("Dave", "Now I totally don't trust you.")

    exec {
      messages.filter(_.id === 7L).map( m => (m.sender, m.content).mapTo[NameText]).update(newValue)
    }
    // printCurrentDatabaseState()

    // Client-side update:
    def exclaim(msg: Message): Message = msg.copy(content = msg.content + "!")

    val all: DBIO[Seq[Message]] = messages.result
    def modify(msg: Message): DBIO[Int] = messages.filter(_.id === msg.id).update(exclaim(msg))
    val action: DBIO[Seq[Int]] = all.flatMap( msgs => DBIO.sequence(msgs.map(modify)) )
    val rowCounts: Seq[Int] = exec(action)

  } finally db.close

}
