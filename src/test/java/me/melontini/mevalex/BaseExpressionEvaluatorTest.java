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

import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.config.TestConfigurationProvider;
import me.melontini.mevalex.parser.ExpressionParser;
import me.melontini.mevalex.parser.ParseException;

public abstract class BaseExpressionEvaluatorTest {

  final ExpressionConfiguration configuration =
      TestConfigurationProvider.StandardConfigurationWithAdditionalTestOperators;
  final ExpressionParser parser =
      TestConfigurationProvider.StandardParserWithAdditionalTestOperators;

  String evaluate(String expressionString) throws ParseException, EvaluationException {
    Expression expression = createExpression(expressionString);
    return expression.evaluate(EvaluationContext.builder(expression).build()).getStringValue();
  }

  Expression createExpression(String expressionString) throws ParseException {
    return parser.parse(expressionString);
  }
}
