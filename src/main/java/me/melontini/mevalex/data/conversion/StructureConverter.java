/*
  Copyright 2012-2023 Udo Klimaschewski

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
package me.melontini.mevalex.data.conversion;

import java.util.HashMap;
import java.util.Map;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.StructureValue;

/** Converter to convert to the STRUCTURE data type. */
public class StructureConverter implements ConverterIfc {
  @Override
  public EvaluationValue convert(Object object, ExpressionConfiguration configuration) {
    Map<String, EvaluationValue> structure = new HashMap<>();
    for (Map.Entry<?, ?> entry : ((Map<?, ?>) object).entrySet()) {
      String name = entry.getKey().toString();
      structure.put(name, EvaluationValue.of(entry.getValue(), configuration));
    }
    return StructureValue.of(structure);
  }

  @Override
  public boolean canConvert(Object object) {
    return object instanceof Map;
  }
}
