import java.nio.file.Path

import sbt._
import sbt.IO._
import sbt.Keys._
import sbt.complete.Parser

object RenameProject {

  val renameArgsParser: Parser[RenameCommand] = {
    import sbt.complete.DefaultParsers._
    (Space ~>
      token(NotSpace, "top level package, for example com.softwaremill") ~
      Space ~
      token(NotSpace, "project name, single word like bootzooka")).map { case ((pkg, _), proj) =>
      RenameCommand(pkg, proj)
    }
  }

  case class RenameCommand(inputPackageName: String, inputProjectName: String) {
    def packageName = inputPackageName.toLowerCase
    def projectName = inputProjectName.toLowerCase
  }

  def renameAction(state: State, cmd: RenameCommand): State =
    "clean" ::
      s"doRename ${cmd.packageName} ${cmd.projectName}" ::
      "reload" ::
      state

  val renameHelp = Help(
    Seq(
      "renameProject <package> <name>" -> "Replace default name and root package name with your own"
    ),
    Map(
      "renameProject" ->
        """|Override default name and root package name with your own. Parameters:
        |<packageName> - top level package like com.softwaremill
        |<projectName> - project name, a single word like bootzooka
      """.stripMargin
    )
  )

  val doRename = inputKey[Unit]("Renames the project")

  val settings = Seq(
    doRename := {

      def info(msg: String) = streams.value.log.info(msg)

      def removeRegexes(regexes: Traversable[String])(sourceString: String, file: File) =
        regexes.foldLeft(sourceString)((currentString, regex) => {
          currentString.replaceAll(regex, "")
        })

      def notHiddenAndExcluding(excludedNames: Seq[String]) =
        new FileFilter {
          override def accept(file: File): Boolean =
            !(excludedNames.contains(file.getName) ||
              file.isHidden ||
              file.getName.endsWith(".class") ||
              file.getName.endsWith(".gif") ||
              file.getName.endsWith(".png"))
        }

      def updateDirContent(root: File, excludes: Seq[String], updateFun: (String, File) => String) {
        val dirContent = listFiles(notHiddenAndExcluding(excludes))(root)
        for (file <- dirContent) {
          if (file.isDirectory)
            updateDirContent(file, excludes, updateFun)
          else
            updateFileContent(updateFun, file)
        }
      }

      def updateFileContent(updateFun: (String, File) => String, file: File) = {
        val content = read(file)
        val updatedContent = updateFun(content, file)
        if (!content.eq(updatedContent)) {
          info(s"Writing updated ${file.getPath}")
          write(file, updatedContent.getBytes)
        }
      }

      def replacePhrases(replacements: Seq[(String, String)])(source: String, file: File) =
        replacements.foldLeft(source)((currentSource, replacementPair) => {
          val (from, to) = replacementPair
          if (currentSource.indexOf(from) != -1)
            currentSource.replaceAllLiterally(from, to)
          else
            currentSource
        })

      def moveSources(baseDir: File, initialPackage: String, newPackage: String) {
        def appendSubDirs(initialPath: Path, subdirectories: Seq[String]) =
          subdirectories.foldLeft(initialPath)((path, subDir) => path.resolve(subDir))

        val scalaRoots = List(List("src", "main", "scala"), List("src", "test", "scala"))
        val srcPackageDirStrs = initialPackage.split('.')
        val dstPackageDirStrs = newPackage.split('.')
        val projectSubDirs = ((baseDir * "*") filter { file =>
          !file.isHidden && file.isDirectory
        }).get
        for {
          projectSubDir <- projectSubDirs
          scalaRoot <- scalaRoots
        } {
          val moduleBaseDir = projectSubDir.toPath
          val srcScalaDir = appendSubDirs(moduleBaseDir, scalaRoot)
          val srcPackageDir = appendSubDirs(srcScalaDir, srcPackageDirStrs).toFile
          val dstPackageDir = appendSubDirs(srcScalaDir, dstPackageDirStrs).toFile
          if (srcPackageDir.exists()) {
            info(s"Moving $srcPackageDir to $dstPackageDir")
            createDirectory(dstPackageDir)
            move(srcPackageDir, dstPackageDir)
          }
        }
      }

      val cmd = renameArgsParser.parsed
      val initialName = name.value
      val initialRootPackage = "com.softwaremill.bootzooka"
      val targetRootPackage = cmd.packageName + "." + cmd.projectName
      val excludes = List("README.md", "RenameProject.scala", "rename.sbt", "out", "node_modules", "target")
      info("Removing scaffolding in HTML elements...")
      val baseDir: File = baseDirectory.value
      updateDirContent(
        baseDir,
        excludes,
        removeRegexes(List("""(?s)<li id='scaffolding.*?li>""", """(?s)<span id='scaffolding.*?span>"""))
      )
      info(s"Replacing project name and package name")
      updateDirContent(
        baseDir,
        excludes,
        replacePhrases(
          List(
            (initialRootPackage, targetRootPackage),
            (initialName, cmd.projectName.toLowerCase),
            (initialName.capitalize, cmd.projectName.toLowerCase.capitalize)
          )
        )
      )
      info(s"Moving classes to new packages")
      moveSources(baseDir, initialRootPackage, targetRootPackage)
      info(s"Removing unnecessary files")
      delete(baseDir / "activator.properties")
      delete(baseDir / "CHANGELOG.md")
      delete(baseDir / "README.md")
      info(
        "Done! If you changed your mind -> run `git reset --hard HEAD; git clean -d -f` to restore state before renaming"
      )
    },
    commands += Command("renameProject", renameHelp)(state => renameArgsParser)(renameAction)
  )
}
