package vgrechka.phizdetsidea.phizdets.debugger;

import com.intellij.xdebugger.frame.XReferrersProvider;
import com.intellij.xdebugger.frame.XValueChildrenList;
import vgrechka.phizdetsidea.phizdets.debugger.pydev.PyDebugCallback;

/**
 * @author traff
 */
public class PyReferrersLoader  {
  private final IPyDebugProcess myProcess;

  public PyReferrersLoader(IPyDebugProcess process) {
    myProcess = process;
  }

  public void loadReferrers(PyReferringObjectsValue value, PyDebugCallback<XValueChildrenList> callback) {
    myProcess.loadReferrers(value, callback);
  }
}
