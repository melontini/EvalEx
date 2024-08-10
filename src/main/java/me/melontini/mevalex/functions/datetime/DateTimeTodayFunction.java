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
package me.melontini.mevalex.functions.datetime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.Expression;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.DateTimeValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.functions.FunctionParameter;
import me.melontini.mevalex.parser.Token;

/**
 * Produces a new DATE_TIME that represents the current date, at midnight (00:00).
 *
 * <p>It is useful for DATE_TIME comparison, when the current time must not be considered. For
 * example, in the expression:
 *
 * <blockquote>
 *
 * {@code IF(expiryDate > DT_TODAY(), "expired", "valid")}
 *
 * </blockquote>
 *
 * <p>This function may accept an optional time zone to be applied. If no zone ID is specified, the
 * default zone ID defined at the {@link ExpressionConfiguration} will be used.
 *
 * @author oswaldobapvicjr
 */
@FunctionParameter(name = "parameters", isVarArg = true)
public class DateTimeTodayFunction extends AbstractFunction {
  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues)
      throws EvaluationException {
    ZoneId zoneId = parseZoneId(context.expression(), functionToken, parameterValues);
    Instant today = LocalDate.now().atStartOfDay(zoneId).toInstant();
    return DateTimeValue.of(today);
  }

  private ZoneId parseZoneId(
      Expression expression, Token functionToken, EvaluationValue... parameterValues)
      throws EvaluationException {
    if (parameterValues.length > 0 && !parameterValues[0].isNullValue()) {
      return ZoneIdConverter.convert(functionToken, parameterValues[0].getStringValue());
    }
    return expression.getConfiguration().getZoneId();
  }
}
