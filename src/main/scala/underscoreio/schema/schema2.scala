package underscoreio.schema

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.meta.MTable

object Example2 extends App {

  def db[T](f: => T) : T =
    Database.forURL("jdbc:postgresql:core-slick", user="core", password="trustno1", driver = "org.postgresql.Driver") withSession {
      implicit session =>
        f
    }

  class Planet(tag: Tag) extends Table[(Int,String,Double)](tag, "planet") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def distance = column[Double]("distance_au")
    def * = (id, name, distance)
  }

  lazy val planets = TableQuery[Planet]

  Database.forURL("jdbc:postgresql:core-slick", user="core", password="trustno1", driver = "org.postgresql.Driver") withSession {
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

      planets +=
        (100, "Earth",    1.0)

      planets ++= Seq(
        (200, "Mercury",  0.4),
        (300, "Venus",    0.7),
        (400, "Mars" ,    1.5),
        (500, "Jupiter",  5.2),
        (600, "Saturn",   9.5),
        (700, "Uranus",  19.0),
        (800, "Neptune", 30.0)
      )


      val query = for {
        planet <- planets
        if planet.distance < 5.0
      } yield planet.name

      println("Inner planets: " + query.run)

  }
}