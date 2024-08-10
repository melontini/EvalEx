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
package me.melontini.mevalex.functions.basic;

import java.math.BigDecimal;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.Expression;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.NumberValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/** Returns the sum value of all parameters. */
@FunctionParameter(name = "value", isVarArg = true)
public class SumFunction extends AbstractFunction {
  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {
    BigDecimal sum = BigDecimal.ZERO;
    for (EvaluationValue parameter : parameterValues) {
      sum =
          sum.add(
              recursiveSum(parameter, context.expression()),
              context.expression().getConfiguration().getMathContext());
    }
    return NumberValue.of(sum);
  }

  private BigDecimal recursiveSum(EvaluationValue parameter, Expression expression) {
    BigDecimal sum = BigDecimal.ZERO;
    if (parameter.isArrayValue()) {
      for (EvaluationValue element : parameter.getArrayValue()) {
        sum =
            sum.add(
                recursiveSum(element, expression), expression.getConfiguration().getMathContext());
      }
    } else {
      sum = sum.add(parameter.getNumberValue(), expression.getConfiguration().getMathContext());
    }
    return sum;
  }
}
