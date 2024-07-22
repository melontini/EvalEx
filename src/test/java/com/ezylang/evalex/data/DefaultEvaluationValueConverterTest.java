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
package com.ezylang.evalex.data;

import static org.assertj.core.api.Assertions.assertThat;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.conversion.DefaultEvaluationValueConverter;
import com.ezylang.evalex.data.types.NullValue;
import com.ezylang.evalex.data.types.StringValue;
import org.junit.jupiter.api.Test;

class DefaultEvaluationValueConverterTest {

  private final DefaultEvaluationValueConverter converter = new DefaultEvaluationValueConverter();
  private final ExpressionConfiguration defaultConfiguration =
      ExpressionConfiguration.defaultConfiguration();

  @Test
  void testNull() {
    EvaluationValue converted = converter.convertObject(null, defaultConfiguration);

    assertThat(converted).isInstanceOf(NullValue.class);
  }

  @Test
  void testNestedEvaluationValueNull() {
    EvaluationValue converted =
        converter.convertObject(StringValue.of("Hello"), defaultConfiguration);

    assertThat(converted).isInstanceOf(StringValue.class);
    assertThat(converted.getStringValue()).isEqualTo("Hello");
  }
}
