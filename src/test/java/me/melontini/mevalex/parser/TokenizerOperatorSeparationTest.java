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

import org.junit.jupiter.api.Test;

class TokenizerOperatorSeparationTest extends BaseParserTest {

  @Test
  void testInfixPrefix() throws ParseException {
    assertAllTokensParsedCorrectly(
        "2+-3",
        new Token(1, "2", Token.TokenType.NUMBER_LITERAL),
        new Token(2, "+", Token.TokenType.INFIX_OPERATOR),
        new Token(3, "-", Token.TokenType.PREFIX_OPERATOR),
        new Token(4, "3", Token.TokenType.NUMBER_LITERAL));
  }

  @Test
  void testPostfixInfix() throws ParseException {
    assertAllTokensParsedCorrectly(
        "a++-3",
        new Token(1, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(2, "++", Token.TokenType.POSTFIX_OPERATOR),
        new Token(4, "-", Token.TokenType.INFIX_OPERATOR),
        new Token(5, "3", Token.TokenType.NUMBER_LITERAL));
  }

  @Test
  void testPostfixInfixPrefix() throws ParseException {
    assertAllTokensParsedCorrectly(
        "a++--3",
        new Token(1, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(2, "++", Token.TokenType.POSTFIX_OPERATOR),
        new Token(4, "-", Token.TokenType.INFIX_OPERATOR),
        new Token(5, "-", Token.TokenType.PREFIX_OPERATOR),
        new Token(6, "3", Token.TokenType.NUMBER_LITERAL));
  }

  @Test
  void testPrefixPrefix() throws ParseException {
    assertAllTokensParsedCorrectly(
        "!-2",
        new Token(1, "!", Token.TokenType.PREFIX_OPERATOR),
        new Token(2, "-", Token.TokenType.PREFIX_OPERATOR),
        new Token(3, "2", Token.TokenType.NUMBER_LITERAL));
  }

  @Test
  void testEqualsEqualsNumbers() throws ParseException {
    assertAllTokensParsedCorrectly(
        "3==3",
        new Token(1, "3", Token.TokenType.NUMBER_LITERAL),
        new Token(2, "==", Token.TokenType.INFIX_OPERATOR),
        new Token(4, "3", Token.TokenType.NUMBER_LITERAL));
  }

  @Test
  void testEqualsEqualsVariables() throws ParseException {
    assertAllTokensParsedCorrectly(
        "a==b",
        new Token(1, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(2, "==", Token.TokenType.INFIX_OPERATOR),
        new Token(4, "b", Token.TokenType.VARIABLE_OR_CONSTANT));
  }

  @Test
  void testEqualsNumbers() throws ParseException {
    assertAllTokensParsedCorrectly(
        "3=3",
        new Token(1, "3", Token.TokenType.NUMBER_LITERAL),
        new Token(2, "=", Token.TokenType.INFIX_OPERATOR),
        new Token(3, "3", Token.TokenType.NUMBER_LITERAL));
  }

  @Test
  void testEqualsVariables() throws ParseException {
    assertAllTokensParsedCorrectly(
        "a=b",
        new Token(1, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(2, "=", Token.TokenType.INFIX_OPERATOR),
        new Token(3, "b", Token.TokenType.VARIABLE_OR_CONSTANT));
  }
}
