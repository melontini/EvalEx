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
package com.ezylang.evalex.data.util;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import java.util.*;
import java.util.function.IntFunction;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LazyArrayWrapper extends AbstractList<EvaluationValue> {

  private final IntFunction<Object> function;
  private final int size;
  private final ExpressionConfiguration configuration;

  @Override
  public EvaluationValue get(int index) {
    return configuration
        .getEvaluationValueConverter()
        .convertObject(function.apply(index), configuration);
  }

  @Override
  public int size() {
    return size;
  }
}
