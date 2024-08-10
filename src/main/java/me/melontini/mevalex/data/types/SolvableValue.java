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

import lombok.*;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.parser.Solvable;

@ToString()
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SolvableValue implements EvaluationValue {

  private final Solvable value;

  public static SolvableValue of(@NonNull Solvable node) {
    return new SolvableValue(node);
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public boolean isSolvable() {
    return true;
  }

  @Override
  public Solvable getSolvable() {
    return value;
  }
}
