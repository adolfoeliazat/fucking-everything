// This is a generated file. Not intended for manual editing.
package vgrechka.phizdetsidea.commandInterface.commandLine.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import vgrechka.phizdetsidea.commandInterface.commandLine.CommandLinePart;
import vgrechka.phizdetsidea.commandInterface.command.Argument;
import vgrechka.phizdetsidea.commandInterface.command.Help;
import vgrechka.phizdetsidea.commandInterface.command.Option;

public interface CommandLineArgument extends CommandLinePart {

  @Nullable
  PsiElement getLiteralStartsFromDigit();

  @Nullable
  PsiElement getLiteralStartsFromLetter();

  @Nullable
  PsiElement getLiteralStartsFromSymbol();

  @Nullable
  Option findOptionForOptionArgument();

  @Nullable
  Argument findRealArgument();

  @Nullable
  Help findBestHelp();

}
