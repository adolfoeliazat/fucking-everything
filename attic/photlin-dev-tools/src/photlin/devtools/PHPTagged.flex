package photlin.devtools;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static photlin.devtools.psi.PHPTaggedTypes.*;

%%

%{
  public PHPTaggedLexer() {
    this((java.io.Reader)null);
  }
%}

%public
%class PHPTaggedLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

NL=(\r|\n)+
AT_TOKEN=\d+@
SHIT_TOKEN=[^\d\r\n]+

%%
<YYINITIAL> {
  {AT_TOKEN}         { return AT_TOKEN; }
  {SHIT_TOKEN}       { return SHIT_TOKEN; }
  {NL}       { return NL; }
}

[^] { return SHIT_TOKEN; }

