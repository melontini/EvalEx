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

import java.util.TreeMap;
import me.melontini.mevalex.config.TestConfigurationProvider;
import org.junit.jupiter.api.Test;

class TokenizerFunctionsTest extends BaseParserTest {

  @Test
  void testSimple() throws ParseException {
    configuration =
        configuration.withFunctionDictionary(
            configuration.getFunctionDictionary().toBuilder(
                    () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
                .add("f", new TestConfigurationProvider.DummyFunction())
                .build());
    assertAllTokensParsedCorrectly(
        "f(x)",
        new Token(1, "f", Token.TokenType.FUNCTION),
        new Token(2, "(", Token.TokenType.BRACE_OPEN),
        new Token(3, "x", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(4, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testBlanks() throws ParseException {
    configuration =
        configuration.withFunctionDictionary(
            configuration.getFunctionDictionary().toBuilder(
                    () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
                .add("f", new TestConfigurationProvider.DummyFunction())
                .build());
    assertAllTokensParsedCorrectly(
        "f (x)",
        new Token(1, "f", Token.TokenType.FUNCTION),
        new Token(3, "(", Token.TokenType.BRACE_OPEN),
        new Token(4, "x", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(5, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testUnderscores() throws ParseException {
    configuration =
        configuration.withFunctionDictionary(
            configuration.getFunctionDictionary().toBuilder(
                    () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
                .add("_f_x_", new TestConfigurationProvider.DummyFunction())
                .build());
    assertAllTokensParsedCorrectly(
        "_f_x_(x)",
        new Token(1, "_f_x_", Token.TokenType.FUNCTION),
        new Token(6, "(", Token.TokenType.BRACE_OPEN),
        new Token(7, "x", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(8, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testWithNumbers() throws ParseException {
    configuration =
        configuration.withFunctionDictionary(
            configuration.getFunctionDictionary().toBuilder(
                    () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
                .add("f1x2", new TestConfigurationProvider.DummyFunction())
                .build());
    assertAllTokensParsedCorrectly(
        "f1x2(x)",
        new Token(1, "f1x2", Token.TokenType.FUNCTION),
        new Token(5, "(", Token.TokenType.BRACE_OPEN),
        new Token(6, "x", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(7, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testWithMoreParameters() throws ParseException {
    assertAllTokensParsedCorrectly(
        "SUM(a, 2, 3)",
        new Token(1, "SUM", Token.TokenType.FUNCTION),
        new Token(4, "(", Token.TokenType.BRACE_OPEN),
        new Token(5, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(6, ",", Token.TokenType.COMMA),
        new Token(8, "2", Token.TokenType.NUMBER_LITERAL),
        new Token(9, ",", Token.TokenType.COMMA),
        new Token(11, "3", Token.TokenType.NUMBER_LITERAL),
        new Token(12, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testWithMixedParameters() throws ParseException {
    assertAllTokensParsedCorrectly(
        "TEST(a, \"hello\", 3)",
        new Token(1, "TEST", Token.TokenType.FUNCTION),
        new Token(5, "(", Token.TokenType.BRACE_OPEN),
        new Token(6, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(7, ",", Token.TokenType.COMMA),
        new Token(9, "hello", Token.TokenType.STRING_LITERAL),
        new Token(16, ",", Token.TokenType.COMMA),
        new Token(18, "3", Token.TokenType.NUMBER_LITERAL),
        new Token(19, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testFunctionInParameter() throws ParseException {
    assertAllTokensParsedCorrectly(
        "TEST(a, FACT(x), 3)",
        new Token(1, "TEST", Token.TokenType.FUNCTION),
        new Token(5, "(", Token.TokenType.BRACE_OPEN),
        new Token(6, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(7, ",", Token.TokenType.COMMA),
        new Token(9, "FACT", Token.TokenType.FUNCTION),
        new Token(13, "(", Token.TokenType.BRACE_OPEN),
        new Token(14, "x", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(15, ")", Token.TokenType.BRACE_CLOSE),
        new Token(16, ",", Token.TokenType.COMMA),
        new Token(18, "3", Token.TokenType.NUMBER_LITERAL),
        new Token(19, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testFunctionInParameterInFunctionParameter() throws ParseException {
    assertAllTokensParsedCorrectly(
        "SUM(a,FACT(MIN(x,y)),3)",
        new Token(1, "SUM", Token.TokenType.FUNCTION),
        new Token(4, "(", Token.TokenType.BRACE_OPEN),
        new Token(5, "a", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(6, ",", Token.TokenType.COMMA),
        new Token(7, "FACT", Token.TokenType.FUNCTION),
        new Token(11, "(", Token.TokenType.BRACE_OPEN),
        new Token(12, "MIN", Token.TokenType.FUNCTION),
        new Token(15, "(", Token.TokenType.BRACE_OPEN),
        new Token(16, "x", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(17, ",", Token.TokenType.COMMA),
        new Token(18, "y", Token.TokenType.VARIABLE_OR_CONSTANT),
        new Token(19, ")", Token.TokenType.BRACE_CLOSE),
        new Token(20, ")", Token.TokenType.BRACE_CLOSE),
        new Token(21, ",", Token.TokenType.COMMA),
        new Token(22, "3", Token.TokenType.NUMBER_LITERAL),
        new Token(23, ")", Token.TokenType.BRACE_CLOSE));
  }

  @Test
  void testUndefinedFunction() {
    assertThatThrownBy(() -> new Tokenizer(configuration).parse("a(b)"))
        .isEqualTo(new ParseException(1, 2, "a", "Undefined function 'a'"));
  }
}
