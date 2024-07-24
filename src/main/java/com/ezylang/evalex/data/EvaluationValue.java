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
package com.ezylang.evalex.data;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.types.ExpressionNodeValue;
import com.ezylang.evalex.parser.ASTNode;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The representation of the final or intermediate evaluation result value. The representation
 * consists of a data type and data value. Depending on the type, the value will be stored in a
 * corresponding object type.
 */
public abstract class EvaluationValue implements Comparable<EvaluationValue> {

  /**
   * Creates a new evaluation value by using the configured converter and configuration.
   *
   * @param value One of the supported data types.
   * @param configuration The expression configuration to use; not null
   * @throws IllegalArgumentException if the data type can't be mapped.
   * @see ExpressionConfiguration#getEvaluationValueConverter()
   */
  public static EvaluationValue of(Object value, ExpressionConfiguration configuration) {
    return configuration.getEvaluationValueConverter().convertObject(value, configuration);
  }

  public abstract Object getValue();

  public boolean isNumberValue() {
    return false;
  }

  public boolean isStringValue() {
    return false;
  }

  public boolean isBooleanValue() {
    return false;
  }

  public boolean isDateTimeValue() {
    return false;
  }

  public boolean isDurationValue() {
    return false;
  }

  public boolean isArrayValue() {
    return false;
  }

  public boolean isStructureValue() {
    return false;
  }

  public boolean isDataAccessorValue() {
    return false;
  }

  public boolean isExpressionNode() {
    return false;
  }

  public boolean isNullValue() {
    return false;
  }

  /**
   * Gets a {@link BigDecimal} representation of the value. If possible and needed, a conversion
   * will be made.
   *
   * <ul>
   *   <li>Boolean <code>true</code> will return a {@link BigDecimal#ONE}, else {@link
   *       BigDecimal#ZERO}.
   * </ul>
   *
   * @return The {@link BigDecimal} representation of the value, or {@link BigDecimal#ZERO} if
   *     conversion is not possible.
   */
  public BigDecimal getNumberValue() {
    return BigDecimal.ZERO;
  }

  /**
   * Gets a {@link String} representation of the value. If possible and needed, a conversion will be
   * made.
   *
   * <ul>
   *   <li>Number values will be returned as {@link BigDecimal#toPlainString()}.
   *   <li>The {@link Object#toString()} will be used in all other cases.
   * </ul>
   *
   * @return The {@link String} representation of the value.
   */
  public String getStringValue() {
    return Objects.toString(getValue());
  }

  /**
   * Gets a {@link Boolean} representation of the value. If possible and needed, a conversion will
   * be made.
   *
   * <ul>
   *   <li>Any non-zero number value will return true.
   *   <li>Any string with the value <code>"true"</code> (case ignored) will return true.
   * </ul>
   *
   * @return The {@link Boolean} representation of the value.
   */
  public Boolean getBooleanValue() {
    return false;
  }

  /**
   * Gets a {@link Instant} representation of the value. If possible and needed, a conversion will
   * be made.
   *
   * <ul>
   *   <li>Any number value will return the instant from the epoc value.
   *   <li>Any string with the string representation of a LocalDateTime (ex: <code>
   *       "2018-11-30T18:35:24.00"</code>) (case ignored) will return the current LocalDateTime.
   *   <li>The date {@link Instant#EPOCH} will return if a conversion error occurs or in all other
   *       cases.
   * </ul>
   *
   * @return The {@link Instant} representation of the value.
   */
  public Instant getDateTimeValue() {
    return Instant.EPOCH;
  }

  /**
   * Gets a {@link Duration} representation of the value. If possible and needed, a conversion will
   * be made.
   *
   * <ul>
   *   <li>Any non-zero number value will return the duration from the millisecond.
   *   <li>Any string with the string representation of an {@link Duration} (ex: <code>
   *       "PnDTnHnMn.nS"</code>) (case ignored) will return the current instant.
   *   <li>The {@link Duration#ZERO} will return if a conversion error occurs or in all other cases.
   * </ul>
   *
   * @return The {@link Duration} representation of the value.
   */
  public Duration getDurationValue() {
    return Duration.ZERO;
  }

  /**
   * Gets a {@link List<EvaluationValue>} representation of the value.
   *
   * @return The {@link List<EvaluationValue>} representation of the value or an empty list, if no
   *     conversion is possible.
   */
  public List<EvaluationValue> getArrayValue() {
    return Collections.emptyList();
  }

  /**
   * Gets a {@link Map} representation of the value.
   *
   * @return The {@link Map} representation of the value or an empty list, if no conversion is
   *     possible.
   */
  public Map<String, EvaluationValue> getStructureValue() {
    return Collections.emptyMap();
  }

  public DataAccessorIfc getDataAccessorValue() {
    return null;
  }

  /**
   * Gets the expression node, if this value is of type {@link ExpressionNodeValue}.
   *
   * @return The expression node, or null for any other data type.
   */
  public ASTNode getExpressionNode() {
    return null;
  }

  @Override
  public int compareTo(EvaluationValue toCompare) {
    return getStringValue().compareTo(toCompare.getStringValue());
  }
}
