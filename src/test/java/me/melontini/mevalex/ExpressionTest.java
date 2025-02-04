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

import java.math.MathContext;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.DataAccessorIfc;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.StringValue;
import me.melontini.mevalex.parser.ExpressionParser;
import me.melontini.mevalex.parser.ParseException;
import me.melontini.mevalex.parser.Solvable;
import me.melontini.mevalex.parser.Token;
import org.junit.jupiter.api.Test;

class ExpressionTest {

  @Test
  void testExpressionDefaults() throws ParseException, EvaluationException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a+b");

    assertThat(expression.getExpressionString()).isEqualTo("a+b");
    assertThat(expression.getConfiguration().getMathContext())
        .isEqualTo(ExpressionConfiguration.DEFAULT_MATH_CONTEXT);
    assertThat(expression.getConfiguration().getFunctionDictionary().hasFunction("SUM")).isTrue();
    assertThat(expression.getConfiguration().getOperatorDictionary().hasInfixOperator("+"))
        .isTrue();
    assertThat(expression.getConfiguration().getOperatorDictionary().hasPrefixOperator("+"))
        .isTrue();
    assertThat(expression.getConfiguration().getOperatorDictionary().hasPostfixOperator("+"))
        .isFalse();
  }

  @Test
  void testValidateOK() throws ParseException, EvaluationException {
    ExpressionConfiguration.defaultExpressionParser().parse("1+1");
  }

  @Test
  void testValidateFail() {
    assertThatThrownBy(() -> ExpressionConfiguration.defaultExpressionParser().parse("2#3"))
        .isInstanceOf(ParseException.class)
        .hasMessage("Undefined operator '#'");
  }

  @Test
  void testExpressionNode() throws ParseException, EvaluationException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a*b");
    Solvable subExpression =
        ExpressionConfiguration.defaultExpressionParser().parse("4+3").getSolvable();

    EvaluationValue result =
        expression.evaluate(builder -> builder.parameter("a", 2).parameter("b", subExpression));

    assertThat(result.getStringValue()).isEqualTo("14");
  }

  @Test
  void testWithValues() throws ParseException, EvaluationException {
    Expression expression =
        ExpressionConfiguration.defaultExpressionParser().parse("(a + b) * (a - b)");

    Map<String, Object> values = new HashMap<>();
    values.put("a", 3.5);
    values.put("b", 2.5);

    EvaluationValue result = expression.evaluate(builder -> builder.parameters(values));

    assertThat(result.getStringValue()).isEqualTo("6");
  }

  @Test
  void testWithValuesDoubleMap() throws ParseException, EvaluationException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a+b");

    Map<String, Double> values = new HashMap<>();
    values.put("a", 3.9);
    values.put("b", 3.1);

    EvaluationValue result = expression.evaluate(builder -> builder.parameters(values));

    assertThat(result.getStringValue()).isEqualTo("7");
  }

  @Test
  void testWithValuesStringMap() throws ParseException, EvaluationException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a+b+c");

    Map<String, String> values = new HashMap<>();
    values.put("a", "Hello");
    values.put("b", " ");
    values.put("c", "world");

    EvaluationValue result = expression.evaluate(builder -> builder.parameters(values));

    assertThat(result.getStringValue()).isEqualTo("Hello world");
  }

  @Test
  void testWithValuesMixedMap() throws ParseException, EvaluationException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a+b+c");

    Map<String, Object> values = new HashMap<>();
    values.put("a", true);
    values.put("b", " ");
    values.put("c", 24.7);

    EvaluationValue result = expression.evaluate(builder -> builder.parameters(values));

    assertThat(result.getStringValue()).isEqualTo("true 24.7");
  }

  @SuppressWarnings("Convert2Lambda")
  @Test
  void testDefaultExpressionOwnsOwnConfigurationEntries()
      throws ParseException, EvaluationException {
    Supplier<ExpressionConfiguration> configuration =
        () ->
            ExpressionConfiguration.builder()
                .dataAccessorSupplier(
                    () ->
                        new DataAccessorIfc() {
                          @Override
                          public EvaluationValue getVariableData(
                              String variable, Token token, EvaluationContext context) {
                            return StringValue.of(variable);
                          }
                        })
                .build();
    Expression expression1 = new ExpressionParser(configuration.get()).parse("1+1");
    Expression expression2 = new ExpressionParser(configuration.get()).parse("1+1");

    assertThat(expression1.getDataAccessor()).isNotSameAs(expression2.getDataAccessor());
    assertThat(expression1.getConfiguration().getOperatorDictionary())
        .isNotSameAs(expression2.getConfiguration().getOperatorDictionary());
    assertThat(expression1.getConfiguration().getFunctionDictionary())
        .isNotSameAs(expression2.getConfiguration().getFunctionDictionary());
    assertThat(expression1.getConfiguration().getConstants())
        .isNotSameAs(expression2.getConfiguration().getConstants());
  }

  @Test
  void testDoubleConverterDefaultMathContext() throws ParseException, EvaluationException {
    Expression defaultMathContextExpression =
        ExpressionConfiguration.defaultExpressionParser().parse("1");
    assertThat(defaultMathContextExpression.convertDoubleValue(1.67987654321).getNumberValue())
        .isEqualByComparingTo("1.67987654321");
  }

  @Test
  void testDoubleConverterLimitedMathContext() throws ParseException, EvaluationException {
    Expression limitedMathContextExpression =
        new ExpressionParser(
                ExpressionConfiguration.builder().mathContext(new MathContext(3)).build())
            .parse("1");
    assertThat(limitedMathContextExpression.convertDoubleValue(1.6789).getNumberValue())
        .isEqualByComparingTo("1.68");
  }

  @Test
  void testCopy() throws ParseException, EvaluationException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a + b");
    Expression copiedExpression = expression.copy();

    EvaluationValue result =
        expression.evaluate(builder -> builder.parameter("a", 1).parameter("b", 2));
    EvaluationValue copiedResult =
        copiedExpression.evaluate(builder -> builder.parameter("a", 3).parameter("b", 4));

    assertThat(result.getStringValue()).isEqualTo("3");
    assertThat(copiedResult.getStringValue()).isEqualTo("7");
  }

  @Test
  void testCopyCreatesNewDataAccessor() throws ParseException {
    // Expression expression = new Expression(("a"));
    // Expression expressionCopy = expression.copy();
    //
    // expression.getDataAccessor().setData("a", EvaluationValue.stringValue("1"));
    // expressionCopy.getDataAccessor().setData("a", EvaluationValue.stringValue("2"));
    //
    // assertThat(expression.getDataAccessor().getData("a"))
    //    .isEqualTo(EvaluationValue.stringValue("1"));
    // assertThat(expressionCopy.getDataAccessor().getData("a"))
    //    .isEqualTo(EvaluationValue.stringValue("2"));
  }
}
