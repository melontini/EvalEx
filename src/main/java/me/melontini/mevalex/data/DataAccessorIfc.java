/*
  Copyright 2012-2022 Udo Klimaschewski

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

import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.parser.Token;
import org.jetbrains.annotations.Nullable;

/**
 * A data accessor is responsible for accessing data, e.g. variable values during an expression
 * evaluation. <br>
 * When implemented on {@link EvaluationValue} will act as a structure.
 */
public interface DataAccessorIfc {

  /**
   * Retrieves a data value.
   *
   * @param variable The variable name, e.g. a variable or constant name.
   * @return The data value, or <code>null</code> if not found.
   */
  @Nullable
  EvaluationValue getVariableData(String variable, Token token, EvaluationContext context)
      throws EvaluationException;
}
