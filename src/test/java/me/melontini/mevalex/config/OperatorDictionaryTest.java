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
package me.melontini.mevalex.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.TreeMap;
import me.melontini.mevalex.config.TestConfigurationProvider.PostfixQuestionOperator;
import me.melontini.mevalex.config.TestConfigurationProvider.PrefixPlusPlusOperator;
import me.melontini.mevalex.operators.OperatorIfc;
import me.melontini.mevalex.operators.arithmetic.InfixModuloOperator;
import org.junit.jupiter.api.Test;

class OperatorDictionaryTest {

  @Test
  void testCreationOfOperators() {
    OperatorIfc prefix = new PrefixPlusPlusOperator();
    OperatorIfc postfix = new PostfixQuestionOperator();
    OperatorIfc infix = new InfixModuloOperator();

    OperatorDictionary dictionary =
        OperatorDictionary.builder(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
            .prefix("++", prefix)
            .postfix("?", postfix)
            .infix("%", infix)
            .build();

    assertThat(dictionary.hasPrefixOperator("++")).isTrue();
    assertThat(dictionary.hasPostfixOperator("?")).isTrue();
    assertThat(dictionary.hasInfixOperator("%")).isTrue();

    assertThat(dictionary.getPrefixOperator("++")).isEqualTo(prefix);
    assertThat(dictionary.getPostfixOperator("?")).isEqualTo(postfix);
    assertThat(dictionary.getInfixOperator("%")).isEqualTo(infix);

    assertThat(dictionary.hasPrefixOperator("A")).isFalse();
    assertThat(dictionary.hasPostfixOperator("B")).isFalse();
    assertThat(dictionary.hasInfixOperator("C")).isFalse();
  }

  @Test
  void testCaseInsensitivity() {
    OperatorIfc prefix = new PrefixPlusPlusOperator();
    OperatorIfc postfix = new PostfixQuestionOperator();
    OperatorIfc infix = new InfixModuloOperator();

    OperatorDictionary dictionary =
        OperatorDictionary.builder(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
            .prefix("PlusPlus", prefix)
            .postfix("Question", postfix)
            .infix("Percent", infix)
            .build();

    assertThat(dictionary.hasPrefixOperator("PlusPlus")).isTrue();
    assertThat(dictionary.hasPrefixOperator("plusplus")).isTrue();
    assertThat(dictionary.hasPrefixOperator("PLUSPLUS")).isTrue();

    assertThat(dictionary.hasPostfixOperator("Question")).isTrue();
    assertThat(dictionary.hasPostfixOperator("question")).isTrue();
    assertThat(dictionary.hasPostfixOperator("QUESTION")).isTrue();

    assertThat(dictionary.hasInfixOperator("Percent")).isTrue();
    assertThat(dictionary.hasInfixOperator("percent")).isTrue();
    assertThat(dictionary.hasInfixOperator("PERCENT")).isTrue();
  }
}
