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
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.NumberValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/** Factorial function, calculates the factorial of a base value. */
@FunctionParameter(name = "base")
public class FactFunction extends AbstractFunction {

  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {
    int number = parameterValues[0].getNumberValue().intValue();
    BigDecimal factorial = BigDecimal.ONE;
    for (int i = 1; i <= number; i++) {
      factorial =
          factorial.multiply(
              new BigDecimal(i, context.expression().getConfiguration().getMathContext()),
              context.expression().getConfiguration().getMathContext());
    }
    return NumberValue.of(factorial);
  }
}
