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
package com.ezylang.evalex.operators.arithmetic;

import com.ezylang.evalex.EvaluationContext;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.operators.AbstractOperator;
import com.ezylang.evalex.operators.PrefixOperator;
import com.ezylang.evalex.parser.Token;

/** Unary prefix minus. */
@PrefixOperator(leftAssociative = false)
public class PrefixMinusOperator extends AbstractOperator {

  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token operatorToken, EvaluationValue... operands)
      throws EvaluationException {
    EvaluationValue operand = operands[0];

    if (operand.isNumberValue()) {
      return context
          .expression()
          .convertValue(
              operand
                  .getNumberValue()
                  .negate(context.expression().getConfiguration().getMathContext()));
    } else {
      throw EvaluationException.ofUnsupportedDataTypeInOperation(operatorToken);
    }
  }
}
