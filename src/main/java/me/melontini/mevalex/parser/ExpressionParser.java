/*
  Copyright 2012-2024 Udo Klimaschewski

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/
package me.melontini.mevalex.parser;

import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.Expression;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.DataAccessorIfc;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.IndexedAccessor;
import me.melontini.mevalex.data.types.SolvableValue;
import me.melontini.mevalex.functions.FunctionIfc;
import me.melontini.mevalex.operators.OperatorIfc;

@Getter
public final class ExpressionParser {

  private final ExpressionConfiguration configuration;
  private final Tokenizer tokenizer;
  private final ShuntingYardConverter converter;

  public ExpressionParser(ExpressionConfiguration configuration) {
    this.configuration = configuration;
    this.tokenizer = new Tokenizer(configuration);
    this.converter = new ShuntingYardConverter(configuration);
  }

  public Expression parse(String expression) throws ParseException, EvaluationException {
    ASTNode root = converter.toAbstractSyntaxTree(tokenizer.parse(expression), expression);
    var proxy = new Expression(expression, toSolvable(root), configuration);
    return new Expression(expression, toSolvable(inline(proxy, root)), configuration);
  }

  public ASTNode inline(Expression parent, ASTNode node) throws EvaluationException {
    if (node instanceof InlinedASTNode) return tryRound(parent, node);

    // We declare the index not inlineable, but its parameters on the other hand...
    var parameters = node.getParameters();
    for (int i = 0; i < parameters.length; i++) {
      switch (parameters[i].getToken().getType()) {
        case ARRAY_INDEX, STRUCTURE_SEPARATOR -> parameters[i] = inline(parent, parameters[i]);
      }
    }

    Token token = node.getToken();
    return tryRound(
        parent,
        switch (token.getType()) {
          case VARIABLE_OR_CONSTANT -> {
            if (!configuration.isAllowOverwriteConstants()) {
              var result = configuration.getConstants().get(token.getValue());
              if (result != null) yield InlinedASTNode.of(token, result);
            }
            yield node;
          }
          case PREFIX_OPERATOR, POSTFIX_OPERATOR -> inlinePrePostfix(parent, token, node);
          case INFIX_OPERATOR -> inlineInfix(parent, token, node);
          case FUNCTION -> inlineFunction(parent, token, node);
          case ARRAY_INDEX -> {
            for (int i1 = 0; i1 < 2; i1++)
              node.getParameters()[i1] = inline(parent, node.getParameters()[i1]);
            yield node;
          }
          case STRUCTURE_SEPARATOR -> {
            node.getParameters()[0] = inline(parent, node.getParameters()[0]);
            yield node;
          }
          default -> throw new IllegalStateException("Unexpected evaluation token: " + token);
        });
  }

  private ASTNode tryRound(Expression parent, ASTNode node) {
    if (!(node instanceof InlinedASTNode inlined)) return node;

    var result = parent.tryRoundValue(inlined.value());
    if (Objects.equals(result.getValue(), inlined.value().getValue())) return inlined;
    return InlinedASTNode.of(node.getToken(), result, node.getParameters());
  }

  private ASTNode inlineFunction(Expression parent, Token token, ASTNode node)
      throws EvaluationException {
    var function = token.getFunctionDefinition();
    if (!function.canInline()) return node;
    var parameters = node.getParameters();

    EvaluationValue[] result = new EvaluationValue[parameters.length];
    boolean allMatch = true;
    for (int i = 0; i < parameters.length; i++) {
      ASTNode parameter = parameters[i];

      if (function.isParameterLazy(i)) {
        if (!canInline(parameter)) allMatch = false;
        result[i] = SolvableValue.of(toSolvable(node));
      } else {
        parameters[i] = inline(parent, parameters[i]);
        if (!(parameters[i] instanceof InlinedASTNode inlined)) {
          allMatch = false;
          continue;
        }
        result[i] = inlined.value();
      }
    }
    if (!allMatch) return node;

    return InlinedASTNode.of(
        token,
        function.evaluate(EvaluationContext.builder(parent).build(), token, result),
        parameters);
  }

  private ASTNode inlineInfix(Expression parent, Token token, ASTNode node)
      throws EvaluationException {
    var operator = token.getOperatorDefinition();
    var parameters = node.getParameters();

    if (!operator.isOperandLazy()) {
      boolean allMatch = true;
      for (int i = 0; i < 2; i++) {
        if (!((parameters[i] = inline(parent, parameters[i])) instanceof InlinedASTNode))
          allMatch = false;
      }
      if (!allMatch) return node;

      if (operator.canInline()) {
        return InlinedASTNode.of(
            token,
            operator.evaluate(
                EvaluationContext.builder(parent).build(),
                token,
                Arrays.stream(parameters)
                    .map(node1 -> ((InlinedASTNode) node1).value())
                    .toArray(EvaluationValue[]::new)),
            parameters);
      }
      return node;
    } else {
      if (!operator.canInline()) return node;

      SolvableValue[] lazy = new SolvableValue[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        ASTNode parameter = parameters[i];
        if (!canInline(parameter)) return node;
        lazy[i] = SolvableValue.of(toSolvable(parameter));
      }
      return InlinedASTNode.of(
          token,
          operator.evaluate(EvaluationContext.builder(parent).build(), token, lazy),
          parameters);
    }
  }

  /**
   * When working with lazy operand we cannot immediately inline the operand as it can throw an
   * {@link EvaluationException}.
   *
   * @return If the node can be safely inlined.
   */
  private boolean canInline(ASTNode node) {
    if (node instanceof InlinedASTNode) return true;

    Token token = node.getToken();
    return switch (token.getType()) {
      case VARIABLE_OR_CONSTANT -> !configuration.isAllowOverwriteConstants()
          && configuration.getConstants().containsKey(token.getValue());
      case PREFIX_OPERATOR, POSTFIX_OPERATOR, INFIX_OPERATOR -> {
        if (!token.getOperatorDefinition().canInline()) yield false;
        for (ASTNode parameter : node.getParameters()) {
          if (!canInline(parameter)) yield false;
        }
        yield true;
      }
      case FUNCTION -> {
        if (!token.getFunctionDefinition().canInline()) yield false;
        for (ASTNode parameter : node.getParameters()) {
          if (!canInline(parameter)) yield false;
        }
        yield true;
      }
      default -> false;
    };
  }

  private ASTNode inlinePrePostfix(Expression parent, Token token, ASTNode node)
      throws EvaluationException {
    var operator = token.getOperatorDefinition();
    node.getParameters()[0] = inline(parent, node.getParameters()[0]);
    if (node.getParameters()[0] instanceof InlinedASTNode inlined && operator.canInline()) {
      return InlinedASTNode.of(
          token,
          operator.evaluate(EvaluationContext.builder(parent).build(), token, inlined.value()),
          node.getParameters());
    }
    return node;
  }

  public Solvable toSolvable(ASTNode node) {
    if (node instanceof InlinedASTNode inlined) return inlined;

    Token token = node.getToken();
    Solvable value =
        switch (token.getType()) {
          case VARIABLE_OR_CONSTANT -> {
            if (!configuration.isAllowOverwriteConstants()) {
              var result = configuration.getConstants().get(token.getValue());
              if (result != null) yield context -> result;
            }

            yield context -> {
              var result = context.expression().getVariableOrConstant(token, context);
              return result.isSolvable() ? result.getSolvable().solve(context) : result;
            };
          }
          case PREFIX_OPERATOR, POSTFIX_OPERATOR -> {
            OperatorIfc operator = token.getOperatorDefinition();
            Solvable solvable = toSolvable(node.getParameters()[0]);
            yield context -> operator.evaluate(context, token, solvable.solve(context));
          }
          case INFIX_OPERATOR -> infixOperatorToSolvable(node);
          case ARRAY_INDEX -> arrayIndexToSolvable(node);
          case STRUCTURE_SEPARATOR -> structureSeparatorToSolvable(node);
          case FUNCTION -> functionToSolvable(node);
          default -> throw new IllegalStateException("Unexpected evaluation token: " + token);
        };
    return context -> context.expression().tryRoundValue(value.solve(context));
  }

  private Solvable infixOperatorToSolvable(ASTNode node) {
    Token token = node.getToken();
    OperatorIfc operator = token.getOperatorDefinition();

    Solvable left;
    Solvable right;
    if (operator.isOperandLazy()) {
      var first = SolvableValue.of(toSolvable(node.getParameters()[0]));
      var second = SolvableValue.of(toSolvable(node.getParameters()[1]));
      left = context -> first;
      right = context -> second;
    } else {
      left = toSolvable(node.getParameters()[0]);
      right = toSolvable(node.getParameters()[1]);
    }
    return context -> operator.evaluate(context, token, left.solve(context), right.solve(context));
  }

  private Solvable arrayIndexToSolvable(ASTNode node) {
    Token token = node.getToken();

    Solvable solvableArray = toSolvable(node.getParameters()[0]);
    Solvable solvableIndex = toSolvable(node.getParameters()[1]);

    return context -> {
      var array = solvableArray.solve(context);
      var index = solvableIndex.solve(context);

      if (array instanceof IndexedAccessor accessor && index.isNumberValue()) {
        var result = accessor.getIndexedData(index.getNumberValue(), token, context);
        if (result == null)
          throw new EvaluationException(
              token,
              String.format(
                  "Index %s out of bounds for %s %s",
                  index.getNumberValue(), array.getName(), array.getValue()));
        return result;
      }
      throw EvaluationException.ofUnsupportedDataTypeInOperation(token);
    };
  }

  private Solvable structureSeparatorToSolvable(ASTNode startNode) {
    Solvable solvableStructure = toSolvable(startNode.getParameters()[0]);
    Token nameToken = startNode.getParameters()[1].getToken();
    String name = nameToken.getValue();

    return context -> {
      EvaluationValue structure = solvableStructure.solve(context);
      if (structure instanceof DataAccessorIfc accessor) {
        var result = accessor.getVariableData(name, nameToken, context);
        if (result == null)
          throw new EvaluationException(
              nameToken, String.format("Field '%s' not found in %s", name, structure.getName()));
        return result;
      }
      throw EvaluationException.ofUnsupportedDataTypeInOperation(startNode.getToken());
    };
  }

  private Solvable functionToSolvable(ASTNode node) {
    Token token = node.getToken();
    FunctionIfc function = token.getFunctionDefinition();

    if (node.getParameters().length == 0) {
      return context -> {
        function.validatePreEvaluation(token, EvaluationValue.EMPTY);
        return function.evaluate(context, token, EvaluationValue.EMPTY);
      };
    }

    if (node.getParameters().length == 1) {
      Solvable solvable;
      if (function.isParameterLazy(0)) {
        var unwrapped = SolvableValue.of(toSolvable(node.getParameters()[0]));
        solvable = context -> unwrapped;
      } else {
        solvable = toSolvable(node.getParameters()[0]);
      }
      return context -> {
        function.validatePreEvaluation(token, solvable.solve(context));
        return function.evaluate(context, token, solvable.solve(context));
      };
    }

    Solvable[] solvables = new Solvable[node.getParameters().length];
    for (int i = 0; i < node.getParameters().length; i++) {
      if (function.isParameterLazy(i)) {
        var unwrapped = SolvableValue.of(toSolvable(node.getParameters()[i]));
        solvables[i] = context -> unwrapped;
      } else {
        solvables[i] = toSolvable(node.getParameters()[i]);
      }
    }

    return context -> {
      EvaluationValue[] parameters = new EvaluationValue[solvables.length];
      for (int i = 0; i < solvables.length; i++) {
        parameters[i] = solvables[i].solve(context);
      }
      function.validatePreEvaluation(token, parameters);
      return function.evaluate(context, token, parameters);
    };
  }
}
