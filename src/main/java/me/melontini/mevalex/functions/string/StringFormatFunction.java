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
package me.melontini.mevalex.functions.string;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.IntStream;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.StringValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/**
 * Returns a formatted string using the specified format string and arguments, using the configured
 * locale.
 *
 * <p>For example:
 *
 * <blockquote>
 *
 * {@code STR_FORMAT("Welcome to %s!", "EvalEx")}
 *
 * </blockquote>
 *
 * <p>The result is produced using {@link String#format(String, Object...)}.
 *
 * @author oswaldobapvicjr
 */
@FunctionParameter(name = "format")
@FunctionParameter(name = "arguments", isVarArg = true)
public class StringFormatFunction extends AbstractFunction {
  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {
    String format = parameterValues[0].getStringValue();
    Object[] arguments =
        getFormatArguments(parameterValues, context.expression().getConfiguration());
    return StringValue.of(
        String.format(context.expression().getConfiguration().getLocale(), format, arguments));
  }

  private Object[] getFormatArguments(
      EvaluationValue[] parameterValues, ExpressionConfiguration configuration) {
    if (parameterValues.length > 1) {
      return convertParametersToObjects(parameterValues, configuration);
    }
    return new Object[0];
  }

  private Object[] convertParametersToObjects(
      EvaluationValue[] parameterValues, ExpressionConfiguration configuration) {
    return IntStream.range(1, parameterValues.length)
        .mapToObj(i -> convertParameterToObject(parameterValues[i], configuration))
        .toArray();
  }

  private Object convertParameterToObject(
      EvaluationValue parameterValue, ExpressionConfiguration configuration) {
    if (parameterValue.isDateTimeValue()) {
      return convertInstantToLocalDateTime(
          parameterValue.getDateTimeValue(), configuration.getZoneId());
    } else {
      return parameterValue.getValue();
    }
  }

  private ZonedDateTime convertInstantToLocalDateTime(Instant instant, ZoneId zoneId) {
    return instant.atZone(zoneId);
  }
}
