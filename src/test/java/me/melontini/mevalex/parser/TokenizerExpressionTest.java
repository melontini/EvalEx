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
package me.melontini.mevalex.parser;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TokenizerExpressionTest extends BaseParserTest {

  @Test
  void testSingleNumber() throws ParseException {
    assertAllTokensParsedCorrectly("1", new Token(1, "1", Token.TokenType.NUMBER_LITERAL));
  }

  @Test
  void testSingleVariable() throws ParseException {
    assertAllTokensParsedCorrectly("a", new Token(1, "a", Token.TokenType.VARIABLE_OR_CONSTANT));
  }

  @Test
  void testSimple() throws ParseException {
    assertAllTokensParsedCorrectly(
        "a+123",
        new Token(1, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(2, "+", Token.TokenType.INFIX_OPERATOR),
        new Token(3, "123", Token.TokenType.NUMBER_LITERAL));
  }

  @Test
  void testTwo() throws ParseException {
    assertAllTokensParsedCorrectly(
        "a+123+c",
        new Token(1, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(2, "+", Token.TokenType.INFIX_OPERATOR),
        new Token(3, "123", Token.TokenType.NUMBER_LITERAL),
        new Token(6, "+", Token.TokenType.INFIX_OPERATOR),
        new Token(7, "c", Token.TokenType.VARIABLE_OR_CONSTANT));
  }

  @Test
  void testUndefinedOperator() {
    assertThatThrownBy(() -> new Tokenizer(configuration).parse("a $ b"))
        .isEqualTo(new ParseException(3, 3, "$", "Undefined operator '$'"));
  }
}
