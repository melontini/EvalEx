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
package com.ezylang.evalex.parser;

import static com.ezylang.evalex.parser.Token.TokenType.*;

import com.ezylang.evalex.config.ExpressionConfiguration;
import com.ezylang.evalex.data.types.NumberValue;
import com.ezylang.evalex.data.types.StringValue;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.parser.Token.TokenType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * The shunting yard algorithm can be used to convert a mathematical expression from an infix
 * notation into either a postfix notation (RPN, reverse polish notation), or into an abstract
 * syntax tree (AST).
 *
 * <p>Here it is used to parse and convert a list of already parsed expression tokens into an AST.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Shunting_yard_algorithm">Shunting yard algorithm</a>
 * @see <a href="https://en.wikipedia.org/wiki/Abstract_syntax_tree">Abstract syntax tree</a>
 */
public class ShuntingYardConverter {

  private final ExpressionConfiguration configuration;

  public ShuntingYardConverter(ExpressionConfiguration configuration) {
    this.configuration = configuration;
  }

  @RequiredArgsConstructor
  private static class Context {
    private final Deque<Token> operatorStack = new ArrayDeque<>();
    private final Deque<ASTNode> operandStack = new ArrayDeque<>();
  }

  public ASTNode toAbstractSyntaxTree(List<Token> expressionTokens, String originalExpression)
      throws ParseException {
    Context context = new Context();
    Token previousToken = null;
    for (Token currentToken : expressionTokens) {
      switch (currentToken.getType()) {
        case VARIABLE_OR_CONSTANT -> context.operandStack.push(new ASTNode(currentToken));
        case NUMBER_LITERAL -> context.operandStack.push(
            new InlinedASTNode(
                currentToken,
                NumberValue.ofString(currentToken.getValue(), configuration.getMathContext())));
        case STRING_LITERAL -> context.operandStack.push(
            new InlinedASTNode(currentToken, StringValue.of(currentToken.getValue())));
        case FUNCTION -> context.operatorStack.push(currentToken);
        case COMMA -> processOperatorsFromStackUntilTokenType(BRACE_OPEN, context);
        case INFIX_OPERATOR, PREFIX_OPERATOR, POSTFIX_OPERATOR -> processOperator(
            currentToken, context);
        case BRACE_OPEN -> processBraceOpen(previousToken, currentToken, context);
        case BRACE_CLOSE -> processBraceClose(context);
        case ARRAY_OPEN -> processArrayOpen(currentToken, context);
        case ARRAY_CLOSE -> processArrayClose(context);
        case STRUCTURE_SEPARATOR -> processStructureSeparator(currentToken, context);
        default -> throw new ParseException(
            currentToken, "Unexpected token of type '" + currentToken.getType() + "'");
      }
      previousToken = currentToken;
    }

    while (!context.operatorStack.isEmpty()) {
      Token token = context.operatorStack.pop();
      createOperatorNode(token, context);
    }

    if (context.operandStack.isEmpty()) {
      throw new ParseException(originalExpression, "Empty expression");
    }

    if (context.operandStack.size() > 1) {
      throw new ParseException(originalExpression, "Too many operands");
    }

    return context.operandStack.pop();
  }

  private void processStructureSeparator(Token currentToken, Context context)
      throws ParseException {
    Token nextToken = context.operatorStack.isEmpty() ? null : context.operatorStack.peek();
    while (nextToken != null && nextToken.getType() == STRUCTURE_SEPARATOR) {
      Token token = context.operatorStack.pop();
      createOperatorNode(token, context);
      nextToken = context.operatorStack.peek();
    }
    context.operatorStack.push(currentToken);
  }

  private void processBraceOpen(Token previousToken, Token currentToken, Context context) {
    if (previousToken != null && previousToken.getType() == FUNCTION) {
      // start of parameter list, marker for variable number of arguments
      Token paramStart =
          new Token(
              currentToken.getStartPosition(),
              currentToken.getValue(),
              TokenType.FUNCTION_PARAM_START);
      context.operandStack.push(new ASTNode(paramStart));
    }
    context.operatorStack.push(currentToken);
  }

  private void processBraceClose(Context context) throws ParseException {
    processOperatorsFromStackUntilTokenType(BRACE_OPEN, context);
    context.operatorStack.pop(); // throw away the marker
    if (!context.operatorStack.isEmpty() && context.operatorStack.peek().getType() == FUNCTION) {
      Token functionToken = context.operatorStack.pop();
      ArrayList<ASTNode> parameters = new ArrayList<>();
      while (true) {
        // add all parameters in reverse order from stack to the parameter array
        ASTNode node = context.operandStack.pop();
        if (node.getToken().getType() == TokenType.FUNCTION_PARAM_START) {
          break;
        }
        parameters.add(0, node);
      }
      validateFunctionParameters(functionToken, parameters);
      context.operandStack.push(new ASTNode(functionToken, parameters.toArray(new ASTNode[0])));
    }
  }

