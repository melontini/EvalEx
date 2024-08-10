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

import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.NumberValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/**
 * Rounds the given value to the specified scale, using the {@link java.math.MathContext} of the
 * expression configuration.
 */
@FunctionParameter(name = "value")
@FunctionParameter(name = "scale")
public class RoundFunction extends AbstractFunction {
  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {

    EvaluationValue value = parameterValues[0];
    EvaluationValue precision = parameterValues[1];

    return NumberValue.of(
        value
            .getNumberValue()
            .setScale(
                precision.getNumberValue().intValue(),
                context.expression().getConfiguration().getMathContext().getRoundingMode()));
  }
}
