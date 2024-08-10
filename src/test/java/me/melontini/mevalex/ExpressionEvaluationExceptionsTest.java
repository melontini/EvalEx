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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.parser.ASTNode;
import me.melontini.mevalex.parser.ParseException;
import me.melontini.mevalex.parser.Token;
import org.junit.jupiter.api.Test;

class ExpressionEvaluationExceptionsTest {

  @Test
  void testUnexpectedToken() throws ParseException {
    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("1");

    assertThatThrownBy(
            () -> {
              ASTNode node = ASTNode.of(new Token(1, "(", Token.TokenType.BRACE_OPEN));
              ExpressionConfiguration.defaultExpressionParser().toSolvable(node);
            })
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Unexpected evaluation token: Token(startPosition=1, value=(, type=BRACE_OPEN)");
  }
}
