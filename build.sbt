name := "tinvest-bot"
ThisBuild / version := "0.1"
ThisBuild / organization := "github.ainr"

scalaVersion := "2.13.4"

//lazy val tinvest4s = ProjectRef(file("../tinvest4s/"), "tinvest4s")
lazy val tinvest4s = ProjectRef(uri("https://github.com/a-khakimov/tinvest4s.git#main"), "tinvest4s")
lazy val root = (project in file(".")).dependsOn(tinvest4s)

lazy val catsVersion = "2.1.4"
lazy val circeVersion = "0.13.0"
lazy val http4sVersion = "0.21.7"
lazy val doobieVersion = "0.9.0"
lazy val telegramiumVersion = "2.49.0"
lazy val pureConfigVersion = "0.13.0"
//autoCompilerPlugins := true

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.0",
  "org.typelevel" %% "cats-core" % "2.1.1",
  "org.typelevel" %% "cats-effect" % "2.1.4",
  "org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-postgres" % doobieVersion,
  "org.tpolecat" %% "doobie-specs2" % doobieVersion % "test",
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-jdk-http-client" % "0.3.1",
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-literal" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.github.apimorphism" %% "telegramium-core" % telegramiumVersion,
  "io.github.apimorphism" %% "telegramium-high" % telegramiumVersion,
  "com.github.pureconfig" %% "pureconfig" % pureConfigVersion,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "org.scalamock" %% "scalamock" % "4.4.0" % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "com.codecommit" %% "cats-effect-testing-scalatest" % "0.4.2" % Test
  //"ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime,
)

scalacOptions ++= Seq(
  "-Xfatal-warnings",
  "-deprecation"
)

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.1" cross CrossVersion.full)
