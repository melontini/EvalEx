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
import java.time.Duration;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.DurationValue;
import org.junit.jupiter.api.Test;

class DurationConverterTest {

  private final ExpressionConfiguration defaultConfiguration =
      ExpressionConfiguration.defaultConfiguration();

  private final DurationConverter converter = new DurationConverter();

  @Test
  void testDuration() {
    Duration duration = Duration.ofMinutes(5);

    EvaluationValue converted = converter.convert(duration, defaultConfiguration);

    assertThat(converted).isInstanceOf(DurationValue.class);
    assertThat(converted.getValue()).isEqualTo(duration);
  }

  @Test
  void testCanConvert() {
    assertThat(converter.canConvert(Duration.ofMinutes(10))).isTrue();
  }

  @Test
  void testCanNotConvert() {
    assertThat(converter.canConvert("hello")).isFalse();
    assertThat(converter.canConvert(new BigDecimal(10))).isFalse();
  }
}
