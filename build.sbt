name := "toyka"

version := "0.1"

scalaVersion := "2.13.2"

lazy val root = (project in file("."))
  .aggregate(core, example, protocol, client, server)

lazy val protocol = project in file("protocol")

lazy val core = (project in file("core")).settings(
  libraryDependencies ++= Seq(
    "org.slf4j" % "slf4j-api" % "1.7.30"
  )
).dependsOn(protocol)

lazy val client = (project in file("client"))
  .dependsOn(core)

lazy val server = (project in file("server"))
  .dependsOn(core)

lazy val example = project in file("example")
