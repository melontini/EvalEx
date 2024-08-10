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
package com.ezylang.evalex.data.conversion;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.types.NullValue;
import com.ezylang.evalex.parser.ASTNode;
import com.ezylang.evalex.parser.InlinedASTNode;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * The default implementation of the {@link EvaluationValueConverterIfc}, used in the standard
 * configuration.
 *
 * <table>
 *   <tr>
 *     <th>Input type</th><th>Converter used</th>
 *   </tr>
 *   <tr><td>BigDecimal</td><td>NumberConverter</td></tr>
 *   <tr><td>Long, long</td><td>NumberConverter</td></tr>
 *   <tr><td>Integer, int</td><td>NumberConverter</td></tr>
 *   <tr><td>Short, short</td><td>NumberConverter</td></tr>
 *   <tr><td>Byte, byte</td><td>NumberConverter</td></tr>
 *   <tr><td>Double, double</td><td>NumberConverter *</td></tr>
 *   <tr><td>Float, float</td><td>NumberConverter *</td></tr>
 *   <tr><td>CharSequence , String</td><td>StringConverter</td></tr>
 *   <tr><td>Boolean, boolean</td><td>BooleanConverter</td></tr>
 *   <tr><td>Instant</td><td>DateTimeConverter</td></tr>
 *   <tr><td>Date</td><td>DateTimeConverter</td></tr>
 *   <tr><td>Calendar</td><td>DateTimeConverter</td></tr>
 *   <tr><td>ZonedDateTime</td><td>DateTimeConverter</td></tr>
 *   <tr><td>LocalDate</td><td>DateTimeConverter - the configured zone ID will be used for conversion</td></tr>
 *   <tr><td>LocalDateTime</td><td>DateTimeConverter - the configured zone ID will be used for conversion</td></tr>
 *   <tr><td>OffsetDateTime</td><td>DateTimeConverter</td></tr>
 *   <tr><td>Duration</td><td>DurationConverter</td></tr>
 *   <tr><td>ASTNode</td><td>ASTNode</td></tr>
 *   <tr><td>List&lt;?&gt;</td><td>ArrayConverter - each entry will be lazily converted</td></tr>
 *   <tr><td>Map&lt?,?&gt;</td><td>StructureConverter - each entry will be converted</td></tr>
 * </table>
 *
 * <i>* Be careful with conversion problems when using float or double, which are fractional
 * numbers. A (float)0.1 is e.g. converted to 0.10000000149011612</i>
 */
public class DefaultEvaluationValueConverter implements EvaluationValueConverterIfc {

  public static final NumberConverter NUMBER_CONVERTER = new NumberConverter();
  public static final StringConverter STRING_CONVERTER = new StringConverter();
  public static final BooleanConverter BOOLEAN_CONVERTER = new BooleanConverter();
  public static final DateTimeConverter DATE_TIME_CONVERTER = new DateTimeConverter();
  public static final DurationConverter DURATION_CONVERTER = new DurationConverter();
  public static final ExpressionNodeConverter EXPRESSION_NODE_CONVERTER =
      new ExpressionNodeConverter();
  public static final ArrayConverter ARRAY_CONVERTER = new ArrayConverter();
  public static final StructureConverter STRUCTURE_CONVERTER = new StructureConverter();

  static final List<ConverterIfc> converters =
      Arrays.asList(
          NUMBER_CONVERTER,
          STRING_CONVERTER,
          BOOLEAN_CONVERTER,
          DATE_TIME_CONVERTER,
          DURATION_CONVERTER,
          EXPRESSION_NODE_CONVERTER,
          ARRAY_CONVERTER,
          STRUCTURE_CONVERTER);

  static final Map<Class<?>, ConverterIfc> FAST_PATH;

  static {
    IdentityHashMap<Class<?>, ConverterIfc> fastPath = new IdentityHashMap<>();
    fastPath.put(BigDecimal.class, NUMBER_CONVERTER);
    fastPath.put(Double.class, NUMBER_CONVERTER);
    fastPath.put(Float.class, NUMBER_CONVERTER);
    fastPath.put(Integer.class, NUMBER_CONVERTER);
    fastPath.put(Long.class, NUMBER_CONVERTER);

    fastPath.put(String.class, STRING_CONVERTER);
    fastPath.put(Character.class, STRING_CONVERTER);

    fastPath.put(Boolean.class, BOOLEAN_CONVERTER);

    fastPath.put(Instant.class, DATE_TIME_CONVERTER);
    fastPath.put(Date.class, DATE_TIME_CONVERTER);

    fastPath.put(Duration.class, DURATION_CONVERTER);

    fastPath.put(ASTNode.class, EXPRESSION_NODE_CONVERTER);
    fastPath.put(InlinedASTNode.class, EXPRESSION_NODE_CONVERTER);

    fastPath.put(List.class, ARRAY_CONVERTER);
    fastPath.put(Object[].class, ARRAY_CONVERTER);

    fastPath.put(Map.class, STRUCTURE_CONVERTER);
    FAST_PATH = Collections.unmodifiableMap(fastPath);
  }

  @Override
  public EvaluationValue convertObject(Object object, ExpressionConfiguration configuration) {
    if (object == null) return NullValue.of();
    if (object instanceof EvaluationValue value) return value;

    var fastPath = FAST_PATH.get(object.getClass());
    if (fastPath != null) return fastPath.convert(object, configuration);

    for (ConverterIfc converter : converters) {
      if (converter.canConvert(object)) {
        return converter.convert(object, configuration);
      }
    }

    throw new IllegalArgumentException(
        "Unsupported data type '" + object.getClass().getName() + "'");
  }
}
