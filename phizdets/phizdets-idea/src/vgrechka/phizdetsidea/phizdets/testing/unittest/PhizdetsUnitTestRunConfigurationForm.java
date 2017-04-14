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
package vgrechka.phizdetsidea.phizdets.testing.unittest;

import com.intellij.openapi.project.Project;
import vgrechka.phizdetsidea.phizdets.PyBundle;
import vgrechka.phizdetsidea.phizdets.testing.AbstractPhizdetsLegacyTestRunConfiguration;
import vgrechka.phizdetsidea.phizdets.testing.AbstractPhizdetsTestRunConfigurationParams;
import vgrechka.phizdetsidea.phizdets.testing.PhizdetsTestRunConfigurationForm;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Leonid Shalupov
 */
public class PhizdetsUnitTestRunConfigurationForm implements PhizdetsUnitTestRunConfigurationParams {
  private JPanel myRootPanel;
  private JCheckBox myIsPureUnittest;

  private PhizdetsTestRunConfigurationForm myTestRunConfigurationForm;


  public PhizdetsUnitTestRunConfigurationForm(final Project project, final PhizdetsUnitTestRunConfiguration configuration) {
    myRootPanel = new JPanel(new BorderLayout());
    myTestRunConfigurationForm = new PhizdetsTestRunConfigurationForm(project, configuration);
    myIsPureUnittest = new JCheckBox("Inspect only subclasses of unittest.TestCase");
    myIsPureUnittest.setSelected(configuration.isPureUnittest());

    final ActionListener testTypeListener = new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        myIsPureUnittest.setVisible(myTestRunConfigurationForm.getTestType() != AbstractPhizdetsLegacyTestRunConfiguration.TestType.TEST_FUNCTION);
      }
    };
    myTestRunConfigurationForm.addTestTypeListener(testTypeListener);

    myIsPureUnittest.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        configuration.setPureUnittest(myIsPureUnittest.isSelected());
      }
    });
    myTestRunConfigurationForm.getAdditionalPanel().add(myIsPureUnittest);
    TitledBorder border = (TitledBorder)myTestRunConfigurationForm.getTestsPanel().getBorder();
    border.setTitle(PyBundle.message("runcfg.unittest.display_name"));
    myTestRunConfigurationForm.setParamsVisible();
    myTestRunConfigurationForm.getParamCheckBox().setSelected(configuration.useParam());

    myRootPanel.add(myTestRunConfigurationForm.getPanel(), BorderLayout.CENTER);
  }

  @Override
  public AbstractPhizdetsTestRunConfigurationParams getTestRunConfigurationParams() {
    return myTestRunConfigurationForm;
  }

  @Override
  public boolean isPureUnittest() {
    return myIsPureUnittest.isSelected();
  }

  @Override
  public void setPureUnittest(boolean isPureUnittest) {
    myIsPureUnittest.setSelected(isPureUnittest);
  }

  public String getParams() {
    return myTestRunConfigurationForm.getParams();
  }

  public void setParams(String params) {
    myTestRunConfigurationForm.setParams(params);
  }

  @Override
  public boolean useParam() {
    return myTestRunConfigurationForm.getParamCheckBox().isSelected();
  }

  @Override
  public void useParam(boolean useParam) {
    myTestRunConfigurationForm.getParamCheckBox().setSelected(useParam);
  }

  public JComponent getPanel() {
    return myRootPanel;
  }
}


