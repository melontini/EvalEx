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

import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/** Returns the hyperbolic arc-sine. */
@FunctionParameter(name = "value")
public class AtanHFunction extends AbstractFunction {
  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues)
      throws EvaluationException {

    /* Formula: atanh(x) = 0.5*ln((1 + x)/(1 - x)) */
    double value = parameterValues[0].getNumberValue().doubleValue();
    if (Math.abs(value) >= 1) {
      throw new EvaluationException(functionToken, "Absolute value must be less than 1");
    }
    return context.expression().convertDoubleValue(0.5 * Math.log((1 + value) / (1 - value)));
  }
}
