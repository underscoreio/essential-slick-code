name := "essential-slick-chapter-04"

version := "1.0"

scalaVersion := "2.11.6"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-feature",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-dead-code"
)


libraryDependencies ++= Seq(
  "org.scala-lang"      % "scala-reflect"         % scalaVersion.value,
  "com.typesafe.slick" %% "slick"                 % "2.1.0",
  "com.h2database"      % "h2"                    % "1.4.185",
  "org.postgresql"      % "postgresql"            % "9.3-1100-jdbc41",
  "mysql"               % "mysql-connector-java"  % "5.1.35",
  "ch.qos.logback"      % "logback-classic"       % "1.1.2",
  "joda-time"           % "joda-time"             % "2.6",
  "org.joda"            % "joda-convert"          % "1.2")

triggeredMessage in ThisBuild := Watched.clearWhenTriggered