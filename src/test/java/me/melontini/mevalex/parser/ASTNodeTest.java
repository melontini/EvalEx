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

import static org.assertj.core.api.Assertions.assertThat;

import me.melontini.mevalex.functions.basic.MinFunction;
import me.melontini.mevalex.operators.arithmetic.InfixPlusOperator;
import me.melontini.mevalex.operators.arithmetic.PrefixMinusOperator;
import org.junit.jupiter.api.Test;

class ASTNodeTest {
  final Token variable = new Token(1, "variable", Token.TokenType.VARIABLE_OR_CONSTANT);

  @Test
  void testJSONSingle() {
    ASTNode node = ASTNode.of(variable);

    assertThat(node.toJSON())
        .isEqualTo("{\"type\":\"VARIABLE_OR_CONSTANT\",\"value\":\"variable\"}");
  }

  @Test
  void testJSONPrefix() {
    Token token = new Token(1, "-", Token.TokenType.PREFIX_OPERATOR, new PrefixMinusOperator());
    ASTNode node = ASTNode.of(token, ASTNode.of(variable));

    assertThat(node.toJSON())
        .isEqualTo(
            "{\"type\":\"PREFIX_OPERATOR\",\"value\":\"-\",\"children\":[{\"type\":\"VARIABLE_OR_CONSTANT\",\"value\":\"variable\"}]}");
  }

  @Test
  void testJSONInfix() {
    Token token = new Token(1, "+", Token.TokenType.INFIX_OPERATOR, new InfixPlusOperator());
    ASTNode node = ASTNode.of(token, ASTNode.of(variable), ASTNode.of(variable));

    assertThat(node.toJSON())
        .isEqualTo(
            "{\"type\":\"INFIX_OPERATOR\",\"value\":\"+\",\"children\":[{\"type\":\"VARIABLE_OR_CONSTANT\",\"value\":\"variable\"},{\"type\":\"VARIABLE_OR_CONSTANT\",\"value\":\"variable\"}]}");
  }

  @Test
  void testJSONFunction() {
    Token token = new Token(1, "+", Token.TokenType.FUNCTION, new MinFunction());
    ASTNode node =
        ASTNode.of(token, ASTNode.of(variable), ASTNode.of(variable), ASTNode.of(variable));

    assertThat(node.toJSON())
        .isEqualTo(
            "{\"type\":\"FUNCTION\",\"value\":\"+\",\"children\":[{\"type\":\"VARIABLE_OR_CONSTANT\",\"value\":\"variable\"},{\"type\":\"VARIABLE_OR_CONSTANT\",\"value\":\"variable\"},{\"type\":\"VARIABLE_OR_CONSTANT\",\"value\":\"variable\"}]}");
  }
}
