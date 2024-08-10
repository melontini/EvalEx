/*
  Copyright 2012-2023 Udo Klimaschewski

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
package me.melontini.mevalex.data.conversion;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.SolvableValue;
import me.melontini.mevalex.data.types.StringValue;
import me.melontini.mevalex.parser.Solvable;
import org.junit.jupiter.api.Test;

class SolvableConverterTest {

  private final ExpressionConfiguration defaultConfiguration =
      ExpressionConfiguration.defaultConfiguration();

  private final SolvableConverter converter = new SolvableConverter();

  private final Solvable testNode = context -> StringValue.of("Hello!");

  @Test
  void testDuration() {
    EvaluationValue converted = converter.convert(testNode, defaultConfiguration);

    assertThat(converted).isInstanceOf(SolvableValue.class);
    assertThat(converted.getSolvable()).isEqualTo(testNode);
  }

  @Test
  void testCanConvert() {
    assertThat(converter.canConvert(testNode)).isTrue();
  }

  @Test
  void testCanNotConvert() {
    assertThat(converter.canConvert("hello")).isFalse();
    assertThat(converter.canConvert(new BigDecimal(10))).isFalse();
  }
}
