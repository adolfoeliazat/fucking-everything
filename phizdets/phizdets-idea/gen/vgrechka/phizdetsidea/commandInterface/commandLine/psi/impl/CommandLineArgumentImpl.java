// This is a generated file. Not intended for manual editing.
package vgrechka.phizdetsidea.commandInterface.commandLine.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static vgrechka.phizdetsidea.commandInterface.commandLine.CommandLineElementTypes.*;
import vgrechka.phizdetsidea.commandInterface.commandLine.CommandLineElement;
import vgrechka.phizdetsidea.commandInterface.commandLine.psi.*;
import vgrechka.phizdetsidea.commandInterface.command.Argument;
import vgrechka.phizdetsidea.commandInterface.command.Help;
import vgrechka.phizdetsidea.commandInterface.command.Option;

public class CommandLineArgumentImpl extends CommandLineElement implements CommandLineArgument {

  public CommandLineArgumentImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CommandLineVisitor visitor) {
    visitor.visitArgument(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CommandLineVisitor) accept((CommandLineVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getLiteralStartsFromDigit() {
    return findChildByType(LITERAL_STARTS_FROM_DIGIT);
  }

  @Override
  @Nullable
  public PsiElement getLiteralStartsFromLetter() {
    return findChildByType(LITERAL_STARTS_FROM_LETTER);
  }

  @Override
  @Nullable
  public PsiElement getLiteralStartsFromSymbol() {
    return findChildByType(LITERAL_STARTS_FROM_SYMBOL);
  }

  @Nullable
  public Option findOptionForOptionArgument() {
    return CommandLinePsiImplUtils.findOptionForOptionArgument(this);
  }

  @Nullable
  public Argument findRealArgument() {
    return CommandLinePsiImplUtils.findRealArgument(this);
  }

  @Nullable
  public Help findBestHelp() {
    return CommandLinePsiImplUtils.findBestHelp(this);
  }

}
