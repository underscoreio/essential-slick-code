package underscoreio.schema

import scala.slick.driver.PostgresDriver.simple._
import scala.slick.jdbc.meta.MTable

object Example1 extends App {

  class Planet(tag: Tag) extends Table[(Int,String)](tag, "planet") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def name = column[String]("name")
    def * = (id, name)
  }

  lazy val planets = TableQuery[Planet]

  Database.forURL("jdbc:postgresql:core-slick", user="core", password="trustno1", driver = "org.postgresql.Driver") withSession {
    implicit session =>

      if (MTable.getTables(planets.baseTableRow.tableName).firstOption.isEmpty)
        planets.ddl.create

  }

}
