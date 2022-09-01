name := "essential-slick-chapter-01"

version := "3.4"

scalaVersion := "2.13.8"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"           % "3.4.0",
  "com.h2database"      % "h2"              % "2.1.214",
  "ch.qos.logback"      % "logback-classic" % "1.3.0"
)

 console / initialCommands := """
  |import slick.jdbc.H2Profile.api._
  |import Example._
  |import scala.concurrent.duration._
  |import scala.concurrent.Await
  |import scala.concurrent.ExecutionContext.Implicits.global
  |val db = Database.forConfig("chapter01")
  |def exec[T](program: DBIO[T]): T = Await.result(db.run(program), 2 seconds)
  |exec(messages.schema.create)
  |exec(messages ++= freshTestData)
""".trim.stripMargin
