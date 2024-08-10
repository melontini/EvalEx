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
package me.melontini.mevalex.functions.datetime;

import java.math.BigDecimal;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.NumberValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/** Function to convert a DATE_TIME value to milliseconds in the epoch of 1970-01-01T00:00:00Z. */
@FunctionParameter(name = "value")
public class DateTimeToEpochFunction extends AbstractFunction {
  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {
    return NumberValue.of(BigDecimal.valueOf(parameterValues[0].getDateTimeValue().toEpochMilli()));
  }
}
