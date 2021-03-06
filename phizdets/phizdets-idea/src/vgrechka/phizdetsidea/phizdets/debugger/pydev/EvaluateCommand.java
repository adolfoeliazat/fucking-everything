package vgrechka.phizdetsidea.phizdets.debugger.pydev;

import vgrechka.phizdetsidea.phizdets.debugger.IPyDebugProcess;
import vgrechka.phizdetsidea.phizdets.debugger.PyDebugValue;
import vgrechka.phizdetsidea.phizdets.debugger.PyDebuggerException;


public class EvaluateCommand extends AbstractFrameCommand {

  private final String myExpression;
  private final boolean myExecute;
  private final IPyDebugProcess myDebugProcess;
  private final boolean myTrimResult;
  private PyDebugValue myValue = null;
  private String myTempName;


  public EvaluateCommand(final RemoteDebugger debugger, final String threadId, final String frameId, final String expression,
                         final boolean execute, final boolean trimResult) {
    super(debugger, (execute ? EXECUTE : EVALUATE), threadId, frameId);
    myExpression = expression;
    myExecute = execute;
    myDebugProcess = debugger.getDebugProcess();
    myTrimResult = trimResult;
    myTempName = myDebugProcess.canSaveToTemp(expression)? debugger.generateSaveTempName(threadId, frameId): "";
  }

  @Override
  protected void buildPayload(Payload payload) {
    super.buildPayload(payload);
    payload.add("FRAME").add(myExpression).add(myTrimResult).add(myTempName);
  }

  @Override
  public boolean isResponseExpected() {
    return true;
  }

  @Override
  protected void processResponse(final ProtocolFrame response) throws PyDebuggerException {
    super.processResponse(response);
    final PyDebugValue value = ProtocolParser.parseValue(response.getPayload(), myDebugProcess);
    myValue = value.setName((myExecute ? "" : myExpression));
    if (!myTempName.isEmpty()) {
      myValue.setTempName(myTempName);
    }
  }

  public PyDebugValue getValue() {
    return myValue;
  }
}
