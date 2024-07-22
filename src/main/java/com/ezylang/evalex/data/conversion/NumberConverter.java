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
package com.ezylang.evalex.data.conversion;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import java.math.BigDecimal;
import java.math.BigInteger;

/** Converter to convert to the NUMBER data type. */
public class NumberConverter implements ConverterIfc {

  @Override
  public EvaluationValue convert(Object object, ExpressionConfiguration configuration) {
    if (object instanceof BigDecimal decimal) {
      return EvaluationValue.numberValue(decimal);
    } else if (object instanceof BigInteger integer) {
      return EvaluationValue.numberValue(new BigDecimal(integer, configuration.getMathContext()));
    } else if (object instanceof Double d) {
      return EvaluationValue.numberValue(
          new BigDecimal(Double.toString(d), configuration.getMathContext()));
    } else if (object instanceof Float f) {
      return EvaluationValue.numberValue(BigDecimal.valueOf(f));
    } else if (object instanceof Integer i) {
      return EvaluationValue.numberValue(BigDecimal.valueOf(i));
    } else if (object instanceof Long l) {
      return EvaluationValue.numberValue(BigDecimal.valueOf(l));
    } else if (object instanceof Short s) {
      return EvaluationValue.numberValue(BigDecimal.valueOf(s));
    } else if (object instanceof Byte b) {
      return EvaluationValue.numberValue(BigDecimal.valueOf(b));
    } else {
      throw illegalArgument(object);
    }
  }

  @Override
  public boolean canConvert(Object object) {
    return (object instanceof BigDecimal
        || object instanceof BigInteger
        || object instanceof Double
        || object instanceof Float
        || object instanceof Integer
        || object instanceof Long
        || object instanceof Short
        || object instanceof Byte);
  }
}
