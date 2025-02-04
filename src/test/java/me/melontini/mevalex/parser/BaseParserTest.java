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

import java.util.List;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.config.TestConfigurationProvider;

/** */
public abstract class BaseParserTest {

  ExpressionConfiguration configuration =
      TestConfigurationProvider.StandardConfigurationWithAdditionalTestOperators;

  void assertAllTokensParsedCorrectly(String input, Token... expectedTokens) throws ParseException {
    assertAllTokensParsedCorrectly(input, configuration, expectedTokens);
  }

  void assertAllTokensParsedCorrectly(
      String input, ExpressionConfiguration configuration, Token... expectedTokens)
      throws ParseException {
    List<Token> tokensParsed = new Tokenizer(configuration).parse(input);

    assertThat(tokensParsed).containsExactly(expectedTokens);
  }

  /**
   * Compares if the generated abstract syntax tree is correct. To visualize the generated JSON in a
   * tree format, i.e. to display the AST, you can use the following online service:<br>
   * <a href="https://vanya.jp.net/vtree/">Online JSON to Tree Diagram Converter</a>
   */
  void assertASTTreeIsEqualTo(String expression, String treeJSON) throws ParseException {

    List<Token> tokensParsed = new Tokenizer(configuration).parse(expression);
    ASTNode root =
        new ShuntingYardConverter(configuration).toAbstractSyntaxTree(tokensParsed, expression);
    assertThat(root.toJSON()).isEqualTo(treeJSON);
  }
}
