package vgrechka.phizdetsidea.phizdets.debugger;

import com.intellij.xdebugger.XSourcePosition;
import com.intellij.xdebugger.frame.XValueChildrenList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Facade to access phizdets variables frame
 *
 * @author traff
 */
public interface PyFrameAccessor {
  PyDebugValue evaluate(final String expression, final boolean execute, boolean doTrunc) throws PyDebuggerException;

  @Nullable
  XValueChildrenList loadFrame() throws PyDebuggerException;

  XValueChildrenList loadVariable(PyDebugValue var) throws PyDebuggerException;

  void changeVariable(PyDebugValue variable, String expression) throws PyDebuggerException;

  @Nullable
  PyReferrersLoader getReferrersLoader();

  ArrayChunk getArrayItems(PyDebugValue var, int rowOffset, int colOffset, int rows, int cols, String format) throws PyDebuggerException;

  @Nullable
  XSourcePosition getSourcePositionForName(String name, String parentType);

  @Nullable
  XSourcePosition getSourcePositionForType(String type);

  default void showNumericContainer(PyDebugValue value) {}

  default void addFrameListener(@NotNull PyFrameListener listener) {}
}