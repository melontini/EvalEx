/*
  Copyright 2012-2024 Udo Klimaschewski

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
package com.ezylang.evalex.parser;

import com.ezylang.evalex.EvaluationException;
import com.ezylang.evalex.Expression;
import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class ExpressionParser {

  private final ExpressionConfiguration configuration;
  private final Tokenizer tokenizer;
  private final ShuntingYardConverter converter;

  public ExpressionParser(ExpressionConfiguration configuration) {
    this.configuration = configuration;
    this.tokenizer = new Tokenizer(configuration);
    this.converter = new ShuntingYardConverter(configuration);
  }

  public Expression parse(String expression) throws ParseException {
    return new Expression(
        expression,
        converter.toAbstractSyntaxTree(tokenizer.parse(expression), expression),
        configuration);
  }

  public Expression parseAndInline(String expression) throws ParseException, EvaluationException {
    Expression result = this.parse(expression);
    return new Expression(
        expression, this.inlineASTNode(result, result.getAbstractSyntaxTree()), configuration);
  }

  public Expression inlineExpression(Expression expression) throws EvaluationException {
    return new Expression(
        expression.getExpressionString(),
        this.inlineASTNode(expression, expression.getAbstractSyntaxTree()),
        expression.getConfiguration());
  }

  /**
   * Optional operation which attempts to inline nodes with constant results.<br>
   * This method attempts to inline constant variables, functions and operators.
   *
   * <p>If an operator cannot be inlined, it must implement {@link
   * OperatorIfc#inlineOperator(Expression, Token, List)} and return null. Same with functions, but
   * {@link FunctionIfc#inlineFunction(Expression, Token, List)}.
   *
   * @throws EvaluationException If there was an issue inlining the node value.
   * @return New {@link InlinedASTNode} or {@link ASTNode} if inlining was unsuccessful.
   */
  public @NotNull ASTNode inlineASTNode(Expression owner, ASTNode node) throws EvaluationException {
    if (node instanceof InlinedASTNode) return node;

    if (node.getParameters().isEmpty()) {
      if (node.getToken().getType() == Token.TokenType.VARIABLE_OR_CONSTANT) {
        if (!owner.getConfiguration().isAllowOverwriteConstants()) {
          EvaluationValue constant =
              owner.getConfiguration().getConstants().get(node.getToken().getValue());
          if (constant != null)
            return new InlinedASTNode(node.getToken(), owner.tryRoundValue(constant));
        }
      } else if (node.getToken().getType() == Token.TokenType.FUNCTION) {
        EvaluationValue function =
            node.getToken()
                .getFunctionDefinition()
                .inlineFunction(owner, node.getToken(), Collections.emptyList());
        if (function != null)
          return new InlinedASTNode(node.getToken(), owner.tryRoundValue(function));
      }
      return node;
    }

    List<ASTNode> result = new ArrayList<>();
    for (ASTNode astNode : node.getParameters()) {
      ASTNode inlineASTNode = inlineASTNode(owner, astNode);
      result.add(inlineASTNode);
    }
    if (!result.stream().allMatch(node1 -> node1 instanceof InlinedASTNode)) return node;
    List<InlinedASTNode> parameters = (List<InlinedASTNode>) (Object) result;

    switch (node.getToken().getType()) {
      case POSTFIX_OPERATOR:
      case PREFIX_OPERATOR:
      case INFIX_OPERATOR:
        EvaluationValue operator =
            node.getToken()
                .getOperatorDefinition()
                .inlineOperator(owner, node.getToken(), parameters);
        if (operator != null)
          return new InlinedASTNode(
              node.getToken(), owner.tryRoundValue(operator), parameters.toArray(ASTNode[]::new));
      case FUNCTION:
        EvaluationValue function =
            node.getToken()
                .getFunctionDefinition()
                .inlineFunction(owner, node.getToken(), parameters);
        if (function != null)
          return new InlinedASTNode(
              node.getToken(), owner.tryRoundValue(function), parameters.toArray(ASTNode[]::new));
    }
    return node;
  }
}
