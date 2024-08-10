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

import lombok.Getter;
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

  public Expression parse(String expression) throws ParseException {
    return new Expression(
        expression,
        this.toSolvable(converter.toAbstractSyntaxTree(tokenizer.parse(expression), expression)),
        configuration);
  }

  public Solvable toSolvable(ASTNode node) {
    if (node instanceof InlinedASTNode inlined)
      return context -> context.expression().tryRoundValue(inlined.value());

    Token token = node.getToken();
    Solvable value =
        switch (token.getType()) {
          case VARIABLE_OR_CONSTANT -> context -> {
            var result = context.expression().getVariableOrConstant(token, context);
            if (result.isSolvable()) return result.getSolvable().solve(context);
            return result;
          };
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
                  index.getNumberValue(), array.getClass().getSimpleName(), array.getValue()));
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
              nameToken,
              String.format(
                  "Field '%s' not found in %s", name, structure.getClass().getSimpleName()));
        return result;
      }
      throw EvaluationException.ofUnsupportedDataTypeInOperation(startNode.getToken());
    };
  }

  private Solvable functionToSolvable(ASTNode node) {
    Token token = node.getToken();
    FunctionIfc function = token.getFunctionDefinition();
    Solvable[] solvables;

    if (node.getParameters().length == 0) {
      solvables = new Solvable[0];
    } else {
      solvables = new Solvable[node.getParameters().length];
      for (int i = 0; i < node.getParameters().length; i++) {
        if (function.isParameterLazy(i)) {
          var unwrapped = SolvableValue.of(toSolvable(node.getParameters()[i]));
          solvables[i] = context -> unwrapped;
        } else {
          solvables[i] = toSolvable(node.getParameters()[i]);
        }
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
