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

import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.config.TestConfigurationProvider;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.parser.ExpressionParser;
import me.melontini.mevalex.parser.ParseException;

public abstract class BaseEvaluationTest {

  protected void assertExpressionHasExpectedResult(String expression, String expectedResult)
      throws EvaluationException, ParseException {
    assertThat(evaluate(expression).getStringValue()).isEqualTo(expectedResult);
  }

  protected void assertExpressionHasExpectedResult(
      String expression, String expectedResult, ExpressionConfiguration expressionConfiguration)
      throws EvaluationException, ParseException {
    assertThat(evaluate(expression, expressionConfiguration).getStringValue())
        .isEqualTo(expectedResult);
  }

  protected EvaluationValue evaluate(String expressionString)
      throws EvaluationException, ParseException {
    Expression expression =
        TestConfigurationProvider.StandardParserWithAdditionalTestOperators.parse(expressionString);
    return expression.evaluate(EvaluationContext.builder(expression).build());
  }

  private EvaluationValue evaluate(String expressionString, ExpressionConfiguration configuration)
      throws EvaluationException, ParseException {
    ExpressionParser parser = new ExpressionParser(configuration);
    Expression expression = parser.parse(expressionString);

    return expression.evaluate(EvaluationContext.builder(expression).build());
  }
}
