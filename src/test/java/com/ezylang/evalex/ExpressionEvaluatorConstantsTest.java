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
package com.ezylang.evalex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.types.NumberValue;
import com.ezylang.evalex.parser.ExpressionParser;
import com.ezylang.evalex.parser.ParseException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ExpressionEvaluatorConstantsTest extends BaseExpressionEvaluatorTest {

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "TRUE | true",
        "true | true",
        "False | false",
        "PI | "
            + " 3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679",
        "e | 2.71828182845904523536028747135266249775724709369995957496696762772407663",
        "DT_FORMAT_ISO_DATE_TIME | yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]['['VV']']",
        "DT_FORMAT_LOCAL_DATE_TIME | yyyy-MM-dd'T'HH:mm:ss[.SSS]",
        "DT_FORMAT_LOCAL_DATE | yyyy-MM-dd"
      })
  void testDefaultConstants(String expression, String expectedResult)
      throws ParseException, EvaluationException {
    assertThat(evaluate(expression)).isEqualTo(expectedResult);
  }

  @Test
  void testCustomConstantsMixedCase() throws EvaluationException, ParseException {
    Map<String, EvaluationValue> constants = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    constants.putAll(
        Map.of(
            "A", NumberValue.of(new BigDecimal("2.5")),
            "B", NumberValue.of(new BigDecimal("3.9"))));

    ExpressionParser parser =
        new ExpressionParser(ExpressionConfiguration.builder().constants(constants).build());

    Expression expression = parser.parse("a+B");

    assertThat(expression.evaluate(UnaryOperator.identity()).getStringValue()).isEqualTo("6.4");
  }

  @Test
  void testOverwriteConstantsWith() throws EvaluationException, ParseException {
    Expression expression =
        new ExpressionParser(
                ExpressionConfiguration.builder().allowOverwriteConstants(true).build())
            .parse("e");
    assertThat(expression.evaluate(builder -> builder.parameter("e", 9)).getStringValue())
        .isEqualTo("9");
  }

  @Test
  void testOverwriteConstantsWithValues() throws EvaluationException, ParseException {
    Map<String, Object> values = new HashMap<>();
    values.put("E", 6);
    Expression expression =
        new ExpressionParser(
                ExpressionConfiguration.builder().allowOverwriteConstants(true).build())
            .parse("e");
    assertThat(expression.evaluate(builder -> builder.parameters(values)).getStringValue())
        .isEqualTo("6");
  }

  @Test
  void testOverwriteConstantsNotAllowed() throws ParseException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("e");
    assertThatThrownBy(() -> expression.evaluate(builder -> builder.parameter("e", 9)))
        .isInstanceOf(UnsupportedOperationException.class)
        .hasMessage("Can't set value for constant 'e'");
  }
}
