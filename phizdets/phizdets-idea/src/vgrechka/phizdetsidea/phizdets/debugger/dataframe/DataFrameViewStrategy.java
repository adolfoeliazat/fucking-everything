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
package vgrechka.phizdetsidea.phizdets.debugger.dataframe;

import vgrechka.phizdetsidea.phizdets.debugger.ArrayChunk;
import vgrechka.phizdetsidea.phizdets.debugger.PyDebugValue;
import vgrechka.phizdetsidea.phizdets.debugger.array.AsyncArrayTableModel;
import vgrechka.phizdetsidea.phizdets.debugger.containerview.ColoredCellRenderer;
import vgrechka.phizdetsidea.phizdets.debugger.containerview.DataViewStrategy;
import vgrechka.phizdetsidea.phizdets.debugger.containerview.PyDataViewerPanel;
import org.jetbrains.annotations.NotNull;

public class DataFrameViewStrategy extends DataViewStrategy {
  private static final String DATA_FRAME = "DataFrame";

  public AsyncArrayTableModel createTableModel(int rowCount, int columnCount, @NotNull PyDataViewerPanel dataProvider, @NotNull PyDebugValue debugValue) {
    return new DataFrameTableModel(rowCount, columnCount, dataProvider, debugValue, this);
  }

  @Override
  public ColoredCellRenderer createCellRenderer(double minValue, double maxValue, @NotNull ArrayChunk arrayChunk) {
    return new DataFrameTableCellRenderer();
  }

  @Override
  public boolean isNumeric(String dtypeKind) {
    return true;
  }

  @NotNull
  @Override
  public String getTypeName() {
    return DATA_FRAME;
  }
}
