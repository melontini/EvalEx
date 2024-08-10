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

import com.ezylang.evalex.parser.ParseException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorArrayTest extends BaseExpressionEvaluatorTest {

  @Test
  void testSimpleArray() throws ParseException, EvaluationException {
    List<BigDecimal> array = List.of(new BigDecimal(99));
    Expression expression = createExpression("a[0]");

    assertThat(expression.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("99");
  }

  @Test
  void testMultipleEntriesArray() throws ParseException, EvaluationException {
    List<BigDecimal> array = Arrays.asList(new BigDecimal(2), new BigDecimal(4), new BigDecimal(6));
    Expression expression = createExpression("a[0]+a[1]+a[2]");

    assertThat(expression.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("12");
  }

  @Test
  void testExpressionArray() throws ParseException, EvaluationException {
    List<BigDecimal> array = List.of(new BigDecimal(3));
    Expression expression = createExpression("a[4-x]");

    assertThat(
            expression
                .evaluate(
                    builder -> builder.parameter("a", array).parameter("x", new BigDecimal(4)))
                .getStringValue())
        .isEqualTo("3");
  }

  @Test
  void testNestedArray() throws ParseException, EvaluationException {
    List<BigDecimal> arrayA = List.of(new BigDecimal(3));
    List<BigDecimal> arrayB =
        Arrays.asList(new BigDecimal(2), new BigDecimal(4), new BigDecimal(6));
    Expression expression = createExpression("a[b[6-4]-x]");

    assertThat(
            expression
                .evaluate(
                    builder ->
                        builder
                            .parameter("a", arrayA)
                            .parameter("b", arrayB)
                            .parameter("x", new BigDecimal(6)))
                .getStringValue())
        .isEqualTo("3");
  }

  @Test
  void testStringArray() throws ParseException, EvaluationException {
    List<String> array = Arrays.asList("Hello", "beautiful", "world");
    Expression expression = createExpression("a[0] + \" \" + a[1] + \" \" + a[2]");

    assertThat(expression.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("Hello beautiful world");
  }

  @Test
  void testBooleanArray() throws ParseException, EvaluationException {
    List<Boolean> array = Arrays.asList(true, true, false);
    Expression expression = createExpression("a[0] + \" \" + a[1] + \" \" + a[2]");

    assertThat(expression.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("true true false");
  }

  @Test
  void testArrayOfArray() throws EvaluationException, ParseException {
    List<BigDecimal> subArray1 = Arrays.asList(new BigDecimal(1), new BigDecimal(2));
    List<BigDecimal> subArray2 = Arrays.asList(new BigDecimal(4), new BigDecimal(8));

    List<List<BigDecimal>> array = Arrays.asList(subArray1, subArray2);

    Expression expression1 = createExpression("a[0][0]");
    Expression expression2 = createExpression("a[0][1]");
    Expression expression3 = createExpression("a[1][0]");
    Expression expression4 = createExpression("a[1][1]");

    assertThat(expression1.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("1");
    assertThat(expression2.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("2");
    assertThat(expression3.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("4");
    assertThat(expression4.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("8");
  }

  @Test
  void testMixedArray() throws ParseException, EvaluationException {
    List<?> array = Arrays.asList("Hello", new BigDecimal(4), true);
    Expression expression1 = createExpression("a[0]");
    Expression expression2 = createExpression("a[1]");
    Expression expression3 = createExpression("a[2]");

    assertThat(expression1.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("Hello");
    assertThat(expression2.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("4");
    assertThat(expression3.evaluate(builder -> builder.parameter("a", array)).getStringValue())
        .isEqualTo("true");
  }

  @Test
  void testArrayAndList() throws EvaluationException, ParseException {
    Expression expression = createExpression("values[i-1] * factors[i-1]");

    assertThat(
            expression
                .evaluate(
                    builder ->
                        builder
                            .parameter("values", List.of(2, 3, 4))
                            .parameter("factors", new Object[] {2, 4, 6})
                            .parameter("i", 1))
                .getStringValue())
        .isEqualTo("4");
  }

  @Test
  void testArrayTypes() throws EvaluationException, ParseException {
    Expression expression =
        createExpression("decimals[1] + integers[1] + doubles[1] + strings[1] + booleans[1]");

    assertThat(
            expression
                .evaluate(
                    builder ->
                        builder
                            .parameter(
                                "decimals", new BigDecimal[] {new BigDecimal(1), new BigDecimal(2)})
                            .parameter("integers", new Integer[] {1, 2})
                            .parameter("doubles", new Double[] {1.1, 2.2})
                            .parameter("strings", new String[] {"Hello ", " World "})
                            .parameter("booleans", new Boolean[] {true, false}))
                .getStringValue())
        .isEqualTo("6.2 World false");
  }

  @Test
  void testThrowsUnsupportedDataTypeForArray() {
    assertThatThrownBy(
            () -> createExpression("a[0]").evaluate(builder -> builder.parameter("a", 125)))
        .isInstanceOf(EvaluationException.class)
        .hasMessage("Unsupported data types in operation");
  }

  @Test
  void testThrowsUnsupportedDataTypeForIndex() {
    assertThatThrownBy(
            () -> {
              List<?> array = List.of("Hello");
              createExpression("a[b]")
                  .evaluate(
                      builder -> builder.parameter("a", array).parameter("b", "anotherString"));
            })
        .isInstanceOf(EvaluationException.class)
        .hasMessage("Unsupported data types in operation");
  }

  @Test
  void testArrayIndexOutOfBounds() {
    assertThatThrownBy(
            () -> {
              List<?> array = List.of("Hello");
              createExpression("a[1]").evaluate(builder -> builder.parameter("a", array));
            })
        .isInstanceOf(EvaluationException.class)
        .hasMessage("Index 1 out of bounds for ArrayValue [StringValue(value=Hello)]");
  }

  @Test
  void testArrayNegativeIndex() {
    assertThatThrownBy(
            () -> {
              List<?> array = List.of("Hello");
              createExpression("a[-1]").evaluate(builder -> builder.parameter("a", array));
            })
        .isInstanceOf(EvaluationException.class)
        .hasMessage("Index -1 out of bounds for ArrayValue [StringValue(value=Hello)]");
  }
}
