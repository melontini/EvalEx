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

import java.util.Map;
import lombok.*;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.data.DataAccessorIfc;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.parser.Token;
import org.jetbrains.annotations.Nullable;

@ToString()
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StructureValue implements EvaluationValue, DataAccessorIfc {

  private final Map<String, EvaluationValue> value;

  // The map must support all immutable AbstractMap methods.
  public static StructureValue of(@NonNull Map<String, EvaluationValue> struct) {
    return new StructureValue(struct);
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public boolean isStructureValue() {
    return true;
  }

  @Override
  public Map<String, EvaluationValue> getStructureValue() {
    return value;
  }

  @Override
  public @Nullable EvaluationValue getVariableData(
      String variable, Token token, EvaluationContext context) throws EvaluationException {
    return value.getOrDefault(variable, null);
  }
}
