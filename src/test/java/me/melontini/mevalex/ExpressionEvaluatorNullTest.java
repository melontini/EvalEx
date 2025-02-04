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
package me.melontini.mevalex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.parser.ParseException;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorNullTest extends BaseExpressionEvaluatorTest {

  @Test
  void testSecondNullEquals() throws ParseException, EvaluationException {
    Expression expression = createExpression("a == null");
    assertExpressionHasExpectedResult(expression, builder -> builder.parameter("a", null), "true");
  }

  @Test
  void testSecondNullNotEquals() throws ParseException, EvaluationException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a != null");
    assertExpressionHasExpectedResult(expression, builder -> builder.parameter("a", null), "false");
  }

  @Test
  void testFirstNullEquals() throws ParseException, EvaluationException {
    Expression expression = createExpression("null == a");
    assertExpressionHasExpectedResult(expression, builder -> builder.parameter("a", null), "true");
  }

  @Test
  void testFirstNullNotEquals() throws ParseException, EvaluationException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("null != a");
    assertExpressionHasExpectedResult(expression, builder -> builder.parameter("a", null), "false");
  }

  @Test
  void testHandleWithIf() throws EvaluationException, ParseException {
    Expression expression1 = createExpression("IF(a != null, a * 5, 1)");
    assertExpressionHasExpectedResult(expression1, builder -> builder.parameter("a", null), "1");
    assertExpressionHasExpectedResult(expression1, builder -> builder.parameter("a", 3), "15");

    Expression expression2 =
        createExpression("IF(a == null, \"Unknown name\", \"The name is \" + a)");
    assertExpressionHasExpectedResult(
        expression2, builder -> builder.parameter("a", null), "Unknown name");
    assertExpressionHasExpectedResult(
        expression2, builder -> builder.parameter("a", "Max"), "The name is Max");
  }

  @Test
  void testHandleWithMaps() throws EvaluationException, ParseException {
    Expression expression = createExpression("a == null && b == null");
    Map<String, Object> values = new HashMap<>();
    values.put("a", null);
    values.put("b", null);

    assertExpressionHasExpectedResult(expression, builder -> builder.parameters(values), "true");
  }

  @Test
  void testFailWithNoHandling() throws ParseException, EvaluationException {
    Expression expression1 = createExpression("a * 5");
    assertThatThrownBy(() -> expression1.evaluate(builder -> builder.parameter("a", null)))
        .isInstanceOf(EvaluationException.class)
        .hasMessage("Unsupported data types in operation");

    Expression expression2 = createExpression("FLOOR(a)");
    assertThatThrownBy(() -> expression2.evaluate(builder -> builder.parameter("a", null)))
        .isInstanceOf(NullPointerException.class);

    Expression expression3 = createExpression("a > 5");
    assertThatThrownBy(() -> expression3.evaluate(builder -> builder.parameter("a", null)))
        .isInstanceOf(NullPointerException.class);
  }

  private void assertExpressionHasExpectedResult(
      Expression expression,
      UnaryOperator<EvaluationContext.EvaluationContextBuilder> operator,
      String expectedResult)
      throws EvaluationException, ParseException {
    assertThat(expression.evaluate(operator).getStringValue()).isEqualTo(expectedResult);
  }
}
