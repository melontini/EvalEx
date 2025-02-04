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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.parser.ExpressionParser;
import me.melontini.mevalex.parser.ParseException;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorDecimalPlacesTest extends BaseExpressionEvaluatorTest {

  @Test
  void testDefaultNoRoundingLiteral() throws ParseException, EvaluationException {
    assertThat(evaluate("2.12345")).isEqualTo("2.12345");
  }

  @Test
  void testDefaultNoRoundingVariable() throws ParseException, EvaluationException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a");

    assertThat(
            expression
                .evaluate(builder -> builder.parameter("a", new BigDecimal("2.12345")))
                .getStringValue())
        .isEqualTo("2.12345");
  }

  @Test
  void testDefaultNoRoundingInfixOperator() throws ParseException, EvaluationException {
    assertThat(evaluate("2.12345+1.54321")).isEqualTo("3.66666");
  }

  @Test
  void testDefaultNoRoundingPrefixOperator() throws ParseException, EvaluationException {
    assertThat(evaluate("-2.12345")).isEqualTo("-2.12345");
  }

  @Test
  void testDefaultNoRoundingFunction() throws ParseException, EvaluationException {
    assertThat(evaluate("SUM(2.12345,1.54321)")).isEqualTo("3.66666");
  }

  @Test
  void testDefaultNoRoundingArray() throws ParseException, EvaluationException {
    List<BigDecimal> array = List.of(new BigDecimal("1.12345"));
    Expression expression = createExpression("a[0]");

    assertThat(expression.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("1.12345");
  }

  @Test
  void testDefaultNoRoundingStructure() throws ParseException, EvaluationException {
    Map<String, BigDecimal> structure = Map.of("b", new BigDecimal("1.12345"));

    Expression expression = createExpression("a.b");

    assertThat(expression.evaluate(builder -> builder.parameter("a", structure)).getStringValue())
        .isEqualTo("1.12345");
  }

  @Test
  void testCustomRoundingDecimalsLiteral() throws ParseException, EvaluationException {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder().decimalPlacesRounding(2).build();
    Expression expression = new ExpressionParser(config).parse("2.12345");

    assertThat(expression.evaluate(UnaryOperator.identity()).getStringValue()).isEqualTo("2.12");
  }

  @Test
  void testCustomRoundingDecimalsVariable() throws ParseException, EvaluationException {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder().decimalPlacesRounding(2).build();
    Expression expression = new ExpressionParser(config).parse("a");

    assertThat(
            expression
                .evaluate(builder -> builder.parameter("a", new BigDecimal("2.126")))
                .getStringValue())
        .isEqualTo("2.13");
  }

  @Test
  void testCustomRoundingDecimalsInfixOperator() throws ParseException, EvaluationException {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder().decimalPlacesRounding(3).build();
    Expression expression = new ExpressionParser(config).parse("2.12345+1.54321");

    // literals are rounded first, the added and rounded again.
    assertThat(expression.evaluate(UnaryOperator.identity()).getStringValue()).isEqualTo("3.666");
  }

  @Test
  void testCustomRoundingDecimalsPrefixOperator() throws ParseException, EvaluationException {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder().decimalPlacesRounding(3).build();
    Expression expression = new ExpressionParser(config).parse("-2.12345");

    assertThat(expression.evaluate(UnaryOperator.identity()).getStringValue()).isEqualTo("-2.123");
  }

  @Test
  void testCustomRoundingDecimalsFunction() throws ParseException, EvaluationException {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder().decimalPlacesRounding(3).build();
    Expression expression = new ExpressionParser(config).parse("SUM(2.12345,1.54321)");

    // literals are rounded first, the added and rounded again.
    assertThat(expression.evaluate(UnaryOperator.identity()).getStringValue()).isEqualTo("3.666");
  }

  @Test
  void testCustomRoundingDecimalsArray() throws ParseException, EvaluationException {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder().decimalPlacesRounding(3).build();
    List<BigDecimal> array = List.of(new BigDecimal("1.12345"));
    Expression expression = new ExpressionParser(config).parse("a[0]");

    assertThat(expression.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("1.123");
  }

  @Test
  void testCustomRoundingStructure() throws ParseException, EvaluationException {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder().decimalPlacesRounding(3).build();
    Map<String, BigDecimal> structure = Map.of("b", new BigDecimal("1.12345"));

    Expression expression = new ExpressionParser(config).parse("a.b");

    assertThat(expression.evaluate(builder -> builder.parameter("a", structure)).getStringValue())
        .isEqualTo("1.123");
  }

  @Test
  void testDefaultStripZeros() throws EvaluationException, ParseException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("9.000");
    assertThat(expression.evaluate(UnaryOperator.identity()).getNumberValue()).isEqualTo("9");
  }

  @Test
  void testDoNotStripZeros() throws EvaluationException, ParseException {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder().stripTrailingZeros(false).build();

    Expression expression = new ExpressionParser(config).parse("9.000");
    assertThat(expression.evaluate(UnaryOperator.identity()).getNumberValue()).isEqualTo("9.000");
  }

  @Test
  void testDecimalPlacesResult() throws EvaluationException, ParseException {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder().decimalPlacesResult(3).build();
    Expression expression = new ExpressionParser(config).parse("1.6666+1.6666+1.6666");

    assertThat(expression.evaluate(UnaryOperator.identity()).getStringValue()).isEqualTo("5");
  }

  @Test
  void testDecimalPlacesResultNoStrip() throws EvaluationException, ParseException {
    Expression expression =
        new ExpressionParser(
                ExpressionConfiguration.builder()
                    .decimalPlacesResult(3)
                    .stripTrailingZeros(false)
                    .build())
            .parse("1.6666+1.6666+1.6666");

    assertThat(expression.evaluate(UnaryOperator.identity()).getStringValue()).isEqualTo("5.000");
  }

  @Test
  void testDecimalPlacesResultAndAuto() throws EvaluationException, ParseException {
    Expression expression =
        new ExpressionParser(
                ExpressionConfiguration.builder()
                    .decimalPlacesResult(3)
                    .decimalPlacesRounding(2)
                    .build())
            .parse("1.6666+1.6666+1.6666");

    assertThat(expression.evaluate(UnaryOperator.identity()).getStringValue()).isEqualTo("5.01");
  }

  @Test
  void testEqualsIgnoresScale() throws EvaluationException, ParseException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a == b");
    EvaluationValue result =
        expression.evaluate(builder -> builder.parameter("a", 70).parameter("b", 70.0));

    assertThat(result.getBooleanValue()).isTrue();
  }

  @Test
  void testNotEqualsIgnoresScale() throws EvaluationException, ParseException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a != b");
    EvaluationValue result =
        expression.evaluate(builder -> builder.parameter("a", 70).parameter("b", 70.0));

    assertThat(result.getBooleanValue()).isFalse();
  }
}
