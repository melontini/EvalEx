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

import java.time.Duration;
import lombok.*;
import me.melontini.mevalex.data.EvaluationValue;

@ToString()
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DurationValue implements EvaluationValue {

  private final Duration value;

  public static DurationValue of(@NonNull Duration duration) {
    return new DurationValue(duration);
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public String getName() {
    return "duration";
  }

  @Override
  public boolean isDurationValue() {
    return true;
  }

  @Override
  public Duration getDurationValue() {
    return value;
  }

  @Override
  public int compareTo(EvaluationValue toCompare) {
    return value.compareTo(toCompare.getDurationValue());
  }
}
