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
package me.melontini.mevalex.data.types;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.IndexedAccessor;
import me.melontini.mevalex.parser.Token;
import org.jetbrains.annotations.Nullable;

@ToString()
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ArrayValue implements EvaluationValue, IndexedAccessor {

  private final List<EvaluationValue> value;

  public static ArrayValue of(@NonNull List<EvaluationValue> array) {
    return new ArrayValue(array);
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public String getName() {
    return "array";
  }

  @Override
  public boolean isArrayValue() {
    return true;
  }

  @Override
  public List<EvaluationValue> getArrayValue() {
    return value;
  }

  @Override
  public @Nullable EvaluationValue getIndexedData(
      BigDecimal index, Token token, EvaluationContext context) throws EvaluationException {
    int intIndex = index.intValue();
    if (intIndex < 0 || intIndex >= value.size()) {
      return null;
    }
    return value.get(intIndex);
  }
}
