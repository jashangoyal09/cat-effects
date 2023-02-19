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
  "io.circe" %% "circe-generic" % "0.14.3"

//  "org.typelevel" %% "cats-effect" % "3.5-6581dc4",
//  "org.http4s" %% "http4s-ember-client" % http4sVersion,
//  "org.http4s" %% "http4s-ember-server" % http4sVersion,
//
//
//  // Optional for auto-derivation of JSON codecs
//
//  // Optional for string interpolation to JSON model
//  "io.circe" %% "circe-literal" % "0.14.3",
//  "org.tpolecat" %% "doobie-core" % "1.0.0-RC2",
//
//  // for core classes and traits, e.g. `Client[F]`
//  "org.http4s" %% "http4s-core" % http4sVersion,
//  "org.http4s" %% "http4s-client" % http4sVersion,
//  "org.http4s" %% "http4s-server" % http4sVersion,
//  "org.http4s" %% "http4s-ember-server" % "0.23.18",
//  "org.http4s" %% "http4s-ember-client" % "0.23.18"
)