  private void validateFunctionParameters(Token functionToken, ArrayList<ASTNode> parameters)
      throws ParseException {
    FunctionIfc function = functionToken.getFunctionDefinition();
    if (parameters.size() < function.getCountOfNonVarArgParameters()) {
      throw new ParseException(functionToken, "Not enough parameters for function");
    }
    if (!function.hasVarArgs()
        && parameters.size() > function.getFunctionParameterDefinitions().size()) {
      throw new ParseException(functionToken, "Too many parameters for function");
    }
  }

  /**
   * Array index is treated like a function with two parameters. First parameter is the array (name
   * or evaluation result). Second parameter is the array index.
   *
   * @param currentToken The current ARRAY_OPEN ("[") token.
   */
  private void processArrayOpen(Token currentToken, Context context) throws ParseException {
    Token nextToken = context.operatorStack.isEmpty() ? null : context.operatorStack.peek();
    while (nextToken != null && (nextToken.getType() == STRUCTURE_SEPARATOR)) {
      Token token = context.operatorStack.pop();
      createOperatorNode(token, context);
      nextToken = context.operatorStack.isEmpty() ? null : context.operatorStack.peek();
    }
    // create ARRAY_INDEX operator (just like a function name) and push it to the operator stack
    Token arrayIndex =
        new Token(currentToken.getStartPosition(), currentToken.getValue(), ARRAY_INDEX);
    context.operatorStack.push(arrayIndex);

    // push the ARRAY_OPEN to the operators, too (to later match the ARRAY_CLOSE)
    context.operatorStack.push(currentToken);
  }

  /**
   * Follows the logic for a function, but with two fixed parameters.
   *
   * @throws ParseException If there were problems while processing the stacks.
   */
  private void processArrayClose(Context context) throws ParseException {
    processOperatorsFromStackUntilTokenType(ARRAY_OPEN, context);
    context.operatorStack.pop(); // throw away the marker
    Token arrayToken = context.operatorStack.pop();
    ArrayList<ASTNode> operands = new ArrayList<>();

    // second parameter of the "ARRAY_INDEX" function is the index (first on stack)
    ASTNode index = context.operandStack.pop();
    operands.add(0, index);

    // first parameter of the "ARRAY_INDEX" function is the array (name or evaluation result)
    // (second on stack)
    ASTNode array = context.operandStack.pop();
    operands.add(0, array);

    context.operandStack.push(new ASTNode(arrayToken, operands.toArray(new ASTNode[0])));
  }

  private void processOperatorsFromStackUntilTokenType(TokenType untilTokenType, Context context)
      throws ParseException {
    while (!context.operatorStack.isEmpty()
        && context.operatorStack.peek().getType() != untilTokenType) {
      Token token = context.operatorStack.pop();
      createOperatorNode(token, context);
    }
  }

  private void createOperatorNode(Token token, Context context) throws ParseException {
    if (context.operandStack.isEmpty()) {
      throw new ParseException(token, "Missing operand for operator");
    }

    ASTNode operand1 = context.operandStack.pop();

    if (token.getType() == TokenType.PREFIX_OPERATOR
        || token.getType() == TokenType.POSTFIX_OPERATOR) {
      context.operandStack.push(new ASTNode(token, operand1));
    } else {
      if (context.operandStack.isEmpty()) {
        throw new ParseException(token, "Missing second operand for operator");
      }
      ASTNode operand2 = context.operandStack.pop();
      context.operandStack.push(new ASTNode(token, operand2, operand1));
    }
  }

  private void processOperator(Token currentToken, Context context) throws ParseException {
    Token nextToken = context.operatorStack.isEmpty() ? null : context.operatorStack.peek();
    while (isOperator(nextToken)
        && isNextOperatorOfHigherPrecedence(
            currentToken.getOperatorDefinition(), nextToken.getOperatorDefinition())) {
      Token token = context.operatorStack.pop();
      createOperatorNode(token, context);
      nextToken = context.operatorStack.isEmpty() ? null : context.operatorStack.peek();
    }
    context.operatorStack.push(currentToken);
  }

  private boolean isNextOperatorOfHigherPrecedence(
      OperatorIfc currentOperator, OperatorIfc nextOperator) {
    // structure operator (null) has always a higher precedence than other operators
    if (nextOperator == null) {
      return true;
    }

    if (currentOperator.isLeftAssociative()) {
      return currentOperator.getPrecedence(configuration)
          <= nextOperator.getPrecedence(configuration);
    } else {
      return currentOperator.getPrecedence(configuration)
          < nextOperator.getPrecedence(configuration);
    }
  }

  private boolean isOperator(Token token) {
    if (token == null) {
      return false;
    }
    TokenType tokenType = token.getType();
    return switch (tokenType) {
      case INFIX_OPERATOR, PREFIX_OPERATOR, POSTFIX_OPERATOR, STRUCTURE_SEPARATOR -> true;
      default -> false;
    };
  }
}
