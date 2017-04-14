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

public class CommandLineCommandImpl extends CommandLineElement implements CommandLineCommand {

  public CommandLineCommandImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CommandLineVisitor visitor) {
    visitor.visitCommand(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CommandLineVisitor) accept((CommandLineVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getLiteralStartsFromLetter() {
    return findNotNullChildByType(LITERAL_STARTS_FROM_LETTER);
  }

}
