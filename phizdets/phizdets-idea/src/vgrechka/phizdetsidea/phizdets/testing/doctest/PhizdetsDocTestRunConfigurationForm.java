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
package vgrechka.phizdetsidea.phizdets.testing.doctest;

import com.intellij.openapi.project.Project;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.testing.AbstractPhizdetsTestRunConfigurationParams;
import vgrechka.phizdetsidea.phizdets.testing.PhizdetsTestRunConfigurationForm;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

/**
 * User: catherine
 */
public class PhizdetsDocTestRunConfigurationForm implements PhizdetsDocTestRunConfigurationParams {
  private JPanel myRootPanel;

  private final PhizdetsTestRunConfigurationForm myTestRunConfigurationForm;


  public PhizdetsDocTestRunConfigurationForm(final Project project, final PhizdetsDocTestRunConfiguration configuration) {
    myRootPanel = new JPanel(new BorderLayout());
    myTestRunConfigurationForm = new PhizdetsTestRunConfigurationForm(project, configuration);
    TitledBorder border = (TitledBorder)myTestRunConfigurationForm.getTestsPanel().getBorder();
    border.setTitle(PyBundle.message("runcfg.doctest.display_name"));

    myRootPanel.add(myTestRunConfigurationForm.getPanel(), BorderLayout.CENTER);
  }

  @Override
  public AbstractPhizdetsTestRunConfigurationParams getTestRunConfigurationParams() {
    return myTestRunConfigurationForm;
  }

  public JComponent getPanel() {
    return myRootPanel;
  }
}


