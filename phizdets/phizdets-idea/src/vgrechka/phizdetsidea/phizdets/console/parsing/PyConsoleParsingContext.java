/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vgrechka.phizdetsidea.phizdets.console.parsing;

import com.intellij.lang.PsiBuilder;
import vgrechka.phizdetsidea.phizdets.PyElementTypes;
import vgrechka.phizdetsidea.phizdets.PyTokenTypes;
import vgrechka.phizdetsidea.phizdets.parsing.ExpressionParsing;
import vgrechka.phizdetsidea.phizdets.parsing.ParsingContext;
import vgrechka.phizdetsidea.phizdets.parsing.ParsingScope;
import vgrechka.phizdetsidea.phizdets.parsing.StatementParsing;
import vgrechka.phizdetsidea.phizdets.psi.LanguageLevel;
import org.jetbrains.annotations.Nullable;

/**
 * @author traff
 */
public class PyConsoleParsingContext extends ParsingContext {
  private final StatementParsing stmtParser;
  private final ExpressionParsing expressionParser;

  public PyConsoleParsingContext(final PsiBuilder builder,
                                 LanguageLevel languageLevel,
                                 StatementParsing.FUTURE futureFlag,
                                 PhizdetsConsoleData phizdetsConsoleData, boolean startsWithIPhizdetsSymbol) {
    super(builder, languageLevel, futureFlag);
    stmtParser = new ConsoleStatementParsing(this, futureFlag, startsWithIPhizdetsSymbol, phizdetsConsoleData);
    if (phizdetsConsoleData.isIPhizdetsEnabled()) {
      expressionParser = new ConsoleExpressionParsing(this);
    }
    else {
      expressionParser = new ExpressionParsing(this);
    }
  }

  @Override
  public StatementParsing getStatementParser() {
    return stmtParser;
  }

  @Override
  public ExpressionParsing getExpressionParser() {
    return expressionParser;
  }

  public static class ConsoleStatementParsing extends StatementParsing {

    private boolean myStartsWithIPhizdetsSymbol;
    private PhizdetsConsoleData myPhizdetsConsoleData;

    protected ConsoleStatementParsing(ParsingContext context,
                                      @Nullable FUTURE futureFlag,
                                      boolean startsWithIPhizdetsSymbol,
                                      PhizdetsConsoleData phizdetsConsoleData) {
      super(context, futureFlag);
      myStartsWithIPhizdetsSymbol = startsWithIPhizdetsSymbol;
      myPhizdetsConsoleData = phizdetsConsoleData;
    }


    @Override
    public void parseStatement() {
      if (myStartsWithIPhizdetsSymbol) {
        parseIPhizdetsCommand();
      }
      else {
        if (myPhizdetsConsoleData.isIPhizdetsEnabled()) {
          if (myPhizdetsConsoleData.isIPhizdetsAutomagic()) {
            if (myPhizdetsConsoleData.isMagicCommand(myBuilder.getTokenText())) {
              parseIPhizdetsCommand();
            }
          }
        }
        if (myPhizdetsConsoleData.getIndentSize() > 0) {
          if (myBuilder.getTokenType() == PyTokenTypes.INDENT) {
            myBuilder.advanceLexer();
          }
        }
        super.parseStatement();
      }
    }

    private void parseIPhizdetsCommand() {
      PsiBuilder.Marker iphizdetsCommand = myBuilder.mark();
      while (!myBuilder.eof()) {
        myBuilder.advanceLexer();
      }
      iphizdetsCommand.done(PyElementTypes.EMPTY_EXPRESSION);
    }

    protected void checkEndOfStatement() {
      if (myPhizdetsConsoleData.isIPhizdetsEnabled()) {
        PsiBuilder builder = myContext.getBuilder();
        if (builder.getTokenType() == PyTokenTypes.STATEMENT_BREAK) {
          builder.advanceLexer();
        }
        else if (builder.getTokenType() == PyTokenTypes.SEMICOLON) {
          final ParsingScope scope = getParsingContext().getScope();
          if (!scope.isSuite()) {
            builder.advanceLexer();
            if (builder.getTokenType() == PyTokenTypes.STATEMENT_BREAK) {
              builder.advanceLexer();
            }
          }
        }
        else if (builder.eof()) {
          return;
        }
        else {
          if (builder.getTokenType() == PyConsoleTokenTypes.PLING || builder.getTokenType() == PyConsoleTokenTypes.QUESTION_MARK) {
            builder.advanceLexer();
            if (builder.getTokenType() == PyConsoleTokenTypes.PLING || builder.getTokenType() == PyConsoleTokenTypes.QUESTION_MARK) {
              builder.advanceLexer();
            }

            return;
          }
          builder.error("End of statement expected");
        }
      }
      else {
        super.checkEndOfStatement();
      }
    }
  }

  public static class ConsoleExpressionParsing extends ExpressionParsing {
    public ConsoleExpressionParsing(ParsingContext context) {
      super(context);
    }

    @Override
    public boolean parseExpressionOptional() {
      if (myBuilder.getTokenType() == PyTokenTypes.PERC ||
          myBuilder.getTokenType() == PyConsoleTokenTypes.PLING ||
          myBuilder.getTokenType() == PyConsoleTokenTypes.QUESTION_MARK) {
        PsiBuilder.Marker expr = myBuilder.mark();
        PsiBuilder.Marker command = myBuilder.mark();

        myBuilder.advanceLexer();

        if (myBuilder.getTokenType() == PyTokenTypes.IDENTIFIER) {
          myBuilder.advanceLexer();
          command.done(getReferenceType());
        }
        else {
          expr.drop();
          command.drop();
          myBuilder.error("Identifier expected.");
          return false;
        }
        while (myBuilder.getTokenType() != null) {
          myBuilder.advanceLexer();
        }
        expr.done(PyElementTypes.EMPTY_EXPRESSION);
        return true;
      }
      else {
        return super.parseExpressionOptional();
      }
    }
  }
}
