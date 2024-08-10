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
import java.math.BigInteger;
import java.math.MathContext;
import java.time.Duration;
import java.time.Instant;
import lombok.*;
import me.melontini.mevalex.data.EvaluationValue;

@ToString()
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberValue implements EvaluationValue {

  private final BigDecimal value;

  public static NumberValue of(@NonNull BigDecimal decimal) {
    return new NumberValue(decimal);
  }

  /**
   * Creates a {@link NumberValue} value from a {@link String}.
   *
   * @param value The {@link String} value.
   * @param mathContext The math context to use for creation of the {@link BigDecimal} storage.
   */
  public static EvaluationValue ofString(String value, MathContext mathContext) {
    if (value.startsWith("0x") || value.startsWith("0X")) {
      BigInteger hexToInteger = new BigInteger(value.substring(2), 16);
      return of(new BigDecimal(hexToInteger, mathContext));
    } else {
      return of(new BigDecimal(value, mathContext));
    }
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public boolean isNumberValue() {
    return true;
  }

  @Override
  public BigDecimal getNumberValue() {
    return value;
  }

  @Override
  public String getStringValue() {
    return value.toPlainString();
  }

  @Override
  public Boolean getBooleanValue() {
    return value.compareTo(BigDecimal.ZERO) != 0;
  }

  @Override
  public Instant getDateTimeValue() {
    return Instant.ofEpochMilli(value.longValue());
  }

  @Override
  public Duration getDurationValue() {
    return Duration.ofMillis(value.longValue());
  }

  @Override
  public int compareTo(EvaluationValue toCompare) {
    return value.compareTo(toCompare.getNumberValue());
  }
}
