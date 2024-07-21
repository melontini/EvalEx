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
package com.ezylang.evalex;

import static org.assertj.core.api.Assertions.assertThat;

import com.ezylang.evalex.parser.InlinedASTNode;
import com.ezylang.evalex.parser.ParseException;
import java.math.BigDecimal;
import java.util.function.UnaryOperator;
import org.junit.jupiter.api.Test;

public class ExpressionInlineTest extends BaseExpressionEvaluatorTest {

  @Test
  public void testSimpleInlinedExpression() throws ParseException, EvaluationException {
    Expression expression = parser.inlineExpression(createExpression("2 + 2"));

    assertThat(expression.evaluate(UnaryOperator.identity()).getNumberValue())
        .isEqualTo(BigDecimal.valueOf(4));
    assertThat(expression.getAbstractSyntaxTree()).isInstanceOf(InlinedASTNode.class);
  }

  @Test
  public void testConstantInlinedExpression() throws ParseException, EvaluationException {
    Expression expression = parser.inlineExpression(createExpression("2 + PI"));

    assertThat(expression.evaluate(UnaryOperator.identity()).getNumberValue())
        .isEqualTo(
            BigDecimal.valueOf(2)
                .add(
                    new BigDecimal(
                        "3.1415926535897932384626433832795028841971693993751058209749445923078")));
    assertThat(expression.getAbstractSyntaxTree()).isInstanceOf(InlinedASTNode.class);
  }

  @Test
  public void testParameterNotInlinedExpression() throws ParseException, EvaluationException {
    Expression expression = parser.inlineExpression(createExpression("cheese / 2"));

    assertThat(expression.evaluate(builder -> builder.parameter("cheese", 22)).getNumberValue())
        .isEqualTo(
            BigDecimal.valueOf(22).divide(BigDecimal.valueOf(2), configuration.getMathContext()));
    assertThat(expression.getAbstractSyntaxTree()).isNotInstanceOf(InlinedASTNode.class);
  }
}
