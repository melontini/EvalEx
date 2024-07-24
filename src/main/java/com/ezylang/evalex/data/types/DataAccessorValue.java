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

import com.ezylang.evalex.data.DataAccessorIfc;
import com.ezylang.evalex.data.EvaluationValue;
import lombok.*;

/**
 * This value type allows passing custom data accessors to expressions. Unlike structures, which
 * require implementing a handful of methods, this type does not. So, it can act like a proxy for
 * complex objects e.g. through reflection.
 *
 * @author melontini
 */
@ToString()
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DataAccessorValue extends EvaluationValue {

  private final DataAccessorIfc value;

  public static DataAccessorValue of(@NonNull DataAccessorIfc value) {
    return new DataAccessorValue(value);
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public boolean isDataAccessorValue() {
    return true;
  }

  @Override
  public DataAccessorIfc getDataAccessorValue() {
    return value;
  }
}
