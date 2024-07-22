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

import com.ezylang.evalex.data.EvaluationValue;
import java.util.Collections;
import java.util.Map;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Value
@Accessors(fluent = true)
public class EvaluationContext {
  Expression expression;
  Map<String, EvaluationValue> parameters;
  Object @Nullable [] context;

  public EvaluationContext(
      Expression expression, Map<String, EvaluationValue> parameters, Object @Nullable [] context) {
    this.expression = expression;
    this.parameters = parameters;
    this.context = context;
  }

  public EvaluationContext withParameter(String parameter, EvaluationValue value) {
    return this.withParameters(Collections.singletonMap(parameter, value));
  }

  public EvaluationContext withParameters(Map<String, EvaluationValue> map) {
    Map<String, EvaluationValue> parameters =
        expression.getConfiguration().getParameterMapSupplier().get();
    parameters.putAll(this.parameters);
    parameters.putAll(map);
    return new EvaluationContext(expression, Collections.unmodifiableMap(parameters), context);
  }

  public static EvaluationContextBuilder builder(Expression expression) {
    return new EvaluationContextBuilder(expression);
  }

  public static final class EvaluationContextBuilder {

    private final Expression expression;
    private Map<String, EvaluationValue> parameters;
    private Object[] context;

    private EvaluationContextBuilder(Expression expression) {
      this.expression = expression;
    }

    public EvaluationContextBuilder parameter(String parameter, Object value) {
      if (!expression.getConfiguration().isAllowOverwriteConstants()) {
        if (expression.getConfiguration().getConstants().containsKey(parameter))
          throw new UnsupportedOperationException(
              String.format("Can't set value for constant '%s'", parameter));
      }
      if (this.parameters == null)
        this.parameters = expression.getConfiguration().getParameterMapSupplier().get();

      this.parameters.put(parameter, expression.convertValue(value));
      return this;
    }

    public EvaluationContextBuilder with(String parameter, Object value) {
      return this.parameter(parameter, value);
    }

    public EvaluationContextBuilder and(String parameter, Object value) {
      return this.parameter(parameter, value);
    }

    public EvaluationContextBuilder parameters(Map<String, ?> parameters) {
      parameters.forEach(this::parameter);
      return this;
    }

    public EvaluationContextBuilder context(Object... context) {
      this.context = context;
      return this;
    }

    public EvaluationContext build() {
      return new EvaluationContext(
          this.expression,
          this.parameters != null
              ? Collections.unmodifiableMap(this.parameters)
              : Collections.emptyMap(),
          this.context);
    }
  }
}
