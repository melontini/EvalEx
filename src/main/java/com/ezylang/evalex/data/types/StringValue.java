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
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import lombok.*;

@ToString()
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringValue extends EvaluationValue {

  private final String value;

  public static StringValue of(@NonNull String string) {
    return new StringValue(string);
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public boolean isStringValue() {
    return true;
  }

  @Override
  public BigDecimal getNumberValue() {
    if ("true".equalsIgnoreCase(value)) return BigDecimal.ONE;
    if ("false".equalsIgnoreCase(value)) return BigDecimal.ZERO;

    try {
      return new BigDecimal(value);
    } catch (NumberFormatException e) {
      return BigDecimal.ZERO;
    }
  }

  @Override
  public String getStringValue() {
    return value;
  }

  @Override
  public Boolean getBooleanValue() {
    return Boolean.parseBoolean(value);
  }

  @Override
  public Instant getDateTimeValue() {
    try {
      return Instant.parse(value);
    } catch (DateTimeException e) {
      return Instant.EPOCH;
    }
  }

  @Override
  public Duration getDurationValue() {
    try {
      return Duration.parse(value);
    } catch (DateTimeException e) {
      return Duration.ZERO;
    }
  }

  @Override
  public int compareTo(EvaluationValue toCompare) {
    return value.compareTo(toCompare.getStringValue());
  }
}
