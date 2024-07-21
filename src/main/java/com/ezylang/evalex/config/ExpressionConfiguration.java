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
package com.ezylang.evalex.config;

import com.ezylang.evalex.data.DataAccessorIfc;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.conversion.DefaultEvaluationValueConverter;
import com.ezylang.evalex.data.conversion.EvaluationValueConverterIfc;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.functions.basic.*;
import com.ezylang.evalex.functions.datetime.*;
import com.ezylang.evalex.functions.string.*;
import com.ezylang.evalex.functions.trigonometric.*;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.operators.arithmetic.*;
import com.ezylang.evalex.operators.booleans.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

/**
 * The expression configuration can be used to configure various aspects of expression parsing and
 * evaluation. <br>
 * A <code>Builder</code> is provided to create custom configurations, e.g.: <br>
 *
 * <pre>
 *   ExpressionConfiguration config = ExpressionConfiguration.builder().mathContext(MathContext.DECIMAL32).arraysAllowed(false).build();
 * </pre>
 */
@With
@Builder(toBuilder = true)
@Getter
public class ExpressionConfiguration {

  /** The standard set constants for EvalEx. */
  public static final Map<String, EvaluationValue> StandardConstants =
      Collections.unmodifiableMap(
          getStandardConstants(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));

  /** Setting the decimal places to unlimited, will disable intermediate rounding. */
  public static final int DECIMAL_PLACES_ROUNDING_UNLIMITED = -1;

  /** The default math context has a precision of 68 and {@link RoundingMode#HALF_EVEN}. */
  public static final MathContext DEFAULT_MATH_CONTEXT =
      new MathContext(68, RoundingMode.HALF_EVEN);

  /**
   * The default date time formatters used when parsing a date string. Each format will be tried and
   * the first matching will be used.
   *
   * <ul>
   *   <li>{@link DateTimeFormatter#ISO_DATE_TIME}
   *   <li>{@link DateTimeFormatter#ISO_DATE}
   *   <li>{@link DateTimeFormatter#ISO_LOCAL_DATE_TIME}
   *   <li>{@link DateTimeFormatter#ISO_LOCAL_DATE}
   * </ul>
   */
  protected static final List<DateTimeFormatter> DEFAULT_DATE_TIME_FORMATTERS =
      new ArrayList<>(
          List.of(
              DateTimeFormatter.ISO_DATE_TIME,
              DateTimeFormatter.ISO_DATE,
              DateTimeFormatter.ISO_LOCAL_DATE_TIME,
              DateTimeFormatter.ISO_LOCAL_DATE,
              DateTimeFormatter.RFC_1123_DATE_TIME));

  private static final ExpressionConfiguration DEFAULT = ExpressionConfiguration.builder().build();

