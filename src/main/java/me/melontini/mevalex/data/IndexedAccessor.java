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
package me.melontini.mevalex.data;

import java.math.BigDecimal;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.parser.Token;
import org.jetbrains.annotations.Nullable;

/**
 * An indexed accessor is responsible for accessing data, e.g. indexed variable values during an
 * expression evaluation. <br>
 * When implemented on {@link EvaluationValue} will act as an array.
 */
public interface IndexedAccessor {

  /**
   * Retrieves a data value.
   *
   * @return The data value, or <code>null</code> if index is out of bounds.
   */
  @Nullable
  EvaluationValue getIndexedData(BigDecimal index, Token token, EvaluationContext context)
      throws EvaluationException;
}
