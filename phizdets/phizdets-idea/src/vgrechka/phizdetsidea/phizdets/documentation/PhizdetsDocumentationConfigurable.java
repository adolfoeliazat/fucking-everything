/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.documentation;

import com.google.common.collect.Sets;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.AddEditRemovePanel;
import com.intellij.ui.ColoredTableCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.util.PlatformUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author yole
 */
public class PhizdetsDocumentationConfigurable implements SearchableConfigurable, Configurable.NoScroll {
  public static final String ID = "vgrechka.phizdetsidea.phizdets.documentation.PhizdetsDocumentationConfigurable";
  private PhizdetsDocumentationPanel myPanel = new PhizdetsDocumentationPanel();

  @NotNull
  @Override
  public String getId() {
    return ID;
  }

  @Nls
  @Override
  public String getDisplayName() {
    return PlatformUtils.isPyCharm() ? "External Documentation" : "Phizdets External Documentation";
  }

  @Override
  public String getHelpTopic() {
    return "preferences.ExternalDocumentation";
  }

  @Override
  public JComponent createComponent() {
    return myPanel;
  }

  @Override
  public void reset() {
    myPanel.getData().clear();
    myPanel.getData().addAll(PhizdetsDocumentationMap.getInstance().getEntries());
  }

  @Override
  public boolean isModified() {
    HashSet<PhizdetsDocumentationMap.Entry> originalEntries = Sets.newHashSet(PhizdetsDocumentationMap.getInstance().getEntries());
    HashSet<PhizdetsDocumentationMap.Entry> editedEntries = Sets.newHashSet(myPanel.getData());
    return !editedEntries.equals(originalEntries);
  }

  @Override
  public void apply() throws ConfigurationException {
    PhizdetsDocumentationMap.getInstance().setEntries(myPanel.getData());
  }

  private static class PhizdetsDocumentationTableModel extends AddEditRemovePanel.TableModel<PhizdetsDocumentationMap.Entry> {
    @Override
    public int getColumnCount() {
      return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
      return columnIndex == 0 ? "Module Name" : "URL/Path Pattern";
    }

    @Override
    public Object getField(PhizdetsDocumentationMap.Entry o, int columnIndex) {
      return columnIndex == 0 ? o.getPrefix() : o.getUrlPattern();
    }
  }

  private static final PhizdetsDocumentationTableModel ourModel = new PhizdetsDocumentationTableModel();

  private static class PhizdetsDocumentationPanel extends AddEditRemovePanel<PhizdetsDocumentationMap.Entry> {
    public PhizdetsDocumentationPanel() {
      super(ourModel, new ArrayList<>());
      setRenderer(1, new ColoredTableCellRenderer() {
        @Override
        protected void customizeCellRenderer(JTable table, Object value, boolean selected, boolean hasFocus, int row, int column) {
          String text = value == null ? "" : (String) value;
          int pos = 0;
          while(pos < text.length()) {
            int openBrace = text.indexOf('{', pos);
            if (openBrace == -1) openBrace = text.length();
            append(text.substring(pos, openBrace));
            int closeBrace = text.indexOf('}', openBrace);
            if (closeBrace == -1)
              closeBrace = text.length();
            else
              closeBrace++;
            append(text.substring(openBrace, closeBrace), new SimpleTextAttributes(SimpleTextAttributes.STYLE_BOLD, JBColor.BLUE.darker()));
            pos = closeBrace;
          }
        }
      });
    }

    @Override
    protected PhizdetsDocumentationMap.Entry addItem() {
      return showEditor(null);
    }

    @Nullable
    private PhizdetsDocumentationMap.Entry showEditor(PhizdetsDocumentationMap.Entry entry) {
      PhizdetsDocumentationEntryEditor editor = new PhizdetsDocumentationEntryEditor(this);
      if (entry != null) {
        editor.setEntry(entry);
      }
      editor.show();
      if (editor.getExitCode() != DialogWrapper.OK_EXIT_CODE) {
        return null;
      }
      return editor.getEntry();
    }

    @Override
    protected boolean removeItem(PhizdetsDocumentationMap.Entry o) {
      return true;
    }

    @Override
    protected PhizdetsDocumentationMap.Entry editItem(PhizdetsDocumentationMap.Entry o) {
      return showEditor(o);
    }
  }

}
