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
import java.util.Map;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import me.melontini.mevalex.data.EvaluationValue;

@ToString()
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NullValue implements EvaluationValue {

  private static final NullValue INSTANCE = new NullValue();

  public static NullValue of() {
    return INSTANCE;
  }

  @Override
  public Object getValue() {
    return null;
  }

  @Override
  public String getName() {
    return "null";
  }

  @Override
  public boolean isNullValue() {
    return true;
  }

  @Override
  public BigDecimal getNumberValue() {
    return null;
  }

  @Override
  public String getStringValue() {
    return null;
  }

  @Override
  public Boolean getBooleanValue() {
    return null;
  }

  @Override
  public List<EvaluationValue> getArrayValue() {
    return null;
  }

  @Override
  public Map<String, EvaluationValue> getStructureValue() {
    return null;
  }

  @Override
  public int compareTo(EvaluationValue toCompare) {
    throw new NullPointerException("Can not compare a null value");
  }
}
