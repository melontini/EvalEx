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
package com.ezylang.evalex.functions.datetime;

import com.ezylang.evalex.EvaluationContext;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.parser.Token;

/** Function to convert a DATE_TIME value to milliseconds in the epoch of 1970-01-01T00:00:00Z. */
@FunctionParameter(name = "value")
public class DateTimeToEpochFunction extends AbstractFunction {
  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {
    return context.expression().convertValue(parameterValues[0].getDateTimeValue().toEpochMilli());
  }
}
