/*
  Copyright 2012-2022 Udo Klimaschewski

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
package com.ezylang.evalex;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.DataAccessorIfc;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.parser.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.UnaryOperator;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * Main class that allow creating, parsing, passing parameters and evaluating an expression string.
 *
 * @see <a href="https://github.com/ezylang/EvalEx">EvalEx Homepage</a>
 */
@Getter
public class Expression {

  private final ExpressionConfiguration configuration;
  private final String expressionString;
  private final @Nullable DataAccessorIfc dataAccessor;
  private final ASTNode abstractSyntaxTree;

  /**
   * Creates a new expression with a custom configuration. The expression is not parsed until it is
   * first evaluated or validated.
   *
   * @param expressionString A string holding an expression.
   */
  public Expression(
      String expressionString, ASTNode abstractSyntaxTree, ExpressionConfiguration configuration) {
    this.expressionString = expressionString;
    this.abstractSyntaxTree = abstractSyntaxTree;
    this.configuration = configuration;
    this.dataAccessor = configuration.getDataAccessorSupplier().get();
  }

  public EvaluationValue evaluate(UnaryOperator<EvaluationContext.EvaluationContextBuilder> builder)
      throws EvaluationException {
    return this.evaluate(builder.apply(EvaluationContext.builder(this)).build());
  }

  /**
   * Evaluates the expression by parsing it (if not done before) and the evaluating it.
   *
   * @return The evaluation result value.
   * @throws EvaluationException If there were problems while evaluating the expression.
   */
  public EvaluationValue evaluate(EvaluationContext context) throws EvaluationException {
    EvaluationValue result = evaluateSubtree(getAbstractSyntaxTree(), context);
    if (result.isNumberValue()) {
      BigDecimal bigDecimal = result.getNumberValue();
      if (configuration.getDecimalPlacesResult()
          != ExpressionConfiguration.DECIMAL_PLACES_ROUNDING_UNLIMITED) {
        bigDecimal = roundValue(bigDecimal, configuration.getDecimalPlacesResult());
      }

      if (configuration.isStripTrailingZeros()) {
        bigDecimal = bigDecimal.stripTrailingZeros();
      }

      result = EvaluationValue.numberValue(bigDecimal);
    }

    return result;
  }

  public EvaluationValue evaluateSubtree(
      ASTNode startNode, UnaryOperator<EvaluationContext.EvaluationContextBuilder> builder)
      throws EvaluationException {
    return this.evaluateSubtree(startNode, builder.apply(EvaluationContext.builder(this)).build());
  }

  /**
   * Evaluates only a subtree of the abstract syntax tree.
   *
   * @param startNode The {@link ASTNode} to start evaluation from.
   * @return The evaluation result value.
   * @throws EvaluationException If there were problems while evaluating the expression.
   */
  public EvaluationValue evaluateSubtree(ASTNode startNode, EvaluationContext context)
      throws EvaluationException {
    if (startNode instanceof InlinedASTNode)
      return tryRoundValue(((InlinedASTNode) startNode).getValue()); // All primitives go here.

    Token token = startNode.getToken();
    EvaluationValue result;
    switch (token.getType()) {
      case NUMBER_LITERAL:
        result = EvaluationValue.numberOfString(token.getValue(), configuration.getMathContext());
        break;
      case STRING_LITERAL:
        result = EvaluationValue.stringValue(token.getValue());
        break;
      case VARIABLE_OR_CONSTANT:
        result = getVariableOrConstant(token, context);
        if (result.isExpressionNode()) {
          result = evaluateSubtree(result.getExpressionNode(), context);
        }
        break;
      case PREFIX_OPERATOR:
      case POSTFIX_OPERATOR:
        result =
            token
                .getOperatorDefinition()
                .evaluate(
                    context, token, evaluateSubtree(startNode.getParameters().get(0), context));
        break;
      case INFIX_OPERATOR:
        result = evaluateInfixOperator(startNode, token, context);
        break;
      case ARRAY_INDEX:
        result = evaluateArrayIndex(startNode, context);
        break;
      case STRUCTURE_SEPARATOR:
        result = evaluateStructureSeparator(startNode, context);
        break;
      case FUNCTION:
        result = evaluateFunction(startNode, token, context);
        break;
      default:
        throw new EvaluationException(token, "Unexpected evaluation token: " + token);
    }
    return tryRoundValue(result);
  }

  public EvaluationValue tryRoundValue(EvaluationValue value) {
    if (value.isNumberValue()
        && configuration.getDecimalPlacesRounding()
            != ExpressionConfiguration.DECIMAL_PLACES_ROUNDING_UNLIMITED) {
      return EvaluationValue.numberValue(
          roundValue(value.getNumberValue(), configuration.getDecimalPlacesRounding()));
    }
    return value;
  }

  private EvaluationValue getVariableOrConstant(Token token, EvaluationContext context)
      throws EvaluationException {
    EvaluationValue result = context.parameters().get(token.getValue());
    if (result == null) {
      result = configuration.getConstants().get(token.getValue());
    }
    if (result == null && getDataAccessor() != null) {
      result = getDataAccessor().getData(token.getValue(), token, context);
    }
    if (result == null) {
      throw new EvaluationException(
          token, String.format("Variable or constant value for '%s' not found", token.getValue()));
    }
    return result;
  }

