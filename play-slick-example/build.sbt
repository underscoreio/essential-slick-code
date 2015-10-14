name := "essential-slick-play-integration"

version := "3.0"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Xfatal-warnings",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code"
)


libraryDependencies ++= Seq(
  "org.scala-lang"      % "scala-reflect"         % scalaVersion.value,
  "com.h2database"      %  "h2"                   % "1.4.185",
  "ch.qos.logback"      %  "logback-classic"      % "1.1.2",
  "joda-time"           %  "joda-time"            % "2.6",
  "org.joda"            %  "joda-convert"         % "1.2",
  "com.typesafe.play"   %% "play-slick"           % "1.1.0",
  "com.typesafe.play"   %% "play-json"            % "2.4.3")

// Play
lazy val root = (project in file(".")).enablePlugins(PlayScala)


