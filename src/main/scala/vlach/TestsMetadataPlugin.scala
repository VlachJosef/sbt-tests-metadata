package vlach

import sbt._
import Keys._
import sbt.internal.inc._
import sjsonnew.{Builder, JsonWriter}
import sjsonnew.BasicJsonProtocol._
import xsbti.compile.CompileAnalysis
import xsbti.VirtualFileRef

object TestsMetadataPlugin extends AutoPlugin {

  case class TestData(test: String, sourceFile: String)
  case class ProjectMetadata(
      project: String,
      base: String,
      definedTest: Seq[TestData],
      sourceDirectories: Seq[String]
  )
  case class TestsMetadata(baseDirectory: String, projects: Seq[ProjectMetadata])

  object TestsMetadata {
    val empty = TestsMetadata("", List.empty[ProjectMetadata])
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

  override def trigger = allRequirements

  object autoImport {
    val testsMetadata        = settingKey[TestsMetadata]("Tests metadata.")
    val testsMetadataRefresh = taskKey[StateTransform]("Refresh tests metadata information.")
  }

  import autoImport._

  override lazy val globalSettings = Seq(
    testsMetadata := TestsMetadata((ThisBuild / baseDirectory).value.getAbsolutePath, List.empty),
    testsMetadataRefresh := StateTransform { state =>
      val extracted: Extracted = Project.extract(state)

      val structure = extracted.structure

      val projectsMetadata: List[ProjectMetadata] =
        structure.allProjectRefs.foldLeft(List.empty[ProjectMetadata]) { case (acc, projectRef) =>
          val testToSource: Map[String, String] =
            Project.runTask(projectRef / Test / compile, state) match {
              case None                       => Map.empty[String, String]
              case Some((newState, Inc(inc))) => Map.empty[String, String]
              case Some((newState, Value(analysis))) =>
                analysis match {
                  case analysis: Analysis =>
                    val relations: Traversable[(VirtualFileRef, String)] = analysis.relations.classes.all
                    relations.map { case (virtualFileRef, string) => string -> virtualFileRef.id }.toMap
                }
            }

          val projectSourceDirectories = (Test / sourceDirectories in projectRef)
            .get(structure.data)
            .fold(Seq.empty[String])(_.map(_.getAbsolutePath))

          def toTestData(testDefinition: TestDefinition): TestData =
            TestData(testDefinition.name, testToSource.getOrElse(testDefinition.name, ""))

          Project.runTask(projectRef / Test / definedTests, state) match {
            case None                       => acc
            case Some((newState, Inc(inc))) => acc
            case Some((newState, Value(v))) =>
              val base = Project.getProject(projectRef, structure).fold("")(_.base.getAbsolutePath)
              acc :+ ProjectMetadata(projectRef.project, base, v.map(toTestData), projectSourceDirectories)
          }
        }

      val globalTestsMetadata: SettingKey[TestsMetadata] = Global / testsMetadata

      extracted.appendWithoutSession(
        Seq(globalTestsMetadata := globalTestsMetadata.value.copy(projects = projectsMetadata)),
        state
      )
    }
  )
}
