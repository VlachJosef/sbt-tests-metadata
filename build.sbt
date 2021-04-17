ThisBuild / version := "0.2.0-SNAPSHOT"

ThisBuild / organization := "io.github.vlachjosef"
ThisBuild / organizationName := "vlachjosef"
ThisBuild / organizationHomepage := Some(url("https://github.com/VlachJosef"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/VlachJosef/sbt-tests-metadata"),
    "scm:git@github.com:VlachJosef/sbt-tests-metadata.git"
  )
)
ThisBuild / developers := List(
  Developer(
    id    = "vlach.josef",
    name  = "Josef Vlach",
    email = "vlach.josef@gmail.com",
    url   = url("https://github.com/VlachJosef")
  )
)

ThisBuild / description := "SBT plugin to expose SBT tests metadata for external test runners."
ThisBuild / licenses := List("Apache 2" -> new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / homepage := Some(url("https://github.com/VlachJosef/sbt-tests-metadata"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}
ThisBuild / publishMavenStyle := true

lazy val root = (project in file("."))
  .settings(
    sbtPlugin := true,
    sbtVersion := "1.4.9",
    name := "sbt-tests-metadata"
  )
