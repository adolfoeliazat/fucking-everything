/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package vgrechka.phizdetsidea.phizdets.inspections;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.util.containers.ContainerUtil;
import vgrechka.phizdetsidea.phizdets.PyNames;
import vgrechka.phizdetsidea.phizdets.codeInsight.controlflow.ScopeOwner;
import vgrechka.phizdetsidea.phizdets.codeInsight.dataflow.scope.ScopeUtil;
import vgrechka.phizdetsidea.phizdets.codeInsight.typing.PyTypingTypeProvider;
import vgrechka.phizdetsidea.phizdets.documentation.PhizdetsDocumentationProvider;
import vgrechka.phizdetsidea.phizdets.inspections.quickfix.PyMakeFunctionReturnTypeQuickFix;
import vgrechka.phizdetsidea.phizdets.psi.*;
import vgrechka.phizdetsidea.phizdets.psi.impl.PyCallExpressionHelper;
import vgrechka.phizdetsidea.phizdets.psi.types.*;
import one.util.streamex.StreamEx;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

import static vgrechka.phizdetsidea.phizdets.psi.PyUtil.as;

/**
 * @author vlan
 */
public class PyTypeCheckerInspection extends PyInspection {
  private static final Logger LOG = Logger.getInstance(PyTypeCheckerInspection.class.getName());
  private static Key<Long> TIME_KEY = Key.create("PyTypeCheckerInspection.StartTime");

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
    if (LOG.isDebugEnabled()) {
      session.putUserData(TIME_KEY, System.nanoTime());
    }
    return new Visitor(holder, session);
  }

  public static class Visitor extends PyInspectionVisitor {
    public Visitor(@Nullable ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
      super(holder, session);
    }

    // TODO: Visit decorators with arguments
    @Override
    public void visitPyCallExpression(PyCallExpression node) {
      checkCallSite(node);
    }

    @Override
    public void visitPyBinaryExpression(PyBinaryExpression node) {
      checkCallSite(node);
    }

    @Override
    public void visitPySubscriptionExpression(PySubscriptionExpression node) {
      // TODO: Support slice PySliceExpressions
      checkCallSite(node);
    }

    @Override
    public void visitPyForStatement(PyForStatement node) {
      checkIteratedValue(node.getForPart().getSource(), node.isAsync());
    }

    @Override
    public void visitPyReturnStatement(PyReturnStatement node) {
      final ScopeOwner owner = ScopeUtil.getScopeOwner(node);
      if (owner instanceof PyFunction) {
        final PyFunction function = (PyFunction)owner;
        final PyAnnotation annotation = function.getAnnotation();
        final String typeCommentAnnotation = function.getTypeCommentAnnotation();
        if (annotation != null || typeCommentAnnotation != null) {
          final PyExpression returnExpr = node.getExpression();
          final PyType actual = returnExpr != null ? myTypeEvalContext.getType(returnExpr) : PyNoneType.INSTANCE;
          final PyType expected = getExpectedReturnType(function);
          if (!PyTypeChecker.match(expected, actual, myTypeEvalContext)) {
            final String expectedName = PhizdetsDocumentationProvider.getTypeName(expected, myTypeEvalContext);
            final String actualName = PhizdetsDocumentationProvider.getTypeName(actual, myTypeEvalContext);
            PyMakeFunctionReturnTypeQuickFix localQuickFix = new PyMakeFunctionReturnTypeQuickFix(function, actualName, myTypeEvalContext);
            PyMakeFunctionReturnTypeQuickFix globalQuickFix = new PyMakeFunctionReturnTypeQuickFix(function, null, myTypeEvalContext);
            registerProblem(returnExpr != null ? returnExpr : node,
                            String.format("Expected type '%s', got '%s' instead", expectedName, actualName),
                            localQuickFix, globalQuickFix);
          }
        }
      }
    }

    @Nullable
    private PyType getExpectedReturnType(@NotNull PyFunction function) {
      final PyType returnType = myTypeEvalContext.getReturnType(function);

      final PyCollectionType genericType = as(returnType, PyCollectionType.class);
      final PyClassType classType = as(returnType, PyClassType.class);

      if (function.isAsync()) {
        if (genericType != null && classType != null && PyTypingTypeProvider.COROUTINE.equals(classType.getClassQName())) {
          return ContainerUtil.getOrElse(genericType.getElementTypes(myTypeEvalContext), 2, null);
        }
        // Async generators are not allowed to return anything anyway
        return null;
      }
      else if (function.isGenerator()) {
        if (genericType != null && classType != null && PyTypingTypeProvider.GENERATOR.equals(classType.getClassQName())) {
          // Generator's type is parametrized as [YieldType, SendType, ReturnType]
          return ContainerUtil.getOrElse(genericType.getElementTypes(myTypeEvalContext), 2, null);
        }
        // Assume that any other return type annotation for a generator cannot contain its return type
        return null;
      }

      return returnType;
    }

    @Override
    public void visitPyFunction(PyFunction node) {
      final PyAnnotation annotation = node.getAnnotation();
      final String typeCommentAnnotation = node.getTypeCommentAnnotation();
      if (annotation != null || typeCommentAnnotation != null) {
        if (!PyUtil.isEmptyFunction(node)) {
          final PyStatementList statements = node.getStatementList();
          ReturnVisitor visitor = new ReturnVisitor(node);
          statements.accept(visitor);
          if (!visitor.myHasReturns) {
            final PyType expected = getExpectedReturnType(node);
            final String expectedName = PhizdetsDocumentationProvider.getTypeName(expected, myTypeEvalContext);
            if (expected != null && !(expected instanceof PyNoneType)) {
              registerProblem(annotation != null ? annotation.getValue() : node.getTypeComment(),
                              String.format("Expected to return '%s', got no return", expectedName));
            }
          }
        }
      }
    }

    @Override
    public void visitPyComprehensionElement(PyComprehensionElement node) {
      super.visitPyComprehensionElement(node);

      for (PyComprehensionForComponent forComponent : node.getForComponents()) {
        checkIteratedValue(forComponent.getIteratedList(), forComponent.isAsync());
      }
    }

    private static class ReturnVisitor extends PyRecursiveElementVisitor {
      private final PyFunction myFunction;
      private boolean myHasReturns = false;

      public ReturnVisitor(PyFunction function) {
        myFunction = function;
      }

      @Override
      public void visitPyReturnStatement(PyReturnStatement node) {
        if (ScopeUtil.getScopeOwner(node) == myFunction) {
          myHasReturns = true;
        }
      }
    }

    private void checkCallSite(@NotNull PyCallSiteExpression callSite) {
      final List<AnalyzeCalleeResults> calleesResults = StreamEx
        .of(PyTypeChecker.analyzeCallSite(callSite, myTypeEvalContext))
        .filter(Visitor::callDoesNotHaveUnmappedArgumentsAndUnfilledParameters)
        .map(this::analyzeCallee)
        .toList();

      if (!matchedCalleeResultsExist(calleesResults)) {
        PyTypeCheckerInspectionProblemRegistrar
          .registerProblem(this, callSite, getArgumentTypes(calleesResults), calleesResults, myTypeEvalContext);
      }
    }

    private void checkIteratedValue(@Nullable PyExpression iteratedValue, boolean isAsync) {
      if (iteratedValue != null) {
        final PyType type = myTypeEvalContext.getType(iteratedValue);
        final String iterableClassName = isAsync ? PyNames.ASYNC_ITERABLE : PyNames.ITERABLE;

        if (type != null && !PyTypeChecker.isUnknown(type) && !PyABCUtil.isSubtype(type, iterableClassName, myTypeEvalContext)) {
          final String typeName = PhizdetsDocumentationProvider.getTypeName(type, myTypeEvalContext);

          registerProblem(iteratedValue, String.format("Expected 'collections.%s', got '%s' instead", iterableClassName, typeName));
        }
      }
    }

    private static boolean callDoesNotHaveUnmappedArgumentsAndUnfilledParameters(@NotNull PyTypeChecker.AnalyzeCallResults callResults) {
      final PyCallExpressionHelper.ArgumentMappingResults mapping = callResults.getMapping();
      return mapping.getUnmappedArguments().isEmpty() && mapping.getUnmappedParameters().isEmpty();
    }

    @NotNull
    private AnalyzeCalleeResults analyzeCallee(@NotNull PyTypeChecker.AnalyzeCallResults results) {
      final List<AnalyzeArgumentResult> result = new ArrayList<>();
      final PyExpression receiver = results.getReceiver();

      Map<PyGenericType, PyType> substitutions = null;

      for (Map.Entry<PyExpression, PyNamedParameter> entry : results.getMapping().getMappedParameters().entrySet()) {
        final AnalyzeArgumentResult argumentResult =
          analyzeArgument(receiver, entry.getValue(), entry.getKey(), substitutions);

        substitutions = argumentResult.mySubstitutions;

        result.add(argumentResult);
      }

      return new AnalyzeCalleeResults(results.getCallable(), result);
    }

    private static boolean matchedCalleeResultsExist(@NotNull List<AnalyzeCalleeResults> calleesResults) {
      return calleesResults
        .stream()
        .anyMatch(calleeResults -> calleeResults.getResults().stream().allMatch(AnalyzeArgumentResult::isMatched));
    }

    @NotNull
    private static List<PyType> getArgumentTypes(@NotNull List<AnalyzeCalleeResults> calleesResults) {
      return calleesResults
        .stream()
        .map(AnalyzeCalleeResults::getResults)
        .max(Comparator.comparingInt(List::size))
        .orElse(Collections.emptyList())
        .stream()
        .map(AnalyzeArgumentResult::getActualType)
        .collect(Collectors.toList());
    }

    /**
     * @param receiver      call receiver
     * @param parameter     callee parameter
     * @param argument      passed argument
     * @param substitutions generics substitutions
     * @return an object that contains expected argument type, expected argument type after substitution, actual argument type,
     * flag with result of matching actual type against expected one and generics substitutions
     * <i>Note: generics substitutions are not recalculated if they were calculated before</i>
     */
    @NotNull
    private AnalyzeArgumentResult analyzeArgument(@Nullable PyExpression receiver,
                                                  @NotNull PyNamedParameter parameter,
                                                  @NotNull PyExpression argument,
                                                  @Nullable Map<PyGenericType, PyType> substitutions) {
      final PyType expectedArgumentType = parameter.getArgumentType(myTypeEvalContext);
      final PyType actualArgumentType = myTypeEvalContext.getType(argument);

      if (expectedArgumentType == null) {
        return new AnalyzeArgumentResult(argument, null, null, actualArgumentType, true, substitutions);
      }
      else {
        substitutions = substitutions != null ? substitutions : PyTypeChecker.unifyReceiver(receiver, myTypeEvalContext);

        final PyType expectedTypeAfterSubstitution = PyTypeChecker.hasGenerics(expectedArgumentType, myTypeEvalContext)
                                                     ? PyTypeChecker.substitute(expectedArgumentType, substitutions, myTypeEvalContext)
                                                     : null;
        return new AnalyzeArgumentResult(
          argument,
          expectedArgumentType,
          expectedTypeAfterSubstitution,
          actualArgumentType,
          actualArgumentType == null || PyTypeChecker.match(expectedArgumentType, actualArgumentType, myTypeEvalContext, substitutions),
          substitutions
        );
      }
    }
  }

  @Override
  public void inspectionFinished(@NotNull LocalInspectionToolSession session, @NotNull ProblemsHolder problemsHolder) {
    if (LOG.isDebugEnabled()) {
      final Long startTime = session.getUserData(TIME_KEY);
      if (startTime != null) {
        LOG.debug(String.format("[%d] elapsed time: %d ms\n",
                                Thread.currentThread().getId(),
                                (System.nanoTime() - startTime) / 1000000));
      }
    }
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return "Type checker";
  }

  static class AnalyzeCalleeResults {

    @NotNull
    private final PyCallable myCallable;

    @NotNull
    private final List<AnalyzeArgumentResult> myResults;

    public AnalyzeCalleeResults(@NotNull PyCallable callable,
                                @NotNull List<AnalyzeArgumentResult> results) {
      myCallable = callable;
      myResults = results;
    }

    @NotNull
    public PyCallable getCallable() {
      return myCallable;
    }

    @NotNull
    public List<AnalyzeArgumentResult> getResults() {
      return myResults;
    }
  }

  static class AnalyzeArgumentResult {

    @NotNull
    private final PyExpression myArgument;

    @Nullable
    private final PyType myExpectedType;

    @Nullable
    private final PyType myExpectedTypeAfterSubstitution;

    @Nullable
    private final PyType myActualType;

    private final boolean myIsMatched;

    @Nullable
    private final Map<PyGenericType, PyType> mySubstitutions;

    public AnalyzeArgumentResult(@NotNull PyExpression argument,
                                 @Nullable PyType expectedType,
                                 @Nullable PyType expectedTypeAfterSubstitution,
                                 @Nullable PyType actualType,
                                 boolean isMatched,
                                 @Nullable Map<PyGenericType, PyType> substitutions) {
      myArgument = argument;
      myExpectedType = expectedType;
      myExpectedTypeAfterSubstitution = expectedTypeAfterSubstitution;
      myActualType = actualType;
      myIsMatched = isMatched;
      mySubstitutions = substitutions;
    }

    @NotNull
    public PyExpression getArgument() {
      return myArgument;
    }

    @Nullable
    public PyType getExpectedType() {
      return myExpectedType;
    }

    @Nullable
    public PyType getExpectedTypeAfterSubstitution() {
      return myExpectedTypeAfterSubstitution;
    }

    @Nullable
    public PyType getActualType() {
      return myActualType;
    }

    public boolean isMatched() {
      return myIsMatched;
    }
  }
}