  private EvaluationValue evaluateFunction(
      ASTNode startNode, Token token, EvaluationContext context) throws EvaluationException {
    List<EvaluationValue> parameterResults = new ArrayList<>();
    for (int i = 0; i < startNode.getParameters().size(); i++) {
      if (token.getFunctionDefinition().isParameterLazy(i)) {
        parameterResults.add(convertValue(startNode.getParameters().get(i)));
      } else {
        parameterResults.add(evaluateSubtree(startNode.getParameters().get(i), context));
      }
    }

    EvaluationValue[] parameters = parameterResults.toArray(new EvaluationValue[0]);

    FunctionIfc function = token.getFunctionDefinition();

    function.validatePreEvaluation(token, parameters);

    return function.evaluate(context, token, parameters);
  }

  private EvaluationValue evaluateArrayIndex(ASTNode startNode, EvaluationContext context)
      throws EvaluationException {
    EvaluationValue array = evaluateSubtree(startNode.getParameters().get(0), context);
    EvaluationValue index = evaluateSubtree(startNode.getParameters().get(1), context);

    if (array.isArrayValue() && index.isNumberValue()) {
      if (index.getNumberValue().intValue() < 0
          || index.getNumberValue().intValue() >= array.getArrayValue().size()) {
        throw new EvaluationException(
            startNode.getToken(),
            String.format(
                "Index %d out of bounds for array of length %d",
                index.getNumberValue().intValue(), array.getArrayValue().size()));
      }
      return array.getArrayValue().get(index.getNumberValue().intValue());
    } else {
      throw EvaluationException.ofUnsupportedDataTypeInOperation(startNode.getToken());
    }
  }

  private EvaluationValue evaluateStructureSeparator(ASTNode startNode, EvaluationContext context)
      throws EvaluationException {
    EvaluationValue structure = evaluateSubtree(startNode.getParameters().get(0), context);
    Token nameToken = startNode.getParameters().get(1).getToken();
    String name = nameToken.getValue();

    if (structure.isStructureValue()) {
      if (!structure.getStructureValue().containsKey(name)) {
        throw new EvaluationException(
            nameToken, String.format("Field '%s' not found in structure", name));
      }
      return structure.getStructureValue().get(name);
    } else {
      throw EvaluationException.ofUnsupportedDataTypeInOperation(startNode.getToken());
    }
  }

  private EvaluationValue evaluateInfixOperator(
      ASTNode startNode, Token token, EvaluationContext context) throws EvaluationException {
    EvaluationValue left;
    EvaluationValue right;

    OperatorIfc op = token.getOperatorDefinition();
    if (op.isOperandLazy()) {
      left = convertValue(startNode.getParameters().get(0));
      right = convertValue(startNode.getParameters().get(1));
    } else {
      left = evaluateSubtree(startNode.getParameters().get(0), context);
      right = evaluateSubtree(startNode.getParameters().get(1), context);
    }
    return op.evaluate(context, token, left, right);
  }

  /**
   * Rounds the given value.
   *
   * @param value The input value.
   * @param decimalPlaces The number of decimal places to round to.
   * @return The rounded value, or the input value if rounding is not configured or possible.
   */
  private BigDecimal roundValue(BigDecimal value, int decimalPlaces) {
    value = value.setScale(decimalPlaces, configuration.getMathContext().getRoundingMode());
    return value;
  }

  /**
   * Returns a copy of the expression.
   *
   * @return The copied Expression instance.
   */
  public Expression copy() {
    return new Expression(getExpressionString(), getAbstractSyntaxTree(), getConfiguration());
  }

  /**
   * Converts a double value to an {@link EvaluationValue} by considering the configured {@link
   * java.math.MathContext}.
   *
   * @param value The double value to covert.
   * @return An {@link EvaluationValue} of type {@link EvaluationValue.DataType#NUMBER}.
   */
  public EvaluationValue convertDoubleValue(double value) {
    return convertValue(value);
  }

  /**
   * Converts an object value to an {@link EvaluationValue} by considering the configuration {@link
   * EvaluationValue(Object, ExpressionConfiguration)}.
   *
   * @param value The object value to covert.
   * @return An {@link EvaluationValue} of the detected type and value.
   */
  public EvaluationValue convertValue(Object value) {
    return EvaluationValue.of(value, configuration);
  }

  /**
   * Returns the list of all nodes of the abstract syntax tree.
   *
   * @return The list of all nodes in the parsed expression.
   */
  public List<ASTNode> getAllASTNodes() {
    return getAllASTNodesForNode(getAbstractSyntaxTree());
  }

  private List<ASTNode> getAllASTNodesForNode(ASTNode node) {
    List<ASTNode> nodes = new ArrayList<>();
    nodes.add(node);
    for (ASTNode child : node.getParameters()) {
      nodes.addAll(getAllASTNodesForNode(child));
    }
    return nodes;
  }

  /**
   * Returns all variables that are used i the expression, excluding the constants like e.g. <code>
   * PI</code> or <code>TRUE</code> and <code>FALSE</code>.
   *
   * @return All used variables excluding constants.
   */
  public Set<String> getUsedVariables() {
    Set<String> variables = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    for (ASTNode node : getAllASTNodes()) {
      if (node.getToken().getType() == Token.TokenType.VARIABLE_OR_CONSTANT
          && !configuration.getConstants().containsKey(node.getToken().getValue())) {
        variables.add(node.getToken().getValue());
      }
    }

    return variables;
  }
}