  /** The operator dictionary holds all operators that will be allowed in an expression. */
  @Builder.Default
  private final OperatorDictionary operatorDictionary =
      getStandardOperators(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)).build();

  /** The function dictionary holds all functions that will be allowed in an expression. */
  @Builder.Default
  private final FunctionDictionary functionDictionary =
      getStandardFunctions(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)).build();

  /** The math context to use. */
  @Builder.Default private final MathContext mathContext = DEFAULT_MATH_CONTEXT;

  /**
   * The data accessor is responsible for accessing variable and constant values in an expression.
   * The supplier will be called once for each new expression providing a new storage for each
   * expression.
   */
  @Builder.Default private final Supplier<DataAccessorIfc> dataAccessorSupplier = () -> null;

  @Builder.Default
  private final Supplier<Map<?, ?>> defaultMapConstructor =
      () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

  /**
   * Default constants will be added automatically to each expression and can be used in expression
   * evaluation.
   */
  @Builder.Default
  private final Map<String, EvaluationValue> defaultConstants =
      getStandardConstants(() -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER));

  /** Support for arrays in expressions are allowed or not. */
  @Builder.Default private final boolean arraysAllowed = true;

  /** Support for structures in expressions are allowed or not. */
  @Builder.Default private final boolean structuresAllowed = true;

  /**
   * Support for the binary (undefined) data type is allowed or not.
   *
   * @since 3.3.0
   */
  @Builder.Default private final boolean binaryAllowed = false;

  /** Support for implicit multiplication, like in (a+b)(b+c) are allowed or not. */
  @Builder.Default private final boolean implicitMultiplicationAllowed = true;

  /** Support for single quote string literals, like in 'Hello World' are allowed or not. */
  @Builder.Default private final boolean singleQuoteStringLiteralsAllowed = false;

  /**
   * The power of operator precedence, can be set higher {@link
   * OperatorIfc#OPERATOR_PRECEDENCE_POWER_HIGHER} or to a custom value.
   */
  @Builder.Default private final int powerOfPrecedence = OperatorIfc.OPERATOR_PRECEDENCE_POWER;

  /**
   * If specified, only the final result of the evaluation will be rounded to the specified number
   * of decimal digits, using the MathContexts rounding mode.
   *
   * <p>The default value of _DECIMAL_PLACES_ROUNDING_UNLIMITED_ will disable rounding.
   */
  @Builder.Default private final int decimalPlacesResult = DECIMAL_PLACES_ROUNDING_UNLIMITED;

  /**
   * If specified, all results from operations and functions will be rounded to the specified number
   * of decimal digits, using the MathContexts rounding mode.
   *
   * <p>Automatic rounding is disabled by default. When enabled, EvalEx will round all input
   * variables, constants, intermediate operation and function results and the final result to the
   * specified number of decimal digits, using the current rounding mode. Using a value of
   * _DECIMAL_PLACES_ROUNDING_UNLIMITED_ will disable automatic rounding.
   */
  @Builder.Default private final int decimalPlacesRounding = DECIMAL_PLACES_ROUNDING_UNLIMITED;

  /**
   * If set to true (default), then the trailing decimal zeros in a number result will be stripped.
   */
  @Builder.Default private final boolean stripTrailingZeros = true;

  /**
   * If set to true (default), then variables can be set that have the name of a constant. In that
   * case, the constant value will be removed and a variable value will be set.
   */
  @Builder.Default private final boolean allowOverwriteConstants = true;

  /** The time zone id. By default, the system default zone ID is used. */
  @Builder.Default private final ZoneId zoneId = ZoneId.systemDefault();

  /** The locale. By default, the system default locale is used. */
  @Builder.Default private final Locale locale = Locale.getDefault();

  /**
   * The date-time formatters. When parsing, each format will be tried and the first matching will
   * be used. For formatting, only the first will be used.
   *
   * <p>By default, the {@link ExpressionConfiguration#DEFAULT_DATE_TIME_FORMATTERS} are used.
   */
  @Builder.Default
  private final List<DateTimeFormatter> dateTimeFormatters = DEFAULT_DATE_TIME_FORMATTERS;

  /** The converter to use when converting different data types to an {@link EvaluationValue}. */
  @Builder.Default
  private final EvaluationValueConverterIfc evaluationValueConverter =
      new DefaultEvaluationValueConverter();

  /**
   * Convenience method to create a default configuration.
   *
   * @return A configuration with default settings.
   */
  public static ExpressionConfiguration defaultConfiguration() {
    return DEFAULT;
  }

  public static OperatorDictionary.Builder getStandardOperators(
      Supplier<Map<String, OperatorIfc>> supplier) {
    return OperatorDictionary.builder(supplier)
        // arithmetic
        .prefix("+", new PrefixPlusOperator())
        .prefix("-", new PrefixMinusOperator())
        .infix("+", new InfixPlusOperator())
        .infix("-", new InfixMinusOperator())
        .infix("*", new InfixMultiplicationOperator())
        .infix("/", new InfixDivisionOperator())
        .infix("^", new InfixPowerOfOperator())
        .infix("%", new InfixModuloOperator())
        // booleans
        .infix("=", new InfixEqualsOperator())
        .infix("==", new InfixEqualsOperator())
        .infix("!=", new InfixNotEqualsOperator())
        .infix("<>", new InfixNotEqualsOperator())
        .infix(">", new InfixGreaterOperator())
        .infix(">=", new InfixGreaterEqualsOperator())
        .infix("<", new InfixLessOperator())
        .infix("<=", new InfixLessEqualsOperator())
        .infix("&&", new InfixAndOperator())
        .infix("||", new InfixOrOperator())
        .prefix("!", new PrefixNotOperator());
  }

  public static FunctionDictionary.Builder getStandardFunctions(
      Supplier<Map<String, FunctionIfc>> supplier) {
    return FunctionDictionary.builder(supplier)
        // basic functions
        .add("ABS", new AbsFunction())
        .add("AVERAGE", new AverageFunction())
        .add("CEILING", new CeilingFunction())
        .add("COALESCE", new CoalesceFunction())
        .add("FACT", new FactFunction())
        .add("FLOOR", new FloorFunction())
        .add("IF", new IfFunction())
        .add("LOG", new LogFunction())
        .add("LOG10", new Log10Function())
        .add("MAX", new MaxFunction())
        .add("MIN", new MinFunction())
        .add("NOT", new NotFunction())
        .add("RANDOM", new RandomFunction())
        .add("ROUND", new RoundFunction())
        .add("SQRT", new SqrtFunction())
        .add("SUM", new SumFunction())
        .add("SWITCH", new SwitchFunction())
        // trigonometric
        .add("ACOS", new AcosFunction())
        .add("ACOSH", new AcosHFunction())
        .add("ACOSR", new AcosRFunction())
        .add("ACOT", new AcotFunction())
        .add("ACOTH", new AcotHFunction())
        .add("ACOTR", new AcotRFunction())
        .add("ASIN", new AsinFunction())
        .add("ASINH", new AsinHFunction())
        .add("ASINR", new AsinRFunction())
        .add("ATAN", new AtanFunction())
        .add("ATAN2", new Atan2Function())
        .add("ATAN2R", new Atan2RFunction())
        .add("ATANH", new AtanHFunction())
        .add("ATANR", new AtanRFunction())
        .add("COS", new CosFunction())
        .add("COSH", new CosHFunction())
        .add("COSR", new CosRFunction())
        .add("COT", new CotFunction())
        .add("COTH", new CotHFunction())
        .add("COTR", new CotRFunction())
        .add("CSC", new CscFunction())
        .add("CSCH", new CscHFunction())
        .add("CSCR", new CscRFunction())
        .add("DEG", new DegFunction())
        .add("RAD", new RadFunction())
        .add("SIN", new SinFunction())
        .add("SINH", new SinHFunction())
        .add("SINR", new SinRFunction())
        .add("SEC", new SecFunction())
        .add("SECH", new SecHFunction())
        .add("SECR", new SecRFunction())
        .add("TAN", new TanFunction())
        .add("TANH", new TanHFunction())
        .add("TANR", new TanRFunction())
        // string functions
        .add("STR_CONTAINS", new StringContains())
        .add("STR_ENDS_WITH", new StringEndsWithFunction())
        .add("STR_FORMAT", new StringFormatFunction())
        .add("STR_LOWER", new StringLowerFunction())
        .add("STR_STARTS_WITH", new StringStartsWithFunction())
        .add("STR_TRIM", new StringTrimFunction())
        .add("STR_UPPER", new StringUpperFunction())
        // date time functions
        .add("DT_DATE_NEW", new DateTimeNewFunction())
        .add("DT_DATE_PARSE", new DateTimeParseFunction())
        .add("DT_DATE_FORMAT", new DateTimeFormatFunction())
        .add("DT_DATE_TO_EPOCH", new DateTimeToEpochFunction())
        .add("DT_DURATION_NEW", new DurationNewFunction())
        .add("DT_DURATION_FROM_MILLIS", new DurationFromMillisFunction())
        .add("DT_DURATION_TO_MILLIS", new DurationToMillisFunction())
        .add("DT_DURATION_PARSE", new DurationParseFunction())
        .add("DT_NOW", new DateTimeNowFunction())
        .add("DT_TODAY", new DateTimeTodayFunction());
  }

  private static Map<String, EvaluationValue> getStandardConstants(
      Supplier<Map<String, EvaluationValue>> supplier) {

    Map<String, EvaluationValue> constants = supplier.get();

    constants.put("TRUE", EvaluationValue.TRUE);
    constants.put("FALSE", EvaluationValue.FALSE);
    constants.put(
        "PI",
        EvaluationValue.numberValue(
            new BigDecimal(
                "3.1415926535897932384626433832795028841971693993751058209749445923078164062862089986280348253421170679")));
    constants.put(
        "E",
        EvaluationValue.numberValue(
            new BigDecimal(
                "2.71828182845904523536028747135266249775724709369995957496696762772407663")));
    constants.put("NULL", EvaluationValue.NULL_VALUE);

    constants.put(
        "DT_FORMAT_ISO_DATE_TIME",
        EvaluationValue.stringValue("yyyy-MM-dd'T'HH:mm:ss[.SSS][XXX]['['VV']']"));
    constants.put(
        "DT_FORMAT_LOCAL_DATE_TIME", EvaluationValue.stringValue("yyyy-MM-dd'T'HH:mm:ss[.SSS]"));
    constants.put("DT_FORMAT_LOCAL_DATE", EvaluationValue.stringValue("yyyy-MM-dd"));

    return constants;
  }
}
