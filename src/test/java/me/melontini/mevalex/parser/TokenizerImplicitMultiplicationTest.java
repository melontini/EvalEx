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

import me.melontini.mevalex.config.ExpressionConfiguration;
import org.junit.jupiter.api.Test;

class TokenizerImplicitMultiplicationTest extends BaseParserTest {

  @Test
  void testImplicitBraces() throws ParseException {
    assertAllTokensParsedCorrectly(
        "(a+b)(a-b)",
        new Token(1, "(", Token.TokenType.BRACE_OPEN),
        new Token(2, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(3, "+", Token.TokenType.INFIX_OPERATOR),
        new Token(4, "b", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(5, ")", Token.TokenType.BRACE_CLOSE),
        new Token(6, "*", Token.TokenType.INFIX_OPERATOR),
        new Token(6, "(", Token.TokenType.BRACE_OPEN),
        new Token(7, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(8, "-", Token.TokenType.INFIX_OPERATOR),
        new Token(9, "b", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(10, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testImplicitNumberBraces() throws ParseException {
    assertAllTokensParsedCorrectly(
        "2(x)",
        new Token(1, "2", Token.TokenType.NUMBER_LITERAL),
        new Token(2, "*", Token.TokenType.INFIX_OPERATOR),
        new Token(2, "(", Token.TokenType.BRACE_OPEN),
        new Token(3, "x", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(4, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testImplicitNumberNoBraces() throws ParseException {
    assertAllTokensParsedCorrectly(
        "2x",
        new Token(1, "2", Token.TokenType.NUMBER_LITERAL),
        new Token(2, "*", Token.TokenType.INFIX_OPERATOR),
        new Token(2, "x", Token.TokenType.VARIABLE_OR_CONSTANT));
  }

  @Test
  void testImplicitNumberVariable() throws ParseException {
    assertAllTokensParsedCorrectly(
        "2x",
        new Token(1, "2", Token.TokenType.NUMBER_LITERAL),
        new Token(2, "*", Token.TokenType.INFIX_OPERATOR),
        new Token(2, "x", Token.TokenType.VARIABLE_OR_CONSTANT));
  }

  @Test
  void testImplicitMultiplicationNotAllowed() {
    ExpressionConfiguration config =
        ExpressionConfiguration.builder().implicitMultiplicationAllowed(false).build();

    assertThatThrownBy(() -> new Tokenizer(config).parse("2(x+y)"))
        .isEqualTo(new ParseException(2, 2, "(", "Missing operator"));
  }
}
