name := "essential-slick-chapter-01"

version := "1.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"           % "2.1.0",
  "com.h2database"      % "h2"              % "1.4.185",
  "ch.qos.logback"      % "logback-classic" % "1.1.2"
)

initialCommands in console := """
  |import scala.slick.driver.H2Driver.simple._
  |import Example._
  |Example.main(Array())
""".trim.stripMargin
