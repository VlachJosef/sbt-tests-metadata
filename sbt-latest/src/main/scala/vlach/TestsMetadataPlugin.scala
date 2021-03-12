package vlach

import sbt._
import Keys._
import sbt.internal.inc.Analysis
import xsbti.VirtualFileRef

object TestsMetadataPlugin extends AutoPlugin with TestsMetadataCommon {

  def classToPathMapping(analysis: Analysis): Map[String, String] = {
    val relations: Traversable[(VirtualFileRef, String)] = analysis.relations.classes.all
    relations.map { case (virtualFileRef, classFqn) =>
      classFqn -> virtualFileRef.id
    }.toMap
  }

  import autoImport._

  override lazy val globalSettings = Seq(
    testsMetadata := TestsMetadata((ThisBuild / baseDirectory).value.getAbsolutePath, List.empty),
    testsMetadataRefresh := StateTransform(state => updateTestsMetadata(state, Global / testsMetadata))
  )
}
