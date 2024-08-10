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
package me.melontini.mevalex.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.data.types.BooleanValue;
import me.melontini.mevalex.data.types.NumberValue;
import me.melontini.mevalex.data.types.StringValue;
import org.junit.jupiter.api.Test;

class MapBasedDataAccessorTest {

  @Test
  void testSetGetData() throws EvaluationException {
    Map<String, EvaluationValue> variables = new HashMap<>();
    DataAccessorIfc dataAccessor = (variable, token, context) -> variables.get(variable);

    EvaluationValue num = NumberValue.of(new BigDecimal("123"));
    EvaluationValue string = StringValue.of("hello");
    EvaluationValue bool = BooleanValue.of(true);

    variables.put("num", num);
    variables.put("string", string);
    variables.put("bool", bool);

    assertThat(dataAccessor.getVariableData("num", null, null)).isEqualTo(num);
    assertThat(dataAccessor.getVariableData("string", null, null)).isEqualTo(string);
    assertThat(dataAccessor.getVariableData("bool", null, null)).isEqualTo(bool);
  }

  @Test
  void testCaseInsensitivity() throws EvaluationException {
    Map<String, EvaluationValue> variables = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    DataAccessorIfc dataAccessor = (variable, token, context) -> variables.get(variable);

    EvaluationValue num = NumberValue.of(new BigDecimal("123"));
    variables.put("Hello", num);

    assertThat(dataAccessor.getVariableData("Hello", null, null)).isEqualTo(num);
    assertThat(dataAccessor.getVariableData("hello", null, null)).isEqualTo(num);
    assertThat(dataAccessor.getVariableData("HELLO", null, null)).isEqualTo(num);
  }
}
