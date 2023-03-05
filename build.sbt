ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

val Http4sVersion = "1.0.0-M21"

lazy val root = (project in file("."))
  .settings(
    name := "subscription-plan"
  )

libraryDependencies ++=Seq(
  "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s" %% "http4s-circe" % Http4sVersion,
  "org.http4s" %% "http4s-dsl" % Http4sVersion,
  "io.circe" %% "circe-generic" % "0.14.3",
  "org.scalatest" %% "scalatest" % "3.2.15" % "test",
  "org.mockito" % "mockito-core" % "4.6.1" % Test,
  "org.mockito" %% "mockito-scala-scalatest" % "1.17.12" % Test

)