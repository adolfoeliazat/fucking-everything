// This is a generated file. Not intended for manual editing.
package photlin.devtools;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static photlin.devtools.psi.PHPTaggedTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class PHPTaggedParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    if (t == AT) {
      r = at(b, 0);
    }
    else if (t == SHIT) {
      r = shit(b, 0);
    }
    else {
      r = parse_root_(t, b, 0);
    }
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return phpTaggedFile(b, l + 1);
  }

  /* ********************************************************** */
  // AT_TOKEN
  public static boolean at(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "at")) return false;
    if (!nextTokenIs(b, AT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT_TOKEN);
    exit_section_(b, m, AT, r);
    return r;
  }

  /* ********************************************************** */
  // (at | shit | NL)*
  static boolean phpTaggedFile(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "phpTaggedFile")) return false;
    int c = current_position_(b);
    while (true) {
      if (!phpTaggedFile_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "phpTaggedFile", c)) break;
      c = current_position_(b);
    }
    return true;
  }

  // at | shit | NL
  private static boolean phpTaggedFile_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "phpTaggedFile_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = at(b, l + 1);
    if (!r) r = shit(b, l + 1);
    if (!r) r = consumeToken(b, NL);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SHIT_TOKEN
  public static boolean shit(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "shit")) return false;
    if (!nextTokenIs(b, SHIT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SHIT_TOKEN);
    exit_section_(b, m, SHIT, r);
    return r;
  }

}
