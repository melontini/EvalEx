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

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import java.util.Map;
import lombok.Value;
import lombok.With;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Value
@With
@Accessors(fluent = true)
public class EvaluationContext {
  ExpressionConfiguration configuration;
  Map<String, EvaluationValue> parameters;
  Object @Nullable [] context;

  public static EvaluationContextBuilder builder() {
    return new EvaluationContextBuilder(ExpressionConfiguration.defaultConfiguration());
  }

  public static EvaluationContextBuilder builder(ExpressionConfiguration configuration) {
    return new EvaluationContextBuilder(configuration);
  }

  public static EvaluationContextBuilder builder(Expression expression) {
    return new EvaluationContextBuilder(expression.getConfiguration());
  }

  public static final class EvaluationContextBuilder {

    private final ExpressionConfiguration configuration;
    Map<String, EvaluationValue> parameters;
    Object[] context;

    private EvaluationContextBuilder(ExpressionConfiguration configuration) {
      this.configuration = configuration;
      this.parameters = configuration.getParameterMapSupplier().get();
    }

    public EvaluationContextBuilder parameter(String parameter, Object value) {
      if (!configuration.isAllowOverwriteConstants()) {
        if (configuration.getConstants().containsKey(parameter))
          throw new UnsupportedOperationException(
              String.format("Can't set value for constant '%s'", parameter));
      }
      this.parameters.put(
          parameter,
          configuration.getEvaluationValueConverter().convertObject(value, configuration));
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
      return new EvaluationContext(this.configuration, this.parameters, this.context);
    }
  }
}
