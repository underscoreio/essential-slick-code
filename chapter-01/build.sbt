name := "essential-slick-chapter-01"

version := "3.0"

scalaVersion := "2.11.6"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code",
  "-Xlint",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"           % "3.0.0",
  "com.h2database"      % "h2"              % "1.4.185",
  "ch.qos.logback"      % "logback-classic" % "1.1.2"  % Runtime
)

initialCommands in console := """
  |import scala.slick.driver.H2Driver.simple._
  |import Example._
  |Example.main(Array())
""".trim.stripMargin
