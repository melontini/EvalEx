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
package me.melontini.mevalex;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.function.UnaryOperator;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.parser.ParseException;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorSimpleVariablesTest extends BaseExpressionEvaluatorTest {

  @Test
  void testSingleStringVariable() throws ParseException, EvaluationException {
    Expression expression = createExpression("a");
    EvaluationValue result = expression.evaluate(builder -> builder.parameter("a", "hello"));
    assertThat(result.isStringValue()).isTrue();
    assertThat(result.getStringValue()).isEqualTo("hello");
  }

  @Test
  void testSingleNumberVariable() throws ParseException, EvaluationException {
    Expression expression = createExpression("a");
    EvaluationValue result =
        expression.evaluate(builder -> builder.parameter("a", BigDecimal.valueOf(9)));
    assertThat(result.isNumberValue()).isTrue();
    assertThat(result.getNumberValue()).isEqualTo(BigDecimal.valueOf(9));
  }

  @Test
  void testNumbers() throws ParseException, EvaluationException {
    Expression expression = createExpression("(a+b)*(a-b)");
    EvaluationValue result =
        expression.evaluate(
            builder ->
                builder
                    .parameter("a", BigDecimal.valueOf(9))
                    .parameter("b", BigDecimal.valueOf(5)));
    assertThat(result.isNumberValue()).isTrue();
    assertThat(result.getNumberValue()).isEqualTo(BigDecimal.valueOf(56));
  }

  @Test
  void testStrings() throws ParseException, EvaluationException {
    Expression expression = createExpression("prefix+infix+postfix");
    EvaluationValue result =
        expression.evaluate(
            builder ->
                builder
                    .parameter("prefix", "Hello")
                    .parameter("infix", " ")
                    .parameter("postfix", "world"));
    assertThat(result.isStringValue()).isTrue();
    assertThat(result.getStringValue()).isEqualTo("Hello world");
  }

  @Test
  void testStringNumberCombined() throws ParseException, EvaluationException {
    Expression expression = createExpression("prefix+infix+postfix");
    EvaluationValue result =
        expression.evaluate(
            builder ->
                builder
                    .parameter("prefix", "Hello")
                    .parameter("infix", BigDecimal.valueOf(2))
                    .parameter("postfix", "world"));
    assertThat(result.isStringValue()).isTrue();
    assertThat(result.getStringValue()).isEqualTo("Hello2world");
  }

  @Test
  void testUnknownVariable() {
    assertThatThrownBy(() -> createExpression("a").evaluate(UnaryOperator.identity()))
        .isInstanceOf(EvaluationException.class)
        .hasMessage("Variable or constant value for 'a' not found");
  }
}
