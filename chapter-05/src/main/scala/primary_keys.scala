import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import slick.jdbc.JdbcProfile

// Code relating to 5.3.2 "Primary Keys".

object PKExample extends App {

  trait Profile {
    val profile: JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.api._

    //
    // The names "HAL", "Dave" and so on, are now stored in a user table:
    //
    case class User(id: Option[Long], name: String, email: Option[String] = None)

    class UserTable(tag: Tag) extends Table[User](tag, "user") {
      def id    = column[Long]("id", O.AutoInc, O.PrimaryKey)
      def name  = column[String]("name")
      def email = column[Option[String]]("email")

      def * = (id.?, name, email).mapTo[User]
    }

    lazy val users = TableQuery[UserTable]
    lazy val insertUser = users returning users.map(_.id)

    //
    // We also represent the various rooms on the ship:
    //
    case class Room(title: String, id: Long = 0L)

    class RoomTable(tag: Tag) extends Table[Room](tag, "room") {
     def id    = column[Long]("id", O.PrimaryKey, O.AutoInc)
     def title = column[String]("title")
     def * = (title, id).mapTo[Room]
    }

    lazy val rooms = TableQuery[RoomTable]
    lazy val insertRoom = rooms returning rooms.map(_.id)

    //
    // A user can be in a room, which we represent in the "occupant" table:
    //
    case class Occupant(roomId: Long, userId: Long)

    class OccupantTable(tag: Tag) extends Table[Occupant](tag, "occupant") {
      def roomId = column[Long]("room")
      def userId = column[Long]("user")

      def pk = primaryKey("room_user_pk", (roomId, userId))

      def * = (roomId, userId).mapTo[Occupant]
    }

    lazy val occupants = TableQuery[OccupantTable]

    //
    // The schema for all three tables:
    //
    lazy val ddl = users.schema ++ rooms.schema ++ occupants.schema
  }

  class Schema(val profile: JdbcProfile) extends Tables with Profile

  val schema = new Schema(slick.jdbc.H2Profile)

  import schema._, profile.api._

  def exec[T](action: DBIO[T]): T =
    Await.result(db.run(action), 2 seconds)

  val db = Database.forConfig("chapter05")

  //
  // Set up the database, populate rooms and users,
  // and place Dave in the Air Lock:
  //
  val init = for {
    _         <- ddl.create
    daveId    <- insertUser += User(None, "Dave", Some("dave@example.org"))
    halId     <- insertUser += User(None, "HAL")
    elena     <- insertUser += User(None, "Elena", Some("elena@example.org"))
    airLockId <- insertRoom += Room("Air Lock")
    _         <- occupants += Occupant(airLockId, daveId)
  } yield ()

  exec(init)

  println("\nUsers database contains:")
  exec(users.result).foreach { println}

  println("\nOccupation is:")
  exec(occupants.result).foreach { println}

}
