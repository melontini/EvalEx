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
import com.ezylang.evalex.data.types.NumberValue;
import com.ezylang.evalex.parser.*;
import java.math.BigDecimal;
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
  private final Solvable solvable;

  /**
   * Creates a new expression with a custom configuration. The expression is not parsed until it is
   * first evaluated or validated.
   *
   * @param expressionString A string holding an expression.
   */
  public Expression(
      String expressionString, Solvable solvable, ExpressionConfiguration configuration) {
    this.expressionString = expressionString;
    this.solvable = solvable;
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
    EvaluationValue result = evaluateSubtree(this.getSolvable(), context);
    if (result.isNumberValue()) {
      BigDecimal bigDecimal = result.getNumberValue();
      if (configuration.getDecimalPlacesResult()
          != ExpressionConfiguration.DECIMAL_PLACES_ROUNDING_UNLIMITED) {
        bigDecimal = roundValue(bigDecimal, configuration.getDecimalPlacesResult());
      }

      if (configuration.isStripTrailingZeros()) {
        bigDecimal = bigDecimal.stripTrailingZeros();
      }

      result = NumberValue.of(bigDecimal);
    }

    return result;
  }

  public EvaluationValue evaluateSubtree(
      Solvable solvable, UnaryOperator<EvaluationContext.EvaluationContextBuilder> builder)
      throws EvaluationException {
    return this.evaluateSubtree(solvable, builder.apply(EvaluationContext.builder(this)).build());
  }

  /**
   * Evaluates only a subtree of the abstract syntax tree.
   *
   * @param solvable The {@link Solvable} to start evaluation from.
   * @return The evaluation result value.
   * @throws EvaluationException If there were problems while evaluating the expression.
   */
  public EvaluationValue evaluateSubtree(Solvable solvable, EvaluationContext context)
      throws EvaluationException {
    return solvable.solve(context);
  }

  public EvaluationValue tryRoundValue(EvaluationValue value) {
    if (value.isNumberValue()
        && configuration.getDecimalPlacesRounding()
            != ExpressionConfiguration.DECIMAL_PLACES_ROUNDING_UNLIMITED) {
      return NumberValue.of(
          roundValue(value.getNumberValue(), configuration.getDecimalPlacesRounding()));
    }
    return value;
  }

  public EvaluationValue getVariableOrConstant(Token token, EvaluationContext context)
      throws EvaluationException {
    EvaluationValue result = context.parameters().get(token.getValue());
    if (result == null) {
      result = configuration.getConstants().get(token.getValue());
    }
    if (result == null && getDataAccessor() != null) {
      result = getDataAccessor().getVariableData(token.getValue(), token, context);
    }
    if (result == null) {
      throw new EvaluationException(
          token, String.format("Variable or constant value for '%s' not found", token.getValue()));
    }
    return result;
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
    return new Expression(getExpressionString(), getSolvable(), getConfiguration());
  }

  /**
   * Converts a double value to an {@link EvaluationValue} by considering the configured {@link
   * java.math.MathContext}.
   *
   * @param value The double value to covert.
   * @return An {@link EvaluationValue} of type {@link NumberValue}.
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
}
