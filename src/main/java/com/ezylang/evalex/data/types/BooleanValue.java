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
package com.ezylang.evalex.data.types;

import com.ezylang.evalex.data.EvaluationValue;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

@ToString()
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class BooleanValue extends EvaluationValue {

  public static final BooleanValue TRUE = new BooleanValue(true);
  public static final BooleanValue FALSE = new BooleanValue(false);

  private final Boolean value;

  public static BooleanValue of(boolean b) {
    return b ? TRUE : FALSE;
  }

  public static BooleanValue of(@Nullable Boolean b) {
    return Boolean.TRUE.equals(b) ? TRUE : FALSE;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public boolean isBooleanValue() {
    return true;
  }

  @Override
  public BigDecimal getNumberValue() {
    return value.equals(Boolean.TRUE) ? BigDecimal.ONE : BigDecimal.ZERO;
  }

  @Override
  public Boolean getBooleanValue() {
    return value;
  }

  @Override
  public int compareTo(EvaluationValue toCompare) {
    return value.compareTo(toCompare.getBooleanValue());
  }
}
