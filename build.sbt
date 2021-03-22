ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "io.github.vlachjosef"
ThisBuild / homepage := Some(url("https://github.com/VlachJosef/sbt-tests-metadata"))

lazy val root = (project in file("."))
  .settings(
    sbtPlugin := true,
    sbtVersion := "1.4.9",
    name := "sbt-tests-metadata"
  )
