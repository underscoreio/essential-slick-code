package underscoreio.schema

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.meta.MTable

object Example5 extends App {

  object Tables extends {
    val profile = scala.slick.driver.PostgresDriver
  } with Tables {
    val db = Database.forURL("jdbc:postgresql:core-slick", user="core", password="trustno1", driver = "org.postgresql.Driver")
  }

  trait Tables {

    val profile: scala.slick.driver.JdbcProfile
    import profile.simple._

    case class Planet(name: String, distance: Double, id: Long=0L)

    class PlanetTable(tag: Tag) extends Table[Planet](tag, "planet") {
      def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def name = column[String]("name")
      def distance = column[Double]("distance_au")
      def * = (name, distance, id) <> (Planet.tupled, Planet.unapply)
    }

    lazy val planets = TableQuery[PlanetTable]

    case class Moon(name: String, planetId: Long, id: Long=0L)

    class MoonTable(tag: Tag) extends Table[Moon](tag, "moon") {
      def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
      def name = column[String]("name")
      def planetId = column[Long]("planet_id")

      def * = (name, planetId, id) <> (Moon.tupled, Moon.unapply)

      def planet = foreignKey("planet_fk", planetId, planets)(_.id)

    }

    lazy val moons = TableQuery[MoonTable]


    def exists[T <: Table[_]](table: TableQuery[T])(implicit session: Session) : Boolean =
      MTable.getTables(table.baseTableRow.tableName).firstOption.isDefined

    def dropAndCreate(implicit session: Session) : Unit = {
      if (exists(moons)) moons.ddl.drop
      if (exists(planets)) planets.ddl.drop
      (planets.ddl ++ moons.ddl).create
    }

  }


  // Our application:

  import Tables._

  db.withSession {
    implicit session =>

      // Create the database table:
      dropAndCreate

      // Populate with some data:

      planets += Planet("Earth", 1.0)

      planets ++= Seq(
        Planet("Mercury",  0.4),
        Planet("Venus",    0.7),
        Planet("Mars" ,    1.5),
        Planet("Jupiter",  5.2),
        Planet("Saturn",   9.5),
        Planet("Uranus",  19.0),
        Planet("Neptune", 30.0)
      )

      // We want to look up a planet by name to create the association
      def idOf(planetName: String) : Long =
        planets.filter(_.name === planetName).map(_.id).first

      val earthId = idOf("Earth")

      moons += Moon("The Moon", earthId)

      val marsId = idOf("Mars")
      moons ++= Seq(
        Moon("Phobos", marsId),
        Moon("Deimos",  marsId)
      )

      // A Join
      val query = for {
        (planet, moon) <- moons innerJoin planets on (_.planetId === _.id)
      } yield (planet.name, moon.name)

      // select x2."name", x3."name" from "planet" x2, "moon" x3 where x3."planet_id" = x2."id"
//      val query = for {
//          p <- planets
//          m <- moons
//          if m.planetId === p.id
//      } yield (p.name, m.name)

      println(query.run)


  }
}