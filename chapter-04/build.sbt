name := "essential-slick-chapter-04"

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
  "com.typesafe.slick" %% "slick"           % "3.1.0-RC2",
  "com.h2database"      % "h2"              % "1.4.185",
  "ch.qos.logback"      % "logback-classic" % "1.1.2",
  "joda-time"           % "joda-time"       % "2.6",
  "org.joda"            % "joda-convert"    % "1.2")

initialCommands in console := """
  |import slick.driver.H2Driver.api._
  |import scala.concurrent.ExecutionContext.Implicits.global
  |import scala.concurrent.Await
  |import scala.concurrent.duration._
  |val db = Database.forConfig("chapter04")
  |def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2 seconds)
""".trim.stripMargin
