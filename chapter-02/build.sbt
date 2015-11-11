name := "essential-slick-chapter-02"

version := "3.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick"           % "3.1.0",
  "com.h2database"      % "h2"              % "1.4.185",
  "ch.qos.logback"      % "logback-classic" % "1.1.2"
)

initialCommands in console := """
  |import slick.driver.H2Driver.api._
  |import Example._
  |import scala.concurrent.Await
  |import scala.concurrent.duration._
  |import scala.concurrent.ExecutionContext.Implicits.global
  |val db = Database.forConfig("chapter02")
  |def exec[T](program: DBIO[T]): T = Await.result(db.run(program), 2.seconds)
  |exec(messages.schema.create)
  |exec(messages ++= freshTestData)
""".trim.stripMargin
