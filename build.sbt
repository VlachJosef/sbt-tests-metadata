ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / organization := "vlach"
ThisBuild / homepage := Some(url("https://github.com/VlachJosef/sbt-tests-metadata"))

lazy val commonSettings = Seq(
  Compile / unmanagedSourceDirectories += file(".").getAbsoluteFile / "shared" / "main" / "scala"
)

lazy val `sbt-latest` = (project in file("sbt-latest"))
  .settings(
    commonSettings,
    sbtPlugin := true,
    sbtVersion := "1.4.9",
    name := "sbt-tests-metadata"
  )

lazy val sbt13 = (project in file("sbt-1.3"))
  .settings(
    commonSettings,
    sbtPlugin := true,
    sbtVersion := "1.3.13",
    name := "sbt-tests-metadata-13"
  )
