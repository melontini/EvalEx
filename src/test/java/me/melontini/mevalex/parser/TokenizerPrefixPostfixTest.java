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

class TokenizerPrefixPostfixTest extends BaseParserTest {

  @Test
  void testPrefixSingle() throws ParseException {
    assertAllTokensParsedCorrectly(
        "++a",
        new Token(1, "++", Token.TokenType.PREFIX_OPERATOR),
        new Token(3, "a", Token.TokenType.VARIABLE_OR_CONSTANT));
  }

  @Test
  void testPostfixSingle() throws ParseException {
    assertAllTokensParsedCorrectly(
        "a++",
        new Token(1, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(2, "++", Token.TokenType.POSTFIX_OPERATOR));
  }

  @Test
  void testPostfixAsPrefixThrowsException() {
    assertThatThrownBy(() -> new Tokenizer(configuration).parse("?a"))
        .isInstanceOf(ParseException.class)
        .hasMessage("Undefined operator '?'");
  }

  @Test
  void testPrefixAndPostfix() throws ParseException {
    // note: if this is supported, depends on the operator and what type it expects as operand
    assertAllTokensParsedCorrectly(
        "++a++",
        new Token(1, "++", Token.TokenType.PREFIX_OPERATOR),
        new Token(3, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(4, "++", Token.TokenType.POSTFIX_OPERATOR));
  }

  @Test
  void testPrefixWithInfixAndPostfix() throws ParseException {
    assertAllTokensParsedCorrectly(
        "++a+a++",
        new Token(1, "++", Token.TokenType.PREFIX_OPERATOR),
        new Token(3, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(4, "+", Token.TokenType.INFIX_OPERATOR),
        new Token(5, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(6, "++", Token.TokenType.POSTFIX_OPERATOR));
  }

  @Test
  void testPrefixWithBraces() throws ParseException {
    assertAllTokensParsedCorrectly(
        "(++a)+(a++)",
        new Token(1, "(", Token.TokenType.BRACE_OPEN),
        new Token(2, "++", Token.TokenType.PREFIX_OPERATOR),
        new Token(4, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(5, ")", Token.TokenType.BRACE_CLOSE),
        new Token(6, "+", Token.TokenType.INFIX_OPERATOR),
        new Token(7, "(", Token.TokenType.BRACE_OPEN),
        new Token(8, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(9, "++", Token.TokenType.POSTFIX_OPERATOR),
        new Token(11, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testPrefixWithFunction() throws ParseException {
    assertAllTokensParsedCorrectly(
        "++MAX(++a,a++,b++)++",
        new Token(1, "++", Token.TokenType.PREFIX_OPERATOR),
        new Token(3, "MAX", Token.TokenType.FUNCTION),
        new Token(6, "(", Token.TokenType.BRACE_OPEN),
        new Token(7, "++", Token.TokenType.PREFIX_OPERATOR),
        new Token(9, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(10, ",", Token.TokenType.COMMA),
        new Token(11, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(12, "++", Token.TokenType.POSTFIX_OPERATOR),
        new Token(14, ",", Token.TokenType.COMMA),
        new Token(15, "b", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(16, "++", Token.TokenType.POSTFIX_OPERATOR),
        new Token(18, ")", Token.TokenType.BRACE_CLOSE),
        new Token(19, "++", Token.TokenType.POSTFIX_OPERATOR));
  }

  @Test
  void testPrefixWithStringLiteral() throws ParseException {
    assertAllTokensParsedCorrectly(
        "++\"hello\"++",
        new Token(1, "++", Token.TokenType.PREFIX_OPERATOR),
        new Token(3, "hello", Token.TokenType.STRING_LITERAL),
        new Token(10, "++", Token.TokenType.POSTFIX_OPERATOR));
  }

  @Test
  void testPrefixWithUnaryAndBinary() throws ParseException {
    assertAllTokensParsedCorrectly(
        "-a - -b++",
        new Token(1, "-", Token.TokenType.PREFIX_OPERATOR),
        new Token(2, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(4, "-", Token.TokenType.INFIX_OPERATOR),
        new Token(6, "-", Token.TokenType.PREFIX_OPERATOR),
        new Token(7, "b", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(8, "++", Token.TokenType.POSTFIX_OPERATOR));
  }
}
