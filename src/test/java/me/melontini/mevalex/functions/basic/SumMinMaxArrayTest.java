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
package me.melontini.mevalex.functions.basic;

import static org.assertj.core.api.Assertions.assertThat;

import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.Expression;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.parser.ParseException;
import org.junit.jupiter.api.Test;

class SumMinMaxArrayTest {

  @Test
  void testSumSingleArray() throws EvaluationException, ParseException {
    Integer[] numbers = {1, 2, 3};

    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("SUM(numbers)");

    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("6");
  }

  @Test
  void testSumMultipleArray() throws EvaluationException, ParseException {
    Integer[] numbers1 = {1, 2, 3};
    Integer[] numbers2 = {4, 5, 6};

    Expression expression =
        ExpressionConfiguration.defaultExpressionParser().parse("SUM(numbers1, numbers2)");

    assertThat(
            expression
                .evaluate(
                    builder ->
                        builder.parameter("numbers1", numbers1).parameter("numbers2", numbers2))
                .getStringValue())
        .isEqualTo("21");
  }

  @Test
  void testSumMixedArrayNumber() throws EvaluationException, ParseException {
    Integer[] numbers = {1, 2, 3};

    Expression expression =
        ExpressionConfiguration.defaultExpressionParser().parse("SUM(numbers, 4)");

    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("10");
  }

  @Test
  void testSumNestedArray() throws EvaluationException, ParseException {
    Integer[][] numbers = {{1, 2, 3}, {4, 5, 6}, {7, 8, 9}};

    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("SUM(numbers)");
    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("45");
  }

  @Test
  void testMinSingleArray() throws EvaluationException, ParseException {
    Integer[] numbers = {5, 2, 3};

    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("MIN(numbers)");

    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("2");
  }

  @Test
  void testMinMultipleArray() throws EvaluationException, ParseException {
    Integer[] numbers1 = {5, 8, 3};
    Integer[] numbers2 = {9, 2, 6};

    Expression expression =
        ExpressionConfiguration.defaultExpressionParser().parse("MIN(numbers1, numbers2)");

    assertThat(
            expression
                .evaluate(
                    builder ->
                        builder.parameter("numbers1", numbers1).parameter("numbers2", numbers2))
                .getStringValue())
        .isEqualTo("2");
  }

  @Test
  void testMinMixedArrayNumberMinNumber() throws EvaluationException, ParseException {
    Integer[] numbers = {1, 2, 3};

    Expression expression =
        ExpressionConfiguration.defaultExpressionParser().parse("MIN(numbers, 0)");

    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("0");
  }

  @Test
  void testMinMixedArrayNumberMinArray() throws EvaluationException, ParseException {
    Integer[] numbers = {8, 2, 3};

    Expression expression =
        ExpressionConfiguration.defaultExpressionParser().parse("MIN(numbers, 7)");

    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("2");
  }

  @Test
  void testMinNestedArray() throws EvaluationException, ParseException {
    Integer[][] numbers = {{4, 5, 6}, {1, 2, 3}, {7, 8, 9}};

    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("MIN(numbers)");
    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("1");
  }

  @Test
  void testMaxSingleArray() throws EvaluationException, ParseException {
    Integer[] numbers = {1, 5, 3};

    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("MAX(numbers)");

    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("5");
  }

  @Test
  void testMaxMultipleArray() throws EvaluationException, ParseException {
    Integer[] numbers1 = {5, 2, 3};
    Integer[] numbers2 = {4, 9, 6};

    Expression expression =
        ExpressionConfiguration.defaultExpressionParser().parse("MAX(numbers1, numbers2)");

    assertThat(
            expression
                .evaluate(
                    builder ->
                        builder.parameter("numbers1", numbers1).parameter("numbers2", numbers2))
                .getStringValue())
        .isEqualTo("9");
  }

  @Test
  void testMaxMixedArrayNumberMaxNumber() throws EvaluationException, ParseException {
    Integer[] numbers = {1, 2, 3};

    Expression expression =
        ExpressionConfiguration.defaultExpressionParser().parse("MAX(numbers, 4)");

    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("4");
  }

  @Test
  void testMaxMixedArrayNumberMaxArray() throws EvaluationException, ParseException {
    Integer[] numbers = {1, 9, 3};

    Expression expression =
        ExpressionConfiguration.defaultExpressionParser().parse("MAX(numbers, 4)");

    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("9");
  }

  @Test
  void testMaxNestedArray() throws EvaluationException, ParseException {
    Integer[][] numbers = {{1, 2, 3}, {7, 8, 9}, {4, 5, 6}};

    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("MAX(numbers)");
    assertThat(
            expression.evaluate(builder -> builder.parameter("numbers", numbers)).getStringValue())
        .isEqualTo("9");
  }
}
