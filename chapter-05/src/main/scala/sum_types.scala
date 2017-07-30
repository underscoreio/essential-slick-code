import slick.jdbc.JdbcProfile
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration._

// Code relating to 5.4.2 "Sum Types"

object SumTypesExample extends App {

  trait Profile {
    val profile: JdbcProfile
  }

  // We represent primary keys using these mapped value classes
  // described in section 5.4.1
  object PKs {
    import slick.lifted.MappedTo
    case class MessagePK(value: Long) extends AnyVal with MappedTo[Long]
    case class UserPK(value: Long) extends AnyVal with MappedTo[Long]
  }

  trait Tables {
    this: Profile =>

    import profile.api._
    import PKs._

    case class User(name: String, id: UserPK = UserPK(0L))

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id   = column[UserPK]("id", O.PrimaryKey, O.AutoInc)
      def name = column[String]("name")

      def * = (name, id).mapTo[User]
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    //
    // The sum type we want to represent:
    //
    sealed trait Flag
    case object Important extends Flag
    case object Offensive extends Flag
    case object Spam extends Flag

    //
    // The custom mapping into our arbitrary database representation:
    //
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
        flag:     Option[Flag] = None,
        id:       MessagePK = MessagePK(0L))

    class MessageTable(tag: Tag) extends Table[Message](tag, "message") {
      def id       = column[MessagePK]("id", O.PrimaryKey, O.AutoInc)
      def senderId = column[UserPK]("sender")
      def content  = column[String]("content")
      def flag     = column[Option[Flag]]("flag")

      def * = (senderId, content, flag, id).mapTo[Message]

      def sender = foreignKey("sender_fk", senderId, users)(_.id, onDelete=ForeignKeyAction.Cascade)
    }

    lazy val messages = TableQuery[MessageTable]

    lazy val ddl = users.schema ++ messages.schema
  }


  class Schema(val profile:JdbcProfile) extends Tables with Profile

  val schema = new Schema(slick.jdbc.H2Profile)

  import schema._, profile.api._

  val db = Database.forConfig("chapter05")

  val program =
    for {
    _      <- ddl.create
    halId  <- insertUser += User("HAL")
    daveId <- insertUser += User("Dave")
    count  <- messages ++= Seq(
               Message(daveId, "Hello, HAL. Do you read me, HAL?"),
               Message(halId,  "Affirmative, Dave. I read you."),
               Message(daveId, "Open the pod bay doors, HAL."),
               Message(halId,  "I'm sorry, Dave. I'm afraid I can't do that.", Some(Important)))
    msgs   <- messages.filter(_.flag === (Important : Flag)).result
  } yield  msgs

  val result =  Await.result(db.run(program), 2 seconds)

  println("\nThe list of important messages:")
  println(result)

}
