package underscoreio.schema

import scala.slick.driver.PostgresDriver.simple._

object Example1 extends App {

  class Planet(tag: Tag) extends Table[(Int,String,Double)](tag, "planet") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def distance = column[Double]("distance_au")
    def * = (id, name, distance)
  }

  lazy val planets = TableQuery[Planet]

  Database.forURL("jdbc:postgresql:core-slick", user="core", password="trustno1", driver = "org.postgresql.Driver") withSession {
    implicit session =>
      planets.ddl.create
  }

}
