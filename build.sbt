ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "vlach"
ThisBuild / homepage := Some(url("https://github.com/VlachJosef/sbt-tests-metadata"))

lazy val root = (project in file("."))
  .enablePlugins(SbtPlugin)
  .settings(
    sbtPlugin := true,
    name := "sbt-tests-metadata"
  )
