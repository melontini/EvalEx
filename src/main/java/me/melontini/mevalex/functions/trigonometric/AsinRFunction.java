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
package me.melontini.mevalex.functions.trigonometric;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.valueOf;

import java.math.BigDecimal;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/** Returns the arc-sine (in radians). */
@FunctionParameter(name = "value")
public class AsinRFunction extends AbstractFunction {

  private static final BigDecimal MINUS_ONE = valueOf(-1);

  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues)
      throws EvaluationException {

    BigDecimal parameterValue = parameterValues[0].getNumberValue();

    if (parameterValue.compareTo(ONE) > 0) {
      throw new EvaluationException(
          functionToken, "Illegal asinr(x) for x > 1: x = " + parameterValue);
    }
    if (parameterValue.compareTo(MINUS_ONE) < 0) {
      throw new EvaluationException(
          functionToken, "Illegal asinr(x) for x < -1: x = " + parameterValue);
    }
    return context
        .expression()
        .convertDoubleValue(Math.asin(parameterValues[0].getNumberValue().doubleValue()));
  }
}
