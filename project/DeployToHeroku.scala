import java.io.File

import com.heroku.sdk.deploy.App
import sbt.Keys._
import sbt.{Logger, _}

import scala.collection.JavaConversions

/**
 * Simple task to deploy a fat-jar to Heroku.
 * Code adapted for sbt-assembly from https://github.com/heroku/sbt-heroku
 *
 * The heroku toolbelt must be installed, and the application must be created and configured in the current
 * source root. Assumes that the task is run from a subproject.
 */
object DeployToHeroku {
  val deployToHeroku = taskKey[Unit]("Deploy to Heroku.")

  val settings = Seq(
    deployToHeroku := {
      val log = streams.value.log
      val assemblyFile = sbtassembly.AssemblyKeys.assembly.value
      log.info(s"Deploying $assemblyFile to Heroku")

      // the base directory must contain .git to read the project name
      // assuming that we are running in a subproject
      val baseDir = baseDirectory.value / ".."

      val includedFiles = java.util.Arrays.asList(assemblyFile)
      val processTypes = JavaConversions.mapAsJavaMap(Map("web" -> "java $JAVA_OPTS -jar bootzooka.jar"))
      new DeployToHeroku("sbt-heroku", baseDir, target.value, java.util.Collections.emptyList(), streams.value.log)
        .deploy(includedFiles, java.util.Collections.emptyMap[String, String](), "", "cedar-14", processTypes, "slug.tgz")
    }
  )
}

class DeployToHeroku(buildPackDesc:String, rootDir:File, targetDir:File, buildpacks:java.util.List[String], log:Logger)
  extends App(buildPackDesc, null, rootDir, targetDir, buildpacks) {

  private var percentUpload = 0

  override def logDebug(message:String) {
    log.debug(message)
  }

  override def logInfo(message:String) {
    log.info(message)
  }

  override def logWarn(message:String) {
    log.warn(message)
  }

  override def logUploadProgress(uploaded:java.lang.Long, contentLength:java.lang.Long) {
    val newPercent = Math.round((uploaded / contentLength.toFloat) * 100)
    if (percentUpload != newPercent) {
      percentUpload = newPercent
      log.info("\u001B[A\r\u001B[2K" + s"[${Level.Info.toString}] -----> Uploading slug... ($percentUpload%)")
    }
  }

  override def isUploadProgressEnabled: java.lang.Boolean = {
    ConsoleLogger.formatEnabled && !("false" == System.getProperty("heroku.log.format"))
  }
}