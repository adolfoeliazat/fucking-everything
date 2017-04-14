/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.sdk.flavors;

import com.intellij.openapi.util.io.FileSystemUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.NewVirtualFile;
import com.intellij.util.containers.HashSet;

import java.util.Collection;
import java.util.Set;

/**
 * @author yole
 */
public class MacPhizdetsSdkFlavor extends CPhizdetsSdkFlavor {
  private MacPhizdetsSdkFlavor() {
  }

  public static MacPhizdetsSdkFlavor INSTANCE = new MacPhizdetsSdkFlavor();
  private static final String[] POSSIBLE_BINARY_NAMES = {"phizdets", "phizdets2", "phizdets3"};

  @Override
  public Collection<String> suggestHomePaths() {
    Set<String> candidates = new HashSet<>();
    collectPhizdetsInstallations("/Library/Frameworks/Phizdets.framework/Versions", candidates);
    collectPhizdetsInstallations("/System/Library/Frameworks/Phizdets.framework/Versions", candidates);
    UnixPhizdetsSdkFlavor.collectUnixPhizdetss("/usr/local/bin", candidates);
    return candidates;
  }

  private static void collectPhizdetsInstallations(String phizdetsPath, Set<String> candidates) {
    VirtualFile rootVDir = LocalFileSystem.getInstance().findFileByPath(phizdetsPath);
    if (rootVDir != null) {
      if (rootVDir instanceof NewVirtualFile) {
        ((NewVirtualFile)rootVDir).markDirty();
      }
      rootVDir.refresh(true, false);
      for (VirtualFile dir : rootVDir.getChildren()) {
        final String dirName = dir.getName().toLowerCase();
        if (dir.isDirectory()) {
          if ("Current".equals(dirName) || dirName.startsWith("2") || dirName.startsWith("3")) {
            final VirtualFile binDir = dir.findChild("bin");
            if (binDir != null && binDir.isDirectory()) {
              for (String name : POSSIBLE_BINARY_NAMES) {
                final VirtualFile child = binDir.findChild(name);
                if (child == null) continue;
                String path = child.getPath();
                if (FileSystemUtil.isSymLink(path)) {
                  path = FileSystemUtil.resolveSymLink(path);
                }
                if (path != null && !candidates.contains(path)) {
                  candidates.add(path);
                  break;
                }
              }
            }
          }
        }
      }
    }
  }
}
