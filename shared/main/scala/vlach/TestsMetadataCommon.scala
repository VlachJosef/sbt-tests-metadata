package vlach

import sbt._
import Keys._
import sbt.internal.inc._
import sjsonnew.{Builder, JsonWriter}
import sjsonnew.BasicJsonProtocol._

case class TestData(test: String, sourceFile: String, suite: String)
case class ProjectMetadata(
    project: String,
    base: String,
    definedTest: Seq[TestData],
    sourceDirectories: Seq[String]
)
case class TestsMetadata(baseDirectory: String, projects: Seq[ProjectMetadata])

object TestsMetadata {
  implicit val writer: JsonWriter[TestsMetadata] = new JsonWriter[TestsMetadata] {
    override def write[J](obj: TestsMetadata, builder: Builder[J]): Unit = {
      builder.beginObject()
      builder.addField("baseDirectory", obj.baseDirectory)
      builder.addFieldName("projects")
      builder.beginArray()
      obj.projects.foreach { projectData =>
        builder.beginObject()
        builder.addField("project", projectData.project)
        builder.addField("base", projectData.base)
        builder.addFieldName("definedTests")
        builder.beginArray()
        projectData.definedTest.foreach { definedTest =>
          builder.beginObject()
          builder.addField("test", definedTest.test)
          builder.addField("source", definedTest.sourceFile)
          builder.addField("suite", definedTest.suite)
          builder.endObject()
        }
        builder.endArray()
        builder.addFieldName("sourceDirectories")
        builder.beginArray()
        projectData.sourceDirectories.foreach { sourceDirectory =>
          builder.writeString(sourceDirectory)
        }
        builder.endArray()
        builder.endObject()
      }
      builder.endArray()
      builder.endObject()
    }
  }
}

trait TestsMetadataCommon { this: AutoPlugin =>

  override def trigger = allRequirements

  object autoImport {
    val testsMetadata = settingKey[TestsMetadata]("Tests metadata.")
  }

  import autoImport._

  override lazy val globalSettings = Seq(
    testsMetadata := TestsMetadata((ThisBuild / baseDirectory).value.getAbsolutePath, List.empty),
    commands += testsMetadataRefresh
  )

  def testsMetadataRefresh = Command.command("testsMetadataRefresh") { state =>
    updateTestsMetadata(state)
  }

  def classToPathMapping(analysis: Analysis): Map[String, String]

  def updateTestsMetadata(state: State): State = {

    val extracted: Extracted = Project.extract(state)

    val structure = extracted.structure

    val projectsMetadata: List[ProjectMetadata] =
      structure.allProjectRefs.foldLeft(List.empty[ProjectMetadata]) { case (acc, projectRef) =>
        Project.runTask(projectRef / Test / definedTests, state) match {
          case None                       => acc
          case Some((newState, Inc(inc))) => acc
          case Some((newState, Value(v))) =>
            if (v.isEmpty) acc
            else {
              val testToSource: Map[String, String] =
                Project.runTask(projectRef / Test / compile, state) match {
                  case None                       => Map.empty[String, String]
                  case Some((newState, Inc(inc))) => Map.empty[String, String]
                  case Some((newState, Value(analysis))) =>
                    analysis match {
                      case analysis: Analysis => classToPathMapping(analysis)
                    }
                }

              def toTestData(testDefinition: TestDefinition): TestData = {
                val isMunitSuite = TestFramework.toString(testDefinition.fingerprint).contains("munit.Suite")
                val suite        = if (isMunitSuite) "munit" else "unknown"
                TestData(testDefinition.name, testToSource.getOrElse(testDefinition.name, ""), suite)
              }

              val projectSourceDirectories = (projectRef / Test / sourceDirectories)
                .get(structure.data)
                .fold(Seq.empty[String])(_.map(_.getAbsolutePath))
              val base = Project.getProject(projectRef, structure).fold("")(_.base.getAbsolutePath)
              acc :+ ProjectMetadata(projectRef.project, base, v.map(toTestData), projectSourceDirectories)
            }
        }
      }

    val globalMetadata = Global / testsMetadata

    extracted.appendWithoutSession(
      Seq(
        globalMetadata := globalMetadata.value.copy(projects = projectsMetadata)
      ),
      state
    )
  }
}
