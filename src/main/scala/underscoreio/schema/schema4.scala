package underscoreio.schema

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.meta.MTable

object Example4 extends App {

  // Our schema:

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
  }


  // Our application:

  import Tables._

  db.withSession {
    implicit session =>

      // Create the database table:

      MTable.getTables(planets.baseTableRow.tableName).firstOption match {
        case None =>
          planets.ddl.create
        case Some(t) =>
          planets.ddl.drop
          planets.ddl.create
      }

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


      // Update one column:
      val udist = planets.filter(_.name === "Uranus").map(_.distance)
      udist.update(19.2)

      // Update two columns:
      val udist2 = planets.filter(_.name === "Uranus").map(p => (p.name, p.distance))
      udist2.update( ("Foo", 100.0) )

  }
}