name := "essential-slick-chapter-07"

version := "3.1"

scalaVersion := "2.11.7"

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
  "com.typesafe.slick" %% "slick"                 % "3.1.1",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.1.1",
  "com.h2database"      % "h2"                    % "1.4.185",
  "org.postgresql"      % "postgresql"            % "9.3-1100-jdbc41",
  "mysql"               % "mysql-connector-java"  % "5.1.35",
  "ch.qos.logback"      % "logback-classic"       % "1.1.2",
  "joda-time"           % "joda-time"             % "2.6",
  "org.joda"            % "joda-convert"          % "1.2")

initialCommands in console := """
  |import scala.concurrent.ExecutionContext.Implicits.global
  |import scala.concurrent.Await
  |import scala.concurrent.duration._
  |import ChatSchema._
  |val schema = new Schema(slick.driver.H2Driver)
  |import schema._
  |import profile.api._
  |def exec[T](action: DBIO[T]): T = Await.result(db.run(action), 2 seconds)
  |val db = Database.forConfig("chapter07")
  |exec(populate)
""".trim.stripMargin

