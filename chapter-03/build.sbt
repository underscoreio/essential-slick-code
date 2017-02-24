name := "essential-slick-chapter-03"

version := "3.2"

scalaVersion := "2.12.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Xlint",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"           % "3.2.0",
  "com.h2database"      % "h2"              % "1.4.185",
  "ch.qos.logback"      % "logback-classic" % "1.1.2")

initialCommands in console := """
  |import slick.jdbc.H2Profile.api._
  |import Example._
  |import scala.concurrent.Await
  |import scala.concurrent.duration._
  |import scala.concurrent.ExecutionContext.Implicits.global
  |val db = Database.forConfig("chapter03")
  |def exec[T](program: DBIO[T]): T = Await.result(db.run(program), 5000 milliseconds)
  |exec(populate)
""".trim.stripMargin
