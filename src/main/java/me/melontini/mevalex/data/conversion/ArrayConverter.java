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

import java.util.List;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.ArrayValue;
import me.melontini.mevalex.data.util.LazyArrayWrapper;
import me.melontini.mevalex.data.util.LazyListWrapper;

/** Converter to convert to the ARRAY data type. */
public class ArrayConverter implements ConverterIfc {
  @Override
  public EvaluationValue convert(Object object, ExpressionConfiguration configuration) {
    if (object.getClass().isArray()) return ArrayValue.of(convertArray(object, configuration));
    if (object instanceof List<?> list)
      return ArrayValue.of(new LazyListWrapper(list, configuration));
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
