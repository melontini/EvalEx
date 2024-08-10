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

import java.util.function.UnaryOperator;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.operators.OperatorIfc;
import me.melontini.mevalex.parser.ExpressionParser;
import me.melontini.mevalex.parser.ParseException;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorPowerOfTest extends BaseExpressionEvaluatorTest {

  @Test
  void testPrecedenceDefault() throws ParseException, EvaluationException {
    assertThat(evaluate("-2^2")).isEqualTo("4");
  }

  @Test
  void testPrecedenceHigher() throws ParseException, EvaluationException {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder()
            .powerOfPrecedence(OperatorIfc.OPERATOR_PRECEDENCE_POWER_HIGHER)
            .build();

    Expression expression = new ExpressionParser(config).parse("-2^2");

    assertThat(expression.evaluate(UnaryOperator.identity()).getStringValue()).isEqualTo("-4");
  }
}
