package vlach

import java.io.File
import sbt.AutoPlugin
import sbt.internal.inc.Analysis

object TestsMetadataPlugin extends AutoPlugin with TestsMetadataCommon {

  def classToPathMapping(analysis: Analysis): Map[String, String] = {
    val relations: Traversable[(File, String)] = analysis.relations.classes.all
    relations.map { case (virtualFileRef, classFqn) =>
      classFqn -> virtualFileRef.getAbsolutePath
    }.toMap
  }
}
