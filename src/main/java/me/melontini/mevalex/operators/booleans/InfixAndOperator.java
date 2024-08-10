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
package me.melontini.mevalex.operators.booleans;

import static me.melontini.mevalex.operators.OperatorIfc.OPERATOR_PRECEDENCE_AND;

import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.data.types.BooleanValue;
import me.melontini.mevalex.operators.AbstractOperator;
import me.melontini.mevalex.operators.InfixOperator;
import me.melontini.mevalex.parser.Token;

/** Boolean AND of two values. */
@InfixOperator(precedence = OPERATOR_PRECEDENCE_AND, operandsLazy = true)
public class InfixAndOperator extends AbstractOperator {

  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token operatorToken, EvaluationValue... operands)
      throws EvaluationException {
    return BooleanValue.of(
        context.expression().evaluateSubtree(operands[0].getSolvable(), context).getBooleanValue()
            && context
                .expression()
                .evaluateSubtree(operands[1].getSolvable(), context)
                .getBooleanValue());
  }
}
