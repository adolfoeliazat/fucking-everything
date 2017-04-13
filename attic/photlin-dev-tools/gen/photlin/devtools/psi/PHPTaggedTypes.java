// This is a generated file. Not intended for manual editing.
package photlin.devtools.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import photlin.devtools.psi.impl.*;

public interface PHPTaggedTypes {

  IElementType AT = new PHPTaggedElementType("AT");
  IElementType SHIT = new PHPTaggedElementType("SHIT");

  IElementType AT_TOKEN = new PHPTaggedTokenType("AT_TOKEN");
  IElementType NL = new PHPTaggedTokenType("NL");
  IElementType SHIT_TOKEN = new PHPTaggedTokenType("SHIT_TOKEN");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
       if (type == AT) {
        return new PHPTaggedAtImpl(node);
      }
      else if (type == SHIT) {
        return new PHPTaggedShitImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
