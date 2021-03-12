package vlach

import java.io.File
import sbt._
import Keys._
import sbt.internal.inc.Analysis

object TestsMetadataPlugin extends AutoPlugin with TestsMetadataCommon {

  def classToPathMapping(analysis: Analysis): Map[String, String] = {
    val relations: Traversable[(File, String)] = analysis.relations.classes.all
    relations.map { case (virtualFileRef, classFqn) =>
      classFqn -> virtualFileRef.getAbsolutePath
    }.toMap
  }

  import autoImport._

  override lazy val globalSettings = Seq(
    testsMetadata := TestsMetadata((ThisBuild / baseDirectory).value.getAbsolutePath, List.empty),
    testsMetadataRefresh := new StateTransform(updateTestsMetadata(state.value, Global / testsMetadata))
  )
}
