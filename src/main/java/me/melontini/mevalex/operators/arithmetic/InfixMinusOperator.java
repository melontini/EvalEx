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
package me.melontini.mevalex.operators.arithmetic;

import java.time.Duration;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.DateTimeValue;
import me.melontini.mevalex.data.types.DurationValue;
import me.melontini.mevalex.data.types.NumberValue;
import me.melontini.mevalex.operators.AbstractOperator;
import me.melontini.mevalex.operators.InfixOperator;
import me.melontini.mevalex.operators.OperatorIfc;
import me.melontini.mevalex.parser.Token;

/** Subtraction of two numbers. */
@InfixOperator(precedence = OperatorIfc.OPERATOR_PRECEDENCE_ADDITIVE)
public class InfixMinusOperator extends AbstractOperator {

  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token operatorToken, EvaluationValue... operands)
      throws EvaluationException {
    EvaluationValue leftOperand = operands[0];
    EvaluationValue rightOperand = operands[1];

    if (leftOperand.isNumberValue() && rightOperand.isNumberValue()) {
      return NumberValue.of(
          leftOperand
              .getNumberValue()
              .subtract(
                  rightOperand.getNumberValue(),
                  context.expression().getConfiguration().getMathContext()));

    } else if (leftOperand.isDateTimeValue() && rightOperand.isDateTimeValue()) {
      return DurationValue.of(
          Duration.ofMillis(
              leftOperand.getDateTimeValue().toEpochMilli()
                  - rightOperand.getDateTimeValue().toEpochMilli()));

    } else if (leftOperand.isDateTimeValue() && rightOperand.isDurationValue()) {
      return DateTimeValue.of(
          leftOperand.getDateTimeValue().minus(rightOperand.getDurationValue()));
    } else if (leftOperand.isDurationValue() && rightOperand.isDurationValue()) {
      return DurationValue.of(
          leftOperand.getDurationValue().minus(rightOperand.getDurationValue()));
    } else if (leftOperand.isDateTimeValue() && rightOperand.isNumberValue()) {
      return DateTimeValue.of(
          leftOperand
              .getDateTimeValue()
              .minus(Duration.ofMillis(rightOperand.getNumberValue().longValue())));
    } else {
      throw EvaluationException.ofUnsupportedDataTypeInOperation(operatorToken);
    }
  }
}
