name := "essential-slick"

version := "1.0"

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
  "com.typesafe.slick" %% "slick"           % "3.1.0",
  "com.h2database"      % "h2"              % "1.4.185",
  "ch.qos.logback"      % "logback-classic" % "1.1.2",
  "com.lihaoyi"         % "ammonite-repl"   % "0.4.8" cross CrossVersion.full
)

val ammoniteInitialCommands = """
  |import slick.driver.H2Driver.api._
  |import scala.concurrent._
  |import scala.concurrent.duration._
  |import scala.concurrent.ExecutionContext.Implicits.global
  |repl.prompt() = "scala> "
  |repl.colors() = ammonite.repl.Colors.Default.copy(
  |  prompt  = ammonite.repl.Ref(Console.BLUE),
  |  `type`  = ammonite.repl.Ref(Console.CYAN),
  |  literal = ammonite.repl.Ref(Console.YELLOW),
  |  comment = ammonite.repl.Ref(Console.WHITE),
  |  keyword = ammonite.repl.Ref(Console.RED)
  |)
""".trim.stripMargin

initialCommands in console := s"""
  |ammonite.repl.Repl.run(\"\"\"$ammoniteInitialCommands\"\"\")
""".trim.stripMargin
