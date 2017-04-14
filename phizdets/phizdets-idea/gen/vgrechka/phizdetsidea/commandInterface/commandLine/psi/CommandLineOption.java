// This is a generated file. Not intended for manual editing.
package vgrechka.phizdetsidea.commandInterface.commandLine.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import vgrechka.phizdetsidea.commandInterface.commandLine.CommandLinePart;
import vgrechka.phizdetsidea.commandInterface.command.Option;

public interface CommandLineOption extends CommandLinePart {

  @Nullable
  PsiElement getLongOptionNameToken();

  @Nullable
  PsiElement getShortOptionNameToken();

  @Nullable
  @NonNls
  String getOptionName();

  boolean isLong();

  @Nullable
  Option findRealOption();

}
