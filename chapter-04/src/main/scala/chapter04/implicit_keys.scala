package chapter04

object ImplicitJoinsExample extends App {

  trait Profile {
    val profile: scala.slick.driver.JdbcProfile
  }

  trait Tables {
    this: Profile =>

    import profile.simple._

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
  }

  class Schema(val profile: scala.slick.driver.JdbcProfile) extends Tables with Profile

  val schema = new Schema(scala.slick.driver.H2Driver)

  import schema._, profile.simple._

  def db = Database.forURL("jdbc:h2:mem:chapter03", driver = "org.h2.Driver")

  db.withSession {
    implicit session =>

      (users.ddl ++ rooms.ddl ++ occupants.ddl).create

      // A few users:
      val daveId: Long = insertUser += User(None, "Dave", Some("dave@example.org"))
      val halId:  Long = insertUser += User(None, "HAL")
      val elena:  Long = insertUser += User(None, "Elena", Some("elena@example.org"))

      println( users.list )

      // A room:
      val airLockId: Long = insertRoom += Room("Air Lock")

      // Put Dave in the Room:
      occupants += Occupant(daveId, airLockId)

      println(occupants.list)
  }

}