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
package com.ezylang.evalex.functions;

import com.ezylang.evalex.EvaluationContext;
import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.data.types.ExpressionNodeValue;
import com.ezylang.evalex.parser.ASTNode;
import com.ezylang.evalex.parser.InlinedASTNode;
import com.ezylang.evalex.parser.Token;
import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Interface that is required for all functions in a function dictionary for evaluation of
 * expressions.
 */
public interface FunctionIfc {

  /**
   * Returns the list of parameter definitions. Is never empty or <code>null</code>.
   *
   * @return The parameter definition list.
   */
  List<FunctionParameterDefinition> getFunctionParameterDefinitions();

  /**
   * Performs the function logic and returns an evaluation result.
   *
   * @param functionToken The function token from the parsed expression.
   * @param parameterValues The parameter values.
   * @return The evaluation result in form of a {@link EvaluationValue}.
   * @throws EvaluationException In case there were problems during evaluation.
   */
  EvaluationValue evaluate(
      EvaluationContext context, Token functionToken, EvaluationValue... parameterValues)
      throws EvaluationException;

  /**
   * Validates the evaluation parameters, called before the actual evaluation.
   *
   * @param token The function token.
   * @param parameterValues The parameter values
   * @throws EvaluationException in case of any validation error
   */
  void validatePreEvaluation(Token token, EvaluationValue... parameterValues)
      throws EvaluationException;

  /**
   * Checks whether the function has a variable number of arguments parameter.
   *
   * @return <code>true</code> or <code>false</code>:
   */
  boolean hasVarArgs();

  /**
   * Checks if the parameter is a lazy parameter.
   *
   * @param parameterIndex The parameter index, starts at 0 for the first parameter. If the index is
   *     bigger than the list of parameter definitions, the last parameter definition will be
   *     checked.
   * @return <code>true</code> if the specified parameter is defined as lazy.
   */
  default boolean isParameterLazy(int parameterIndex) {
    if (parameterIndex >= getFunctionParameterDefinitions().size()) {
      parameterIndex = getFunctionParameterDefinitions().size() - 1;
    }
    return getFunctionParameterDefinitions().get(parameterIndex).isLazy();
  }

  /**
   * Returns the count of non-var-arg parameters defined by this function. If the function has
   * var-args, the result is the count of parameter definitions - 1.
   *
   * @return the count of non-var-arg parameters defined by this function.
   */
  default int getCountOfNonVarArgParameters() {
    int numOfParameters = getFunctionParameterDefinitions().size();
    return hasVarArgs() ? numOfParameters - 1 : numOfParameters;
  }

  default boolean forceInline() {
    return false;
  }

  default @Nullable EvaluationValue inlineFunction(
      Expression expression, Token token, ASTNode... parameters) throws EvaluationException {

    EvaluationValue[] parsed;
    if (parameters.length == 0) {
      parsed = EvaluationValue.EMPTY;
    } else {
      parsed = new EvaluationValue[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        if (token.getFunctionDefinition().isParameterLazy(i)) {
          parsed[i] = ExpressionNodeValue.of(parameters[i]);
        } else {
          parsed[i] = ((InlinedASTNode) parameters[i]).value();
        }
      }
    }

    this.validatePreEvaluation(token, parsed);
    return this.evaluate(EvaluationContext.builder(expression).build(), token, parsed);
  }
}
