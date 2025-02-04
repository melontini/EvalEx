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
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.DateTimeValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.parser.Token;

/**
 * Produces a new DATE_TIME that represents the current date and time.
 *
 * <p>It is useful to calculate a value based on the current date and time. For example, if you know
 * the start DATE_TIME of a running process, you might use the following expression to find the
 * DURATION that represents the process age:
 *
 * <blockquote>
 *
 * {@code DT_NOW() - startDateTime}
 *
 * </blockquote>
 *
 * @author oswaldobapvicjr
 */
public class DateTimeNowFunction extends AbstractFunction {
  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {
    return DateTimeValue.of(Instant.now());
  }

  @Override
  public boolean canInline() {
    return false;
  }
}
