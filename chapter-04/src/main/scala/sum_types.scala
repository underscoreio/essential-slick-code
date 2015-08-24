import java.sql.Timestamp
import org.joda.time.DateTime
import org.joda.time.DateTimeZone.UTC
import slick.driver.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._
import slick.lifted.MappedTo
import slick.lifted.ProvenShape.proveShapeOf


object SumTypesExample extends App {

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
    import PKs._

    implicit val jodaDateTimeType =
      MappedColumnType.base[DateTime, Timestamp](
        dt => new Timestamp(dt.getMillis),
        ts => new DateTime(ts.getTime, UTC))

    case class User(name: String, id: UserPK = UserPK(0L))

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id   = column[UserPK]("id", O.PrimaryKey, O.AutoInc)
      def name = column[String]("name")

      def * = (name, id) <> (User.tupled, User.unapply)
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)


    sealed trait Flag
    case object Important extends Flag
    case object Offensive extends Flag
    case object Spam extends Flag
    // case object OffTopic extends Flag

    implicit val flagType =
      MappedColumnType.base[Flag, Char](
        f => f match {
          case Important => '!'
          case Offensive => 'X'
          case Spam      => '$'
        },
        c => c match {
          case '!' => Important
          case 'X' => Offensive
          case '$' => Spam
        })

    case class Message(
        senderId: UserPK,
        content:  String,
        ts:       DateTime,
        flag:     Option[Flag] = None,
        id:       MessagePK = MessagePK(0L))

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id       = column[MessagePK]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[UserPK]("sender")
      def content  = column[String]("content")
      def flag     = column[Option[Flag]]("flag")
      def ts       = column[DateTime]("ts")

      def * = (senderId, content, ts, flag, id) <> (Message.tupled, Message.unapply)

      def sender = foreignKey("sender_fk", senderId, users)(_.id, onDelete=ForeignKeyAction.Cascade)
    }

    lazy val messages = TableQuery[MessageTable]
    
    lazy val ddl = users.schema ++ messages.schema
  }


  class Schema(val profile:JdbcProfile) extends Tables with Profile

  val schema = new Schema(slick.driver.H2Driver)

  import schema._, profile.api._
  import PKs._

  def db = Database.forConfig("chapter04")

  // Insert the conversation, which took place in Feb, 2001:
  val start = new DateTime(2001, 2, 17, 10, 22, 50)  
  
  val program = 
    for {
    _      <- ddl.create
    halId  <- insertUser += User("HAL")
    daveId <- insertUser += User("Dave")
    count  <- messages ++= Seq(
               Message(daveId, "Hello, HAL. Do you read me, HAL?", start),
               Message(halId,  "Affirmative, Dave. I read you.", start plusSeconds 2),
               Message(daveId, "Open the pod bay doors, HAL.", start plusSeconds 4),
               Message(halId,  "I'm sorry, Dave. I'm afraid I can't do that.", start plusSeconds 6, Some(Important)))
    msgs   <- messages.filter(_.flag === (Important : Flag)).result                          
  } yield  msgs

  val result =  Await.result(db.run(program), 2 seconds)
 
  println(result)  

}