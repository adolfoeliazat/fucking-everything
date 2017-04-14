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
package vgrechka.phizdetsidea.phizdets.sdk;

import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Comparing;
import vgrechka.phizdetsidea.phizdets.sdk.flavors.CPhizdetsSdkFlavor;
import vgrechka.phizdetsidea.phizdets.sdk.flavors.PhizdetsSdkFlavor;

import java.util.Comparator;

/**
* @author yole
*/
public class PreferredSdkComparator implements Comparator<Sdk> {
  public static PreferredSdkComparator INSTANCE = new PreferredSdkComparator();

  @Override
  public int compare(Sdk o1, Sdk o2) {
    final PhizdetsSdkFlavor flavor1 = PhizdetsSdkFlavor.getFlavor(o1);
    final PhizdetsSdkFlavor flavor2 = PhizdetsSdkFlavor.getFlavor(o2);
    int remote1Weight = PySdkUtil.isRemote(o1) ? 0 : 1;
    int remote2Weight = PySdkUtil.isRemote(o2) ? 0 : 1;
    if (remote1Weight != remote2Weight) {
      return remote2Weight - remote1Weight;
    }
    int detectedWeight1 = o1 instanceof PyDetectedSdk ? 0 : 1;
    int detectedWeight2 = o2 instanceof PyDetectedSdk ? 0 : 1;
    if (detectedWeight1 != detectedWeight2) {
      return detectedWeight2 - detectedWeight1;
    }

    int venv1weight = PhizdetsSdkType.isVirtualEnv(o1) ? 0 : 1;
    int venv2weight = PhizdetsSdkType.isVirtualEnv(o2) ? 0 : 1;
    if (venv1weight != venv2weight) {
      return venv2weight - venv1weight;
    }
    int flavor1weight = flavor1 instanceof CPhizdetsSdkFlavor ? 1 : 0;
    int flavor2weight = flavor2 instanceof CPhizdetsSdkFlavor ? 1 : 0;
    if (flavor1weight != flavor2weight) {
      return flavor2weight - flavor1weight;
    }

    return -Comparing.compare(o1.getVersionString(), o2.getVersionString());
  }
}
