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
package com.ezylang.evalex.data.conversion;

import static com.ezylang.evalex.data.EvaluationValue.arrayValue;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.util.LazyArrayWrapper;
import com.ezylang.evalex.data.util.LazyListWrapper;
import java.util.List;

/** Converter to convert to the ARRAY data type. */
public class ArrayConverter implements ConverterIfc {
  @Override
  public EvaluationValue convert(Object object, ExpressionConfiguration configuration) {
    if (object.getClass().isArray()) return arrayValue(convertArray(object, configuration));
    if (object instanceof List<?> list) return arrayValue(new LazyListWrapper(list, configuration));
    throw illegalArgument(object);
  }

  @Override
  public boolean canConvert(Object object) {
    return object instanceof List || object.getClass().isArray();
  }

  private List<EvaluationValue> convertArray(Object array, ExpressionConfiguration configuration) {
    if (array instanceof int[] arr)
      return new LazyArrayWrapper(i -> arr[i], arr.length, configuration);
    if (array instanceof long[] arr)
      return new LazyArrayWrapper(i -> arr[i], arr.length, configuration);
    if (array instanceof double[] arr)
      return new LazyArrayWrapper(i -> arr[i], arr.length, configuration);
    if (array instanceof float[] arr)
      return new LazyArrayWrapper(i -> arr[i], arr.length, configuration);
    if (array instanceof short[] arr)
      return new LazyArrayWrapper(i -> arr[i], arr.length, configuration);
    if (array instanceof char[] arr)
      return new LazyArrayWrapper(i -> arr[i], arr.length, configuration);
    if (array instanceof byte[] arr)
      return new LazyArrayWrapper(i -> arr[i], arr.length, configuration);
    if (array instanceof boolean[] arr)
      return new LazyArrayWrapper(i -> arr[i], arr.length, configuration);
    var arr = ((Object[]) array);
    return new LazyArrayWrapper(i -> arr[i], arr.length, configuration);
  }
}
