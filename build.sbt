name := "tinvest-bot"
ThisBuild / version := "0.1"
ThisBuild / organization := "github.ainr"

scalaVersion := "2.13.4"

val circeVersion = "0.13.0"
val http4sVersion = "0.21.7"
val doobieVersion = "0.8.8"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.0",
  "org.typelevel" %% "cats-core" % "2.1.1",
  "org.typelevel" %% "cats-effect" % "2.1.4",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "ch.qos.logback" % "logback-classic" % "1.1.3" % Runtime
)

scalacOptions ++= Seq(
  "-Xfatal-warnings"
)
