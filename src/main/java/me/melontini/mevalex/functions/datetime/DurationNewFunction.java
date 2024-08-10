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

import java.time.Duration;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.DurationValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/**
 * Function to create a new Duration. First parameter is required and specifies the number of days.
 * All other parameters are optional and specify hours, minutes, seconds, milliseconds and
 * nanoseconds.
 */
@FunctionParameter(name = "days")
@FunctionParameter(name = "parameters", isVarArg = true)
public class DurationNewFunction extends AbstractFunction {
  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {

    int parameterLength = parameterValues.length;

    int days = parameterValues[0].getNumberValue().intValue();
    int hours = parameterLength >= 2 ? parameterValues[1].getNumberValue().intValue() : 0;
    int minutes = parameterLength >= 3 ? parameterValues[2].getNumberValue().intValue() : 0;
    int seconds = parameterLength >= 4 ? parameterValues[3].getNumberValue().intValue() : 0;
    int millis = parameterLength >= 5 ? parameterValues[4].getNumberValue().intValue() : 0;
    int nanos = parameterLength == 6 ? parameterValues[5].getNumberValue().intValue() : 0;

    Duration duration =
        Duration.ofDays(days)
            .plusHours(hours)
            .plusMinutes(minutes)
            .plusSeconds(seconds)
            .plusMillis(millis)
            .plusNanos(nanos);

    return DurationValue.of(duration);
  }
}
