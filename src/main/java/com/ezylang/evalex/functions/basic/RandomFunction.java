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
package com.ezylang.evalex.functions.basic;

import com.ezylang.evalex.EvaluationContext;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.AbstractFunction;
import com.ezylang.evalex.parser.InlinedASTNode;
import com.ezylang.evalex.parser.Token;
import java.security.SecureRandom;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/** Random function produces a random value between 0 and 1. */
public class RandomFunction extends AbstractFunction {

  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {

    SecureRandom secureRandom = new SecureRandom();

    return context.expression().convertDoubleValue(secureRandom.nextDouble());
  }

  @Override
  public @Nullable EvaluationValue inlineFunction(
      Expression expression, Token token, List<InlinedASTNode> parameters) {
    return null;
  }
}
