import sbt._
import RenameProject._
import sbt.IO._
val renameProject = inputKey[Unit]("Renames the project")

renameProject := {
  val cmd = renameArgsParser.parsed
  val initialName = name.value
  val log = streams.value.log
  val initialRootPackage = "com.softwaremill.bootzooka"
  val targetRootPackage = cmd.packageName + "." + cmd.projectName.toLowerCase
  val excludes = List("README.md", "RenameProject.scala", "rename.sbt", "out")
  log.info("Cleaning all projects...")
  log.info("Removing scaffolding in HTML elements...")
  val baseDir: File = baseDirectory.value
  RenameProject.updateDirContent(baseDir, excludes, removeRegexes(List(
    """(?s)<li id='scaffolding.*?li>""",
    """(?s)<span id='scaffolding.*?span>""")), log)
  log.info(s"Replacing project name and package name")
  updateDirContent(baseDir, excludes, replacePhrases(List(
    (initialRootPackage, targetRootPackage),
    (initialName, cmd.projectName.toLowerCase),
    (initialName.capitalize, cmd.projectName.toLowerCase.capitalize)
  )), log)
  log.info(s"Moving classes to new packages")
  moveSources(baseDir, initialRootPackage, targetRootPackage, log)
  log.info(s"Removing unnecessary files")
  delete(baseDir / "activator.properties")
  delete(baseDir / "CHANGELOG.md")
  delete(baseDir / "README.md")
  log.info("Done! If you changed your mind -> run `git reset --hard HEAD; git clean -d -f` to restore state before renaming")
}

