import java.nio.file.Path

import sbt.IO._
import sbt._
import sbt.complete.Parser

object RenameProject {

  val renameArgsParser: Parser[RenameCommand] = {
    import sbt.complete.DefaultParsers._
    (Space ~> NotSpace ~ Space ~ NotSpace).map {
      case ((pkg, _), proj) => RenameCommand(pkg, proj)
    }
  }

  case class RenameCommand(packageName: String, projectName: String)

  def removeRegexes(regexes: Traversable[String])(sourceString: String, file: File) = {
    regexes.foldLeft(sourceString)((currentString, regex) => {
      currentString.replaceAll(regex, "")
    })
  }

  def notHiddenAndExcluding(excludedNames: Seq[String]) =
    new FileFilter {
      override def accept(file: File): Boolean = {
        !(excludedNames.contains(file.getName) ||
          file.isHidden ||
          file.getName.endsWith(".class") ||
          file.getName.endsWith(".gif") ||
          file.getName.endsWith(".png"))
      }
    }

  def updateDirContent(root: File, excludes: Seq[String], updateFun: (String, File) => String, log: Logger) {
    val dirContent = listFiles(notHiddenAndExcluding(excludes))(root)
    for (file <- dirContent) {
      if (file.isDirectory)
        updateDirContent(file, excludes, updateFun, log)
      else
        updateFileContent(updateFun, log, file)
    }
  }

  def updateFileContent(updateFun: (String, File) => String, log: Logger, file: File) = {
    val content = read(file)
    val updatedContent = updateFun(content, file)
    if (!content.eq(updatedContent)) {
      log.info(s"Writing updated ${file.getPath}")
      write(file, updatedContent.getBytes)
    }
  }

  def replacePhrases(replacements: Seq[(String, String)])(source: String, file: File) = {
    replacements.foldLeft(source)((currentSource, replacementPair) => {
      val (from, to) = replacementPair
      if (currentSource.indexOf(from) > 0)
        currentSource.replaceAllLiterally(from, to)
      else
        currentSource
    })
  }

  def moveSources(baseDir: File, initialPackage: String, newPackage: String, log: Logger) {
    def appendSubDirs(initialPath: Path, subdirectories: Seq[String]) =
      subdirectories.foldLeft(initialPath)((path, subDir) => path.resolve(subDir))

    val scalaRoots = List(List("src", "main", "scala"), List("src", "test", "scala"))
    val srcPackageDirStrs = initialPackage.split('.')
    val dstPackageDirStrs = newPackage.split('.')
    val projectSubDirs = ((baseDir * "*") filter { file => !file.isHidden && file.isDirectory }).get
    for {
      projectSubDir <- projectSubDirs
      scalaRoot <- scalaRoots
    } {
      val moduleBaseDir = projectSubDir.toPath
      val srcScalaDir = appendSubDirs(moduleBaseDir, scalaRoot)
      val srcPackageDir = appendSubDirs(srcScalaDir, srcPackageDirStrs).toFile
      val dstPackageDir = appendSubDirs(srcScalaDir, dstPackageDirStrs).toFile
      if (srcPackageDir.exists()) {
        log.info(s"Moving $srcPackageDir to $dstPackageDir")
        createDirectory(dstPackageDir)
        move(srcPackageDir, dstPackageDir)
      }
    }
  }
}

