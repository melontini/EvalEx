/*
  Copyright 2012-2024 Udo Klimaschewski

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
package com.ezylang.evalex;

import com.ezylang.evalex.data.DataAccessorIfc;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.types.DataAccessorValue;
import com.ezylang.evalex.parser.ParseException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ExpressionEvaluatorDataAccessorTest extends BaseExpressionEvaluatorTest {

  @Test
  void testBasicAccessorAccess() throws ParseException, EvaluationException {
    DataAccessorValue value = DataAccessorValue.of(reflectiveAccessor(new TestClass()));

    Assertions.assertThat(
            createExpression("test_obj.testField")
                .evaluate(builder -> builder.parameter("test_obj", value)))
        .extracting(EvaluationValue::getBooleanValue)
        .isEqualTo(true);

    Assertions.assertThat(
            createExpression("test_obj.testIntField")
                .evaluate(builder -> builder.parameter("test_obj", value)))
        .extracting(EvaluationValue::getNumberValue)
        .isEqualTo(BigDecimal.valueOf(45));

    Assertions.assertThat(
            createExpression("test_obj.coolString")
                .evaluate(builder -> builder.parameter("test_obj", value)))
        .extracting(EvaluationValue::getStringValue)
        .isEqualTo("Hello World!");

    Assertions.assertThatThrownBy(
            () ->
                createExpression("test_obj.noString")
                    .evaluate(builder -> builder.parameter("test_obj", value)))
        .isInstanceOf(EvaluationException.class)
        .hasMessage("Field 'noString' not found in structure");
  }

  public static class TestClass {
    public boolean testField = true;
    public int testIntField = 45;
    public final String coolString = "Hello World!";
  }

  public static DataAccessorIfc reflectiveAccessor(Object object) {
    Map<String, Function<Object, Object>> fields = new HashMap<>();
    Set<String> invalid = new HashSet<>();
    return (variable, token, context) -> {
      if (fields.containsKey(variable))
        return context.expression().convertValue(fields.get(variable).apply(object));
      if (invalid.contains(variable)) return null;

      for (Field field : object.getClass().getFields()) {
        if (!field.getName().equals(variable)) continue;
        if (Modifier.isStatic(field.getModifiers())) continue;
        Function<Object, Object> function =
            object1 -> {
              try {
                return field.get(object1);
              } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
              }
            };
        fields.put(variable, function);
        return context.expression().convertValue(function.apply(object));
      }
      invalid.add(variable);
      return null;
    };
  }
}
