import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

object ModifiersExample extends App {

  trait Profile {
    val profile: JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.api._

    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime, UTC))

    case class User(name: String, avatar: Option[Array[Byte]] = None, id: Long = 0L)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def name   = column[String]("name", O.Length(64, true), O.Default("Anonymous Coward"))
      def avatar = column[Option[Array[Byte]]]("avatar", O.SqlType("BINARY(2048)"))

      def nameIndex = index("name_idx", (name,avatar), unique=true)

      def * = (name, avatar, id) <> (User.tupled, User.unapply)
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    case class Message(
        senderId: Long,
        content:  String,
        ts:       DateTime,
        id:       Long = 0L)

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id       = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[Long]("sender")
      def content  = column[String]("content")
      def ts       = column[DateTime]("ts")

      def * = (senderId, content, ts, id) <> (Message.tupled, Message.unapply)

      def sender = foreignKey("sender_fk", senderId, users)(_.id, onDelete=ForeignKeyAction.Cascade)
    }

    lazy val messages = TableQuery[MessageTable]
    
    lazy val ddl = users.schema ++ messages.schema    
  }
  
 
  class Schema(val profile: JdbcProfile) extends Tables with Profile

  val schema = new Schema(slick.driver.H2Driver)

  import schema._, profile.api._

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)  
  
  def db = Database.forConfig("chapter04")

  // Insert the conversation, which took place in Feb, 2001:
  val start = new DateTime(2001, 2, 17, 10, 22, 50)  
  
  val initalise = 
    for {
    _      <- ddl.create
    halId  <- insertUser += User("HAL")
    daveId <- insertUser += User("Dave")
    count  <- messages ++= Seq(
               Message(daveId, "Hello, HAL. Do you read me, HAL?", start),
               Message(halId,  "Affirmative, Dave. I read you.", start plusSeconds 2),
               Message(daveId, "Open the pod bay doors, HAL.", start plusSeconds 4),
               Message(halId,  "I'm sorry, Dave. I'm afraid I can't do that.", start plusSeconds 6))
  } yield count

  // A simple join using the foreign key:
  val q = for {
    msg <- messages
    usr <- msg.sender
  } yield (usr.name, msg.content)

  val delete = users.filter(_.name === "HAL").delete
  
  exec(initalise)
  
  println(s"Result of join: ${exec(q.result)}" )
  // Example CASCADE DELETE:
  println(s"Rows deleted: ${exec(delete)}")
  println(s"Messages after delete: ${exec(messages.result)}")
}