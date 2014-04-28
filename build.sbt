name := "core-slick-example"

version := "1.0"

scalaVersion := "2.10.3"

libraryDependencies += "com.typesafe.slick" %% "slick" % "2.0.1"

libraryDependencies += "org.postgresql" % "postgresql" % "9.3-1101-jdbc41"

libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.1.2"


initialCommands in console := """
import scala.slick.driver.PostgresDriver.simple._
val db = Database.forURL("jdbc:postgresql:core-slick", user="core", password="trustno1", driver = "org.postgresql.Driver")
implicit val session = db.createSession
println("\nSession created, but you may want to also import a schema. For example:\n\n    import underscoreio.schema.Example1._\n or import underscoreio.schema.Example5.Tables._\n")
//
"""
