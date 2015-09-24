import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.H2Driver.api._
import slick.lifted.ProvenShape.proveShapeOf
import scala.util.Try

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
  def db = Database.forConfig("chapter03")

  // Helper method for running a query in this example file
  def exec[T](program: DBIO[T]): T =
    Await.result(db.run(program), 5000 milliseconds)


  def populate: DBIOAction[Option[Int], NoStream,Effect.All] =  for {
    //Create the table:
    _     <- messages.schema.create
    // Add some data:
    count <- messages ++= Seq(
      Message("Dave", "Hello, HAL. Do you read me, HAL?"),
      Message("HAL",  "Affirmative, Dave. I read you."),
      Message("Dave", "Open the pod bay doors, HAL."),
      Message("HAL",  "I'm sorry, Dave. I'm afraid I can't do that."))
  } yield count

  try {
    exec(populate)

    // -- INSERTS --

    // Insert one, returning the ID:
    val id = exec((messages returning messages.map(_.id)) += Message("HAL", "I'm back"))
    println(s"The ID inserted was: $id")


    // -- DELETES --

    // Delete messages from HAL:
    // NB: will be zero rows affected because we've renamed HAL to HALL 9000
    println("\nDeleting messages from HAL:")
    val rowsDeleted = exec(messages.filter(_.sender === "HAL").delete)
    println(s"Rows deleted: $rowsDeleted")



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

    // Client-side update:
    def exclaim(msg: Message): Message = msg.copy(content = msg.content + "!")

    val all: DBIO[Seq[Message]] = messages.result
    def modify(msg: Message): DBIO[Int] = messages.filter(_.id === msg.id).update(exclaim(msg))
    val action: DBIO[Seq[Int]] = all.flatMap( msgs => DBIO.sequence(msgs.map(modify)) )
    val rowCounts: Seq[Int] = exec(action)

    //
    // Action Combinators
    //

    // Map:

    // http://rosettacode.org/wiki/Rot-13#Scala
    def rot13(s: String) = s map {
      case c if 'a' <= c.toLower && c.toLower <= 'm' => c + 13 toChar
      case c if 'n' <= c.toLower && c.toLower <= 'z' => c - 13 toChar
      case c => c
    }

    val text: DBIO[String] = messages.map(_.content).result.head
    val encrypted: DBIO[String] = text.map(rot13) // obviously very weak "encryption"

    println("\nAn 'encrypted' message from the database:")
    println(exec(encrypted))

    // Filter and asTry:

    println("\nFiltering for a 'long message':")
    val longMsgAction: DBIO[Try[String]] = text.filter(s => s.length > 100).asTry
    println(exec(longMsgAction))


    // FlatMap:
    val delete: DBIO[Int] = messages.delete
    def insert(count: Int) = messages += Message("NOBODY", s"I removed ${count} messages")

    val resetMessagesAction: DBIO[Int] = delete.flatMap{ count => insert(count) }

    val resetMessagesAction2: DBIO[Int] =
      delete.flatMap{
        case 0 | 1 => DBIO.successful(0)
        case n     => insert(n)
      }

    //
    // unfold
    //

    final case class Room(name: String, next: String)

    final class FloorPlan(tag: Tag) extends Table[Room](tag, "floorplan") {
      def name = column[String]("name")
      def next = column[String]("next")
      def * = (name, next) <> (Room.tupled, Room.unapply)
    }

    lazy val floorplan = TableQuery[FloorPlan]

    exec {
      (floorplan.schema.create) >>
      (floorplan += Room("enter",  "room 1")) >>
      (floorplan += Room("room 1", "room 2")) >>
      (floorplan += Room("room 2", "room 3")) >>
      (floorplan += Room("room 3", "exit"))
    }

    def unfold[T]
      (z: T, acc: Seq[T] = Seq.empty)
      (f: T => DBIO[Option[T]]): DBIO[Seq[T]] =
      f(z).flatMap {
        case None    => DBIO.successful(acc)
        case Some(t) => unfold(t, z +: acc)(f)
      }

    println("\nRoom path:")
    val sa: DBIO[Seq[String]] =
      unfold("enter") {
         r => floorplan.filter(_.name === r).map(_.next).result.headOption
       }
    println( exec(sa) )

    def currentState() = {
      println("\nState of the database:")
      exec(messages.result.map(_.foreach(println)))
    }

    currentState

    // TODO: Why do we need this?   Maybe for exercises...
    exec(messages returning messages.map(_.id) += Message("Dave", "Point taken." ))



    def updateContent(id: Long) =
      messages.filter(_.id === id).map(_.content)

    try {
      exec {
        (
        updateContent(2L).update("Blue Mooon")                          andThen
        updateContent(3L).update("Please, anything but your singing ")  andThen
        messages.result.map(_.foreach { println })                      andThen
        DBIO.failed(new Exception("agggh my ears"))                     andThen
        updateContent(4L).update("That's incredibly hurtful")
        ).transactionally
      }

    } catch {
      case weKnow: Throwable => println("expected")
    }

    currentState
  } finally db.close

  //Exercises

  //Insert only once
  /*
  def insertOnce(sender: String, text: String): Long = ???

  println(insertOnce("Dave","Have you changed the locks?") == insertOnce("Dave","Have you changed the locks?"))

  //Update Using a For Comprehension
  val rowsAffected = messages.
                     filter(_.sender === "HAL").
                     map(msg => (msg.sender)).
                     update("HAL 9000")

  val rowsAffectedUsingForComprehension = ???
  */

}