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
  case class ProjectData(
      project: String,
      base: String,
      definedTest: Seq[TestData],
      sourceDirectories: Seq[String]
  )
  case class TestsMetadata(baseDirectory: String, projects: Seq[ProjectData])

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

  //override def requires = sbt.plugins.JvmPlugin
  override def trigger = allRequirements

  object autoImport {
    val testRunnerData =
      settingKey[TestsMetadata]("Tests files information.").withRank(KeyRanks.Invisible)
  }
  import autoImport._

  override lazy val globalSettings = Seq(
    commands += Command.command("pipa")(refreshTestRunnerData),
    testRunnerData := TestsMetadata(
      (ThisBuild / baseDirectory).value.getAbsolutePath,
      List.empty[ProjectData]
    )
  )

  val refreshTestRunnerData: State => State = s => {

    val extracted: Extracted = Project.extract(s)

    val structure = extracted.structure

    val newTestRunnerData: List[ProjectData] =
      structure.allProjectRefs.foldLeft(List.empty[ProjectData]) { case (acc, projectRef) =>
        val base = Project.getProject(projectRef, structure).fold("")(_.base.getAbsolutePath)

        val testDefinitionForProject: Option[(State, Result[Seq[TestDefinition]])] =
          Project.runTask(projectRef / Test / definedTests, s)
        val analysisO: Option[(State, Result[CompileAnalysis])] =
          Project.runTask(projectRef / Test / compile, s)

        val testToSource: Map[String, String] =
          analysisO match {
            case None                       => Map.empty[String, String]
            case Some((newState, Inc(inc))) => Map.empty[String, String]
            case Some((newState, Value(analysis))) =>
              analysis match {
                case analysis: Analysis =>
                  val relations: Traversable[(VirtualFileRef, String)] =
                    analysis.relations.classes.all
                  relations.map { case (virtualFileRef, string) =>
                    string -> virtualFileRef.id
                  }.toMap
              }
          }

        val projectSourceDirectories = (Test / sourceDirectories in projectRef)
          .get(structure.data)
          .fold(Seq.empty[String])(_.map(_.getAbsolutePath))

        def toTestData(testDefinition: TestDefinition): TestData =
          TestData(testDefinition.name, testToSource.getOrElse(testDefinition.name, ""))

        testDefinitionForProject match {
          case None => acc // Key wasn't defined.
          case Some((newState, Inc(inc))) =>
            acc // error detail, inc is of type Incomplete, use Incomplete.show(inc.tpe) to get an error message
          case Some((newState, Value(v))) =>
            acc :+ ProjectData(
              projectRef.project,
              base,
              v.map(toTestData),
              projectSourceDirectories
            )
        }
      }

    val newState = extracted.appendWithoutSession(
      Seq(
        Global / testRunnerData := (Global / testRunnerData).value
          .copy(projects = newTestRunnerData)
      ),
      s
    )
    newState
  }
}
