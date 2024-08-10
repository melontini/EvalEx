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
package me.melontini.mevalex.functions.basic;

import java.security.SecureRandom;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.functions.AbstractFunction;
import me.melontini.mevalex.parser.Token;

/** Random function produces a random value between 0 and 1. */
public class RandomFunction extends AbstractFunction {

  @Override
  public EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues) {

    SecureRandom secureRandom = new SecureRandom();

    return context.expression().convertDoubleValue(secureRandom.nextDouble());
  }
}
