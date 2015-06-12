import os
import sys
import shutil
import re

def replacePhrase(fromStr, toStr):
    def doReplace(str):
        occurrences = str.count(fromStr.lower()) + str.count(fromStr.capitalize())
        if occurrences > 0:
            newStr = str.replace(fromStr.lower(), toStr.lower())
            newStr = newStr.replace(fromStr.capitalize(), toStr.capitalize())
            return newStr
        else:
            return str
    return doReplace


def removeRegexes(regexes):
    def doRemove(sourceStr):
        return reduce(lambda currentStr, regex: re.sub(regex, "", currentStr), regexes, sourceStr)
    return doRemove

def updateFileContent(rootPath, excludes, contentUpdateFunction):
    for dname, dirs, files in os.walk(rootPath):
        if not dname.startswith('./.'):
            for fname in files:
                fpath = os.path.join(dname, fname)
                if fname not in excludes and not fname.startswith('.'):
                    with open(fpath) as f:
                        fileContent = f.read()
                        newFileContent = contentUpdateFunction(fileContent)
                        if newFileContent != fileContent:
                            with open(fpath, "w") as f:
                                print 'Updating ' + fname
                                f.write(newFileContent)
    return

def cleanup(scalaModules):
    print 'Cleaning up...'
    shutil.rmtree('./ui/bower_files', True)
    shutil.rmtree('./ui/dist', True)
    shutil.rmtree('./ui/node_modules', True)
    shutil.rmtree('./ui/target', True)
    for moduleName in scalaModules:
        shutil.rmtree('./' + moduleName + '/target', True)
    return

def moveSources(oldPackage, newPackage, scalaModules):
    oldDirTree = oldPackage.replace('.', '/')
    newDirTree = newPackage.replace('.', '/')
    rootDirs = ['/src/main/scala/', '/src/test/scala/']
    for moduleName in scalaModules:
        for rootDir in rootDirs:
            try:
                shutil.move('./' + moduleName + rootDir + oldDirTree, './' + moduleName + rootDir + newDirTree)
            except Exception:
                pass
    return

#######

if len(sys.argv) != 3:
    print 'Illegal number of arguments.\nUsage (Rename Foobar to Foobar using com.mycompany as root package):\n./rename.py com.mycompany foobar'
    exit(-1)


initialName = 'Bootzooka'
initialRootPackage = 'com.softwaremill'
replaceExcludes = ['README.md', 'rename.py']
scalaModules = ['backend','dist','ui-tests']

cleanup(scalaModules)

newName = sys.argv[2]
packageName = sys.argv[1]

print 'Removing scaffolding HTML elements'
updateFileContent('./ui', [], removeRegexes([re.compile(r"<li id='scaffolding.*?li>", re.DOTALL),
                                             re.compile(r"<span id='scaffolding.*?span>", re.DOTALL)]))

print 'Replacing ' + initialName + ' with ' + newName
updateFileContent('./', replaceExcludes, replacePhrase(initialName, newName))

print 'Replacing package references from ' + initialRootPackage + ' to ' + packageName
updateFileContent('./', replaceExcludes, replacePhrase(initialRootPackage, packageName))

print 'Moving classes to new package'
moveSources(initialRootPackage, packageName, scalaModules)

print 'Removing irrelevant files'
os.remove('./activator.properties')
os.remove('./CHANGELOG.md')
os.remove('./README.md')
os.remove('./ui/README.md')

print 'Done! In case you want to roll back, call `git reset --hard HEAD; git clean -f -d`'