/*
  Copyright 2012-2023 Udo Klimaschewski

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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.conversion.DateTimeConverter;
import me.melontini.mevalex.data.types.DateTimeValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/**
 * Parses a date-time string to a {@link DateTimeValue} value.
 *
 * <p>Optional arguments are the time zone and a list of {@link java.time.format.DateTimeFormatter}
 * patterns. Each pattern will be tried to convert the string to a date-time. The first matching
 * pattern will be used. If <code>NULL</code> is specified for the time zone, the currently
 * configured zone is used. If no formatter is specified, the function will use the formatters
 * defined at the {@link ExpressionConfiguration}.
 */
@FunctionParameter(name = "value")
@FunctionParameter(name = "parameters", isVarArg = true)
public class DateTimeParseFunction extends AbstractFunction {

  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues)
      throws EvaluationException {

    String value = parameterValues[0].getStringValue();

    ZoneId zoneId = context.expression().getConfiguration().getZoneId();
    if (parameterValues.length > 1 && !parameterValues[1].isNullValue()) {
      zoneId = ZoneIdConverter.convert(functionToken, parameterValues[1].getStringValue());
    }

    List<DateTimeFormatter> formatters;

    if (parameterValues.length > 2) {
      formatters = new ArrayList<>();
      for (int i = 2; i < parameterValues.length; i++) {
        try {
          formatters.add(DateTimeFormatter.ofPattern(parameterValues[i].getStringValue()));
        } catch (IllegalArgumentException ex) {
          throw new EvaluationException(
              functionToken,
              String.format(
                  "Illegal date-time format in parameter %d: '%s'",
                  i + 1, parameterValues[i].getStringValue()));
        }
      }
    } else {
      formatters = context.expression().getConfiguration().getDateTimeFormatters();
    }
    DateTimeConverter converter = new DateTimeConverter();
    Instant instant = converter.parseDateTime(value, zoneId, formatters);

    if (instant == null) {
      throw new EvaluationException(
          functionToken, String.format("Unable to parse date-time string '%s'", value));
    }
    return DateTimeValue.of(instant);
  }
}
