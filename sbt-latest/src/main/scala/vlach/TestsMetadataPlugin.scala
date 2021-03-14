package vlach

import sbt.AutoPlugin
import sbt.internal.inc.Analysis
import xsbti.VirtualFileRef

object TestsMetadataPlugin extends AutoPlugin with TestsMetadataCommon {

  def classToPathMapping(analysis: Analysis): Map[String, String] = {
    val relations: Traversable[(VirtualFileRef, String)] = analysis.relations.classes.all
    relations.map { case (virtualFileRef, classFqn) =>
      classFqn -> virtualFileRef.id
    }.toMap
  }
}
