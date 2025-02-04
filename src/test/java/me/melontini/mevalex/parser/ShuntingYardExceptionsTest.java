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

import static me.melontini.mevalex.parser.Token.TokenType.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.operators.arithmetic.InfixMultiplicationOperator;
import me.melontini.mevalex.operators.arithmetic.PrefixMinusOperator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class ShuntingYardExceptionsTest extends BaseParserTest {

  @Test
  void testUnexpectedToken() {
    List<Token> tokens = List.of(new Token(1, "x", FUNCTION_PARAM_START));
    assertThatThrownBy(
            () -> new ShuntingYardConverter(configuration).toAbstractSyntaxTree(tokens, "x"))
        .isInstanceOf(ParseException.class)
        .hasMessage("Unexpected token of type 'FUNCTION_PARAM_START'");
  }

  @Test
  void testMissingPrefixOperand() {
    List<Token> tokens = List.of(new Token(1, "-", PREFIX_OPERATOR, new PrefixMinusOperator()));
    assertThatThrownBy(
            () -> new ShuntingYardConverter(configuration).toAbstractSyntaxTree(tokens, "-"))
        .isInstanceOf(ParseException.class)
        .hasMessage("Missing operand for operator");
  }

  @Test
  void testMissingSecondInfixOperand() {
    List<Token> tokens =
        Arrays.asList(
            new Token(1, "2", VARIABLE_OR_CONSTANT),
            new Token(2, "*", INFIX_OPERATOR, new InfixMultiplicationOperator()));
    assertThatThrownBy(
            () -> new ShuntingYardConverter(configuration).toAbstractSyntaxTree(tokens, "2*"))
        .isInstanceOf(ParseException.class)
        .hasMessage("Missing second operand for operator");
  }

  @Test
  void testEmptyExpression() {
    assertThatThrownBy(() -> ExpressionConfiguration.defaultExpressionParser().parse(""))
        .isInstanceOf(ParseException.class)
        .hasMessage("Empty expression");
  }

  @Test
  void testEmptyExpressionBraces() {
    assertThatThrownBy(() -> ExpressionConfiguration.defaultExpressionParser().parse("()"))
        .isInstanceOf(ParseException.class)
        .hasMessage("Empty expression");
  }

  @Test
  void testComma() {
    assertThatThrownBy(() -> ExpressionConfiguration.defaultExpressionParser().parse(","))
        .isInstanceOf(ParseException.class)
        .hasMessage("Empty expression");
  }

  @Test
  void testDoubleStructureOperator() {
    List<Token> tokens =
        List.of(new Token(1, ".", STRUCTURE_SEPARATOR), new Token(2, ".", STRUCTURE_SEPARATOR));
    assertThatThrownBy(
            () -> new ShuntingYardConverter(configuration).toAbstractSyntaxTree(tokens, ".."))
        .isInstanceOf(ParseException.class)
        .hasMessage("Missing operand for operator");
  }

  @Test
  void testStructureFollowsPostfixOperator() {
    List<Token> tokens =
        List.of(new Token(1, ".", STRUCTURE_SEPARATOR), new Token(2, "!", POSTFIX_OPERATOR));
    assertThatThrownBy(
            () -> new ShuntingYardConverter(configuration).toAbstractSyntaxTree(tokens, ".."))
        .isInstanceOf(ParseException.class)
        .hasMessage("Missing operand for operator");
  }

  @Test
  void testStructureFollowsTwoPostfixOperators() {
    List<Token> tokens =
        List.of(
            new Token(1, ".", STRUCTURE_SEPARATOR),
            new Token(2, "!", POSTFIX_OPERATOR),
            new Token(2, "!", POSTFIX_OPERATOR));
    assertThatThrownBy(
            () -> new ShuntingYardConverter(configuration).toAbstractSyntaxTree(tokens, ".."))
        .isInstanceOf(ParseException.class)
        .hasMessage("Missing operand for operator");
  }

  @Test
  void testFunctionNotEnoughParameters() {
    assertThatThrownBy(() -> ExpressionConfiguration.defaultExpressionParser().parse("ROUND(2)"))
        .isInstanceOf(ParseException.class)
        .hasMessage("Not enough parameters for function");
  }

  @Test
  void testFunctionNotEnoughParametersForVarArgs() {
    // The DT_DATE_PARSE() function has a required parameter and var-args
    assertThatThrownBy(
            () -> ExpressionConfiguration.defaultExpressionParser().parse("DT_DATE_PARSE()"))
        .isInstanceOf(ParseException.class)
        .hasMessage("Not enough parameters for function");
  }

  @Test
  void testFunctionTooManyParameters() {
    assertThatThrownBy(
            () -> ExpressionConfiguration.defaultExpressionParser().parse("ROUND(1,2,3)"))
        .isInstanceOf(ParseException.class)
        .hasMessage("Too many parameters for function");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "Hello, World",
        "Hello ROUND(1,2) + (1 + 1)",
        "Hello ROUND(1,2)",
        "Hello 1 + (1 + 1)",
        "Hello 1 + 1",
        "Hello World",
        "Hello 1",
        "1 2",
        "e.1",
        "E.1"
      })
  void testTooManyOperands(String expressionString) {
    assertThatThrownBy(
            () -> ExpressionConfiguration.defaultExpressionParser().parse(expressionString))
        .isInstanceOf(ParseException.class)
        .hasMessage("Too many operands");
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = ':',
      value = {"(x+y)*(a-) : 10", "a** : 3", "5+, : 3"})
  void testInvalidTokenAfterInfixOperator(String expressionString, int position) {
    assertThatThrownBy(
            () -> ExpressionConfiguration.defaultExpressionParser().parse(expressionString))
        .isInstanceOf(ParseException.class)
        .hasMessage("Unexpected token after infix operator")
        .extracting("startPosition")
        .isEqualTo(position);
  }
}
