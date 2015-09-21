import org.joda.time.DateTime
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

object PKExample extends App {

  trait Profile {
    val profile:JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.api._

    case class User(id: Option[Long], name: String, email: Option[String] = None)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id    = column[Long]("id", O.AutoInc, O.PrimaryKey)
      def name  = column[String]("name")
      def email = column[Option[String]]("email")

      def * = (id.?, name, email) <> (User.tupled, User.unapply)
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    case class Room(title: String, id: Long = 0L)

    class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
     def id    = column[Long]("id", O.PrimaryKey, O.AutoInc)
     def title = column[String]("title")
     def * = (title, id) <> (Room.tupled, Room.unapply)
    }

    lazy val rooms = TableQuery[RoomTable]
    lazy val insertRoom = rooms returning rooms.map(_.id)

    case class Occupant(roomId: Long, userId: Long)

    class OccupantTable(tag: Tag) extends Table[Occupant](tag, "occupant") {
      def roomId = column[Long]("room")
      def userId = column[Long]("user")

      def pk = primaryKey("room_user_pk", (roomId, userId))

      def * = (roomId, userId) <> (Occupant.tupled, Occupant.unapply)
    }

    lazy val occupants = TableQuery[OccupantTable]

    lazy val ddl = users.schema ++ rooms.schema ++ occupants.schema
  }

  class Schema(val profile: JdbcProfile) extends Tables with Profile

  val schema = new Schema(slick.driver.H2Driver)

  import schema._, profile.api._

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  val db = Database.forConfig("chapter04")

  // Insert the conversation, which took place in Feb, 2001:
  val start = new DateTime(2001, 2, 17, 10, 22, 50)

  val init = for {
    _         <- ddl.create
    daveId    <- insertUser += User(None, "Dave", Some("dave@example.org"))
    halId     <- insertUser += User(None, "HAL")
    elena     <- insertUser += User(None, "Elena", Some("elena@example.org"))
    airLockId <- insertRoom += Room("Air Lock")
    _         <- occupants += Occupant(airLockId, daveId)
  } yield ()

  exec(init)
  exec(users.result).foreach { println}
  exec(occupants.result).foreach { println}

}