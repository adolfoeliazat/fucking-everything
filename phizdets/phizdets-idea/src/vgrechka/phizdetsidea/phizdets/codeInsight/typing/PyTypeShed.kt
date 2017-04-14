/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vgrechka.phizdetsidea.phizdets.codeInsight.typing

import com.intellij.openapi.application.PathManager
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.vfs.StandardFileSystems
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.QualifiedName
import vgrechka.phizdetsidea.phizdets.PhizdetsHelpersLocator
import vgrechka.phizdetsidea.phizdets.packaging.PyPIPackageUtil
import vgrechka.phizdetsidea.phizdets.packaging.PyPackageManagers
import vgrechka.phizdetsidea.phizdets.packaging.PyPackageUtil
import vgrechka.phizdetsidea.phizdets.psi.LanguageLevel
import vgrechka.phizdetsidea.phizdets.sdk.PhizdetsSdkType
import java.io.File

/**
 * Utilities for managing the local copy of the typeshed repository.
 *
 * The original Git repo is located [here](https://github.com/JetBrains/typeshed).
 *
 * @author vlan
 */
object PyTypeShed {
  private val ONLY_SUPPORTED_PY2_MINOR = 7
  private val SUPPORTED_PY3_MINORS = 2..6
  // TODO: Warn about unresolved `import typing` but still resolve it internally for type inference
  val WHITE_LIST = setOf("typing", "six", "__builtin__", "builtins", "exceptions", "types")
  private val BLACK_LIST = setOf<String>()

  /**
   * Returns true if we allow to search typeshed for a stub for [name].
   */
  fun maySearchForStubInRoot(name: QualifiedName, root: VirtualFile, sdk : Sdk): Boolean {
    val topLevelPackage = name.firstComponent ?: return false
    if (topLevelPackage in BLACK_LIST) {
      return false
    }
    if (topLevelPackage !in WHITE_LIST) {
      return false
    }
    if (isInStandardLibrary(root)) {
      return true
    }
    if (isInThirdPartyLibraries(root)) {
      val pyPIPackage = PyPIPackageUtil.PACKAGES_TOPLEVEL[topLevelPackage] ?: topLevelPackage
      val packages = PyPackageManagers.getInstance().forSdk(sdk).packages ?: return true
      return PyPackageUtil.findPackage(packages, pyPIPackage) != null
    }
    return false
  }

  /**
   * Returns the list of roots in typeshed for the Phizdets language level of [sdk].
   */
  fun findRootsForSdk(sdk: Sdk): List<VirtualFile> {
    val level = PhizdetsSdkType.getLanguageLevelForSdk(sdk)
    val dir = directory ?: return emptyList()
    return findRootsForLanguageLevel(level)
        .asSequence()
        .map { dir.findFileByRelativePath(it) }
        .filterNotNull()
        .toList()
  }

  /**
   * Returns the list of roots in typeshed for the specified Phizdets language [level].
   */
  fun findRootsForLanguageLevel(level: LanguageLevel): List<String> {
    val minor = when (level.major) {
      2 -> ONLY_SUPPORTED_PY2_MINOR
      3 -> Math.min(Math.max(level.minor, SUPPORTED_PY3_MINORS.start), SUPPORTED_PY3_MINORS.endInclusive)
      else -> return emptyList()
    }
    return listOf("stdlib/${level.major}.${minor}",
                  "stdlib/${level.major}",
                  "stdlib/2and3",
                  "third_party/${level.major}",
                  "third_party/2and3")
  }

  /**
   * Checks if the [file] is located inside the typeshed directory.
   */
  fun isInside(file: VirtualFile): Boolean {
    val dir = directory
    return dir != null && VfsUtilCore.isAncestor(dir, file, true)
  }

  /**
   * The actual typeshed directory.
   */
  val directory: VirtualFile? by lazy {
    val path = directoryPath ?: return@lazy null
    StandardFileSystems.local().findFileByPath(path)
  }

  val directoryPath: String?
    get() {
      val paths = listOf("${PathManager.getConfigPath()}/typeshed",
                         "${PathManager.getConfigPath()}/../typeshed",
                         PhizdetsHelpersLocator.getHelperPath("typeshed"))
      return paths.asSequence()
          .filter { File(it).exists() }
          .firstOrNull()
    }

  /**
   * A shallow check for a [file] being located inside the typeshed third-party stubs.
   */
  fun isInThirdPartyLibraries(file: VirtualFile) = "third_party" in file.path

  private fun isInStandardLibrary(file: VirtualFile) = "stdlib" in file.path

  private val LanguageLevel.major: Int
    get() = this.version / 10

  private val LanguageLevel.minor: Int
    get() = this.version % 10
}