import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.JdbcProfile

//
// Solution to "Custom Boolean" exercise
//

object CustomBooleanExample extends App {

  trait Profile {
    val profile: JdbcProfile
  }

  object PKs {
    import slick.lifted.MappedTo
    case class MessagePK(value: Long) extends AnyVal with MappedTo[Long]
    case class UserPK(value: Long) extends AnyVal with MappedTo[Long]
  }

  trait Tables {
    this: Profile =>

    import profile.api._
    import PKs.{UserPK, MessagePK}

    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime, UTC))

    case class User(name: String, id: UserPK = UserPK(0L))

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id   = column[UserPK]("id", O.PrimaryKey, O.AutoInc)
      def name = column[String]("name")

      def * = (name, id).mapTo[User]
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    sealed trait Priority
    case object HighPriority extends Priority
    case object LowPriority  extends Priority

    implicit val priorityType =
      MappedColumnType.base[Priority, String](
        flag => flag match {
          case HighPriority => "y"
          case LowPriority  => "n"
        },
        ch => ch match {
          case "Y" | "y" | "+" | "high"          => HighPriority
          case "N" | "n" | "-" | "lo"   | "low"  => LowPriority
      })

    case class Message(
        senderId: UserPK,
        content:  String,
        ts:       DateTime,
        flag:     Option[Priority] = None,
        id:       MessagePK = MessagePK(0L))

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id       = column[MessagePK]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[UserPK]("sender")
      def content  = column[String]("content")
      def priority = column[Option[Priority]]("priority")
      def ts       = column[DateTime]("ts")

      def * = (senderId, content, ts, priority, id).mapTo[Message]

      def sender = foreignKey("sender_fk", senderId, users)(_.id, onDelete=ForeignKeyAction.Cascade)
    }

    lazy val messages = TableQuery[MessageTable]

    lazy val ddl = users.schema ++ messages.schema
  }


  class Schema(val profile: slick.jdbc.JdbcProfile) extends Tables with Profile

  val schema = new Schema(slick.jdbc.H2Profile)

  import schema._, profile.api._

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  val db = Database.forConfig("chapter05")

 // Insert the conversation, which took place in Feb, 2001:
  val start = new DateTime(2001, 2, 17, 10, 22, 50)

  val program =
    for {
    _      <- ddl.create
    halId  <- insertUser += User("HAL")
    daveId <- insertUser += User("Dave")
    _      <- messages ++= Seq(
               Message(daveId, "Hello, HAL. Do you read me, HAL?", start),
               Message(halId,  "Affirmative, Dave. I read you.", start plusSeconds 2),
               Message(daveId, "Open the pod bay doors, HAL.", start plusSeconds 4),
               Message(halId,  "I'm sorry, Dave. I'm afraid I can't do that.", start plusSeconds 6, Some(HighPriority)))
    result <- messages.filter(_.priority === (HighPriority:Priority)).result
  } yield result


  println(s"query result ${exec(program)}" )
}
