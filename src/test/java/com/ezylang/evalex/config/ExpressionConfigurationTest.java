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

import static org.assertj.core.api.Assertions.assertThat;

import com.ezylang.evalex.EvaluationContext;
import com.ezylang.evalex.config.TestConfigurationProvider.DummyFunction;
import com.ezylang.evalex.data.DataAccessorIfc;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.types.StringValue;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.operators.arithmetic.InfixPlusOperator;
import com.ezylang.evalex.parser.Token;
import java.math.MathContext;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExpressionConfigurationTest {

  @Test
  void testDefaultSetup() {
    ExpressionConfiguration configuration = ExpressionConfiguration.defaultConfiguration();

    assertThat(configuration.getMathContext())
        .isEqualTo(ExpressionConfiguration.DEFAULT_MATH_CONTEXT);
    assertThat(configuration.getOperatorDictionary()).isInstanceOf(OperatorDictionary.class);
    assertThat(configuration.getFunctionDictionary()).isInstanceOf(FunctionDictionary.class);
    assertThat(configuration.getDataAccessorSupplier().get()).isEqualTo(null);
    assertThat(configuration.isArraysAllowed()).isTrue();
    assertThat(configuration.isStructuresAllowed()).isTrue();
    assertThat(configuration.isImplicitMultiplicationAllowed()).isTrue();
    assertThat(configuration.getPowerOfPrecedence())
        .isEqualTo(OperatorIfc.OPERATOR_PRECEDENCE_POWER);
    assertThat(configuration.getConstants())
        .containsAllEntriesOf(ExpressionConfiguration.StandardConstants);
    assertThat(configuration.getDecimalPlacesRounding())
        .isEqualTo(ExpressionConfiguration.DECIMAL_PLACES_ROUNDING_UNLIMITED);
    assertThat(configuration.isStripTrailingZeros()).isTrue();
    assertThat(configuration.isAllowOverwriteConstants()).isFalse();
    assertThat(configuration.getZoneId()).isEqualTo(ZoneId.systemDefault());
    assertThat(configuration.getLocale()).isEqualTo(Locale.getDefault());
    assertThat(configuration.isSingleQuoteStringLiteralsAllowed()).isFalse();
  }

  @Test
  void testWithAdditionalOperators() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder()
            .operatorDictionary(
                ExpressionConfiguration.getStandardOperators(
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
                    .infix("ADDED1", new InfixPlusOperator())
                    .infix("ADDED2", new InfixPlusOperator())
                    .build())
            .build();

    assertThat(configuration.getOperatorDictionary().hasInfixOperator("ADDED1")).isTrue();
    assertThat(configuration.getOperatorDictionary().hasInfixOperator("ADDED2")).isTrue();
  }

  @Test
  void testWithAdditionalFunctions() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder()
            .functionDictionary(
                ExpressionConfiguration.getStandardFunctions(
                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
                    .add("ADDED1", new DummyFunction())
                    .add("ADDED2", new DummyFunction())
                    .build())
            .build();

    assertThat(configuration.getFunctionDictionary().hasFunction("ADDED1")).isTrue();
    assertThat(configuration.getFunctionDictionary().hasFunction("ADDED2")).isTrue();
  }

  @Test
  void testCustomMathContext() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().mathContext(MathContext.DECIMAL32).build();

    assertThat(configuration.getMathContext()).isEqualTo(MathContext.DECIMAL32);
  }

  @Test
  void testCustomOperatorDictionary() {
    OperatorDictionary mockedOperatorDictionary = Mockito.mock(OperatorDictionary.class);

    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().operatorDictionary(mockedOperatorDictionary).build();

    assertThat(configuration.getOperatorDictionary()).isEqualTo(mockedOperatorDictionary);
  }

  @Test
  void testCustomFunctionDictionary() {
    FunctionDictionary mockedFunctionDictionary = Mockito.mock(FunctionDictionary.class);

    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().functionDictionary(mockedFunctionDictionary).build();

    assertThat(configuration.getFunctionDictionary()).isEqualTo(mockedFunctionDictionary);
  }

  @Test
  void testCustomDataAccessorSupplier() {
    DataAccessorIfc mockedDataAccessor = Mockito.mock(DataAccessorIfc.class);

    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().dataAccessorSupplier(() -> mockedDataAccessor).build();

    assertThat(configuration.getDataAccessorSupplier().get()).isEqualTo(mockedDataAccessor);
  }

  @SuppressWarnings("Convert2Lambda")
  @Test
  void testDataAccessorSupplierReturnsNewInstance() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder()
            .dataAccessorSupplier(
                () ->
                    new DataAccessorIfc() {
                      @Override
                      public EvaluationValue getData(
                          String variable, Token token, EvaluationContext context) {
                        return StringValue.of(variable);
                      }
                    })
            .build();

    DataAccessorIfc accessor1 = configuration.getDataAccessorSupplier().get();
    DataAccessorIfc accessor2 = configuration.getDataAccessorSupplier().get();

    assertThat(accessor1).isNotEqualTo(accessor2);
  }

  @Test
  void testCustomConstants() {
    Map<String, EvaluationValue> constants =
        Map.of(
            "A", StringValue.of("a"),
            "B", StringValue.of("b"));
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().constants(constants).build();

    assertThat(configuration.getConstants()).containsAllEntriesOf(constants);
  }

  @Test
  void testArraysAllowed() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().arraysAllowed(false).build();

    assertThat(configuration.isArraysAllowed()).isFalse();
  }

  @Test
  void testStructuresAllowed() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().structuresAllowed(false).build();

    assertThat(configuration.isStructuresAllowed()).isFalse();
  }

  @Test
  void testSingleQuoteStringLiteralsAllowed() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().singleQuoteStringLiteralsAllowed(true).build();

    assertThat(configuration.isSingleQuoteStringLiteralsAllowed()).isTrue();
  }

  @Test
  void testDecimalPlacesRounding() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().decimalPlacesRounding(2).build();

    assertThat(configuration.getDecimalPlacesRounding()).isEqualTo(2);
  }

  @Test
  void testStripTrailingZeros() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().stripTrailingZeros(false).build();

    assertThat(configuration.isStripTrailingZeros()).isFalse();
  }

  @Test
  void testAllowOverwriteConstants() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().allowOverwriteConstants(false).build();

    assertThat(configuration.isAllowOverwriteConstants()).isFalse();
  }

  @Test
  void testZoneId() {
    ZoneId zoneId = ZoneId.of("Asia/Shanghai");
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().zoneId(zoneId).build();

    assertThat(configuration.getZoneId()).isEqualTo(zoneId);
  }

  @Test
  void testLocale() {
    Locale locale = Locale.CHINA;
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().locale(locale).build();

    assertThat(configuration.getLocale()).isEqualTo(locale);
  }

  @Test
  void testImplicitMultiplicationAllowed() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder().implicitMultiplicationAllowed(false).build();

    assertThat(configuration.isImplicitMultiplicationAllowed()).isFalse();
  }

  @Test
  void testPowerOfPrecedence() {
    ExpressionConfiguration configuration =
        ExpressionConfiguration.builder()
            .powerOfPrecedence(OperatorIfc.OPERATOR_PRECEDENCE_POWER_HIGHER)
            .build();

    assertThat(configuration.getPowerOfPrecedence())
        .isEqualTo(OperatorIfc.OPERATOR_PRECEDENCE_POWER_HIGHER);
  }
}
