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
package me.melontini.mevalex.operators;

import static me.melontini.mevalex.operators.OperatorIfc.OPERATOR_PRECEDENCE_MULTIPLICATIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.StringValue;
import me.melontini.mevalex.parser.Token;
import org.junit.jupiter.api.Test;

class OperatorTest {

  @Test
  void testPrefixOperator() {
    OperatorIfc operator = new CorrectPrefixOperator();

    assertThat(operator.getPrecedence()).isEqualTo(OperatorIfc.OPERATOR_PRECEDENCE_UNARY);
    assertThat(operator.isLeftAssociative()).isFalse();

    assertThat(operator.isPrefix()).isTrue();
    assertThat(operator.isPostfix()).isFalse();
    assertThat(operator.isInfix()).isFalse();
  }

  @Test
  void testPostfixOperator() {
    OperatorIfc operator = new CorrectPostfixOperator();

    assertThat(operator.getPrecedence()).isEqualTo(88);
    assertThat(operator.isLeftAssociative()).isTrue();

    assertThat(operator.isPrefix()).isFalse();
    assertThat(operator.isPostfix()).isTrue();
    assertThat(operator.isInfix()).isFalse();
  }

  @Test
  void testInfixOperator() {
    OperatorIfc operator = new CorrectInfixOperator();

    assertThat(operator.getPrecedence()).isEqualTo(OperatorIfc.OPERATOR_PRECEDENCE_MULTIPLICATIVE);
    assertThat(operator.isLeftAssociative()).isTrue();

    assertThat(operator.isPrefix()).isFalse();
    assertThat(operator.isPostfix()).isFalse();
    assertThat(operator.isInfix()).isTrue();
  }

  @Test
  void testThrowsFunctionParameterAnnotationNotFoundException() {

    assertThatThrownBy(DummyAnnotationOperator::new)
        .isInstanceOf(OperatorAnnotationNotFoundException.class)
        .hasMessage(
            "Operator annotation for"
                + " 'me.melontini.mevalex.operators.OperatorTest$DummyAnnotationOperator' not"
                + " found");
  }

  @PrefixOperator(leftAssociative = false)
  private static class CorrectPrefixOperator extends DummyAnnotationOperator {}

  @PostfixOperator(precedence = 88)
  private static class CorrectPostfixOperator extends DummyAnnotationOperator {}

  @InfixOperator(precedence = OPERATOR_PRECEDENCE_MULTIPLICATIVE)
  private static class CorrectInfixOperator extends DummyAnnotationOperator {}

  private static class DummyAnnotationOperator extends AbstractOperator {
    @Override
    public EvaluationValue evaluate(
        EvaluationContext context, Token operatorToken, EvaluationValue... operands) {
      return StringValue.of("OK");
    }
  }
}
