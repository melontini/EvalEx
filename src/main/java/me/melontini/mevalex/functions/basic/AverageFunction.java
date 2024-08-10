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
package me.melontini.mevalex.functions.basic;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.NumberValue;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/**
 * Returns the average (arithmetic mean) of the numeric arguments.
 *
 * @author oswaldo.bapvic.jr
 */
@FunctionParameter(name = "firstValue")
@FunctionParameter(name = "additionalValues", isVarArg = true)
public class AverageFunction extends AbstractMinMaxFunction {
  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {
    MathContext mathContext = context.expression().getConfiguration().getMathContext();
    BigDecimal sum =
        Arrays.stream(parameterValues)
            .map(EvaluationValue::getNumberValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal count = BigDecimal.valueOf(parameterValues.length);
    BigDecimal average = sum.divide(count, mathContext);
    return NumberValue.of(average);
  }
}
