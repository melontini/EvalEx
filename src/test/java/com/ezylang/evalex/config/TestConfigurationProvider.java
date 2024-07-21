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

import com.ezylang.evalex.EvaluationContext;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.functions.FunctionParameter;
import com.ezylang.evalex.operators.AbstractOperator;
import com.ezylang.evalex.operators.PostfixOperator;
import com.ezylang.evalex.operators.PrefixOperator;
import com.ezylang.evalex.parser.ExpressionParser;
import com.ezylang.evalex.parser.Token;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TreeMap;

public class TestConfigurationProvider {

  public static final ExpressionConfiguration StandardConfigurationWithAdditionalTestOperators =
      ExpressionConfiguration.builder()
          .zoneId(ZoneId.of("Europe/Berlin"))
          .locale(Locale.US)
          .operatorDictionary(
              ExpressionConfiguration.getStandardOperators(
                      () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
                  .prefix("++", new PrefixPlusPlusOperator())
                  .postfix("++", new PostfixPlusPlusOperator())
                  .postfix("?", new PostfixQuestionOperator())
                  .build())
          .functionDictionary(
              ExpressionConfiguration.getStandardFunctions(
                      () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER))
                  .add("TEST", new DummyFunction())
                  .build())
          .build();

  public static final ExpressionParser StandardParserWithAdditionalTestOperators =
      new ExpressionParser(StandardConfigurationWithAdditionalTestOperators);

  public static final ExpressionConfiguration GermanConfiguration =
      ExpressionConfiguration.builder()
          .zoneId(ZoneId.of("Europe/Berlin"))
          .locale(Locale.GERMAN)
          .build();
  public static final ExpressionConfiguration ChicagoConfiguration =
      ExpressionConfiguration.builder()
          .zoneId(ZoneId.of("America/Chicago"))
          .locale(Locale.ENGLISH)
          .build();

  @FunctionParameter(name = "input", isVarArg = true)
  public static class DummyFunction extends AbstractFunction {
    @Override
    public EvaluationValue evaluate(
        EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {
      // dummy implementation
      return context.expression().convertValue("OK");
    }
  }

  @PrefixOperator(leftAssociative = false)
  public static class PrefixPlusPlusOperator extends AbstractOperator {
    @Override
    public EvaluationValue evaluate(
        EvaluationContext context, Token operatorToken, EvaluationValue... operands) {
      // dummy implementation
      EvaluationValue operand = operands[0];
      return EvaluationValue.numberValue(operand.getNumberValue().add(BigDecimal.ONE));
    }
  }

  @PostfixOperator()
  public static class PostfixPlusPlusOperator extends AbstractOperator {
    @Override
    public EvaluationValue evaluate(
        EvaluationContext context, Token operatorToken, EvaluationValue... operands) {
      // dummy implementation
      EvaluationValue operand = operands[0];
      return EvaluationValue.numberValue(operand.getNumberValue().add(BigDecimal.ONE));
    }
  }

  @PostfixOperator(leftAssociative = false)
  public static class PostfixQuestionOperator extends AbstractOperator {
    @Override
    public EvaluationValue evaluate(
        EvaluationContext context, Token operatorToken, EvaluationValue... operands) {
      // dummy implementation
      return EvaluationValue.stringValue("?");
    }
  }
}
