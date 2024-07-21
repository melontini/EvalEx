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
import com.ezylang.evalex.config.FunctionDictionary;
import com.ezylang.evalex.config.OperatorDictionary;
import com.ezylang.evalex.functions.FunctionIfc;
import com.ezylang.evalex.operators.OperatorIfc;
import com.ezylang.evalex.parser.Token.TokenType;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * The tokenizer is responsible to parse a string and return a list of tokens. The order of tokens
 * will follow the infix expression notation, skipping any blank characters.
 */
public class Tokenizer {

  private final OperatorDictionary operatorDictionary;
  private final FunctionDictionary functionDictionary;
  private final ExpressionConfiguration configuration;

  public Tokenizer(ExpressionConfiguration configuration) {
    this.configuration = configuration;
    this.operatorDictionary = configuration.getOperatorDictionary();
    this.functionDictionary = configuration.getFunctionDictionary();
  }

  @RequiredArgsConstructor
  private static class Context {
    private final String expressionString;
    private final List<Token> tokens = new ArrayList<>();
    private int currentColumnIndex = 0;
    private int currentChar = -2;
    private int braceBalance;
    private int arrayBalance;
  }

  /**
   * Parse the given expression and return a list of tokens, representing the expression.
   *
   * @return A list of expression tokens.
   * @throws ParseException When the expression can't be parsed.
   */
  public List<Token> parse(String expressionString) throws ParseException {
    Context context = new Context(expressionString);

    Token currentToken = getNextToken(context);
    while (currentToken != null) {
      if (implicitMultiplicationPossible(currentToken, context)) {
        if (configuration.isImplicitMultiplicationAllowed()) {
          Token multiplication =
              new Token(
                  currentToken.getStartPosition(),
                  "*",
                  TokenType.INFIX_OPERATOR,
                  operatorDictionary.getInfixOperator("*"));
          context.tokens.add(multiplication);
        } else {
          throw new ParseException(currentToken, "Missing operator");
        }
      }
      validateToken(currentToken, context);
      context.tokens.add(currentToken);
      currentToken = getNextToken(context);
    }

    if (context.braceBalance > 0) {
      throw new ParseException(expressionString, "Closing brace not found");
    }

    if (context.arrayBalance > 0) {
      throw new ParseException(expressionString, "Closing array not found");
    }

    return context.tokens;
  }

  private boolean implicitMultiplicationPossible(Token currentToken, Context context) {
    Token previousToken = getPreviousToken(context);

    if (previousToken == null) {
      return false;
    }

    return ((previousToken.getType() == BRACE_CLOSE && currentToken.getType() == BRACE_OPEN)
        || (previousToken.getType() == NUMBER_LITERAL
            && currentToken.getType() == VARIABLE_OR_CONSTANT)
        || (previousToken.getType() == NUMBER_LITERAL && currentToken.getType() == BRACE_OPEN));
  }

  private void validateToken(Token currentToken, Context context) throws ParseException {

    if (currentToken.getType() == STRUCTURE_SEPARATOR && getPreviousToken(context) == null) {
      throw new ParseException(currentToken, "Misplaced structure operator");
    }

    Token previousToken = getPreviousToken(context);
    if (previousToken != null
        && previousToken.getType() == INFIX_OPERATOR
        && invalidTokenAfterInfixOperator(currentToken)) {
      throw new ParseException(currentToken, "Unexpected token after infix operator");
    }
  }

  private boolean invalidTokenAfterInfixOperator(Token token) {
    switch (token.getType()) {
      case INFIX_OPERATOR:
      case BRACE_CLOSE:
      case COMMA:
        return true;
      default:
        return false;
    }
  }

  private Token getNextToken(Context context) throws ParseException {

    // blanks are always skipped.
    skipBlanks(context);

    // end of input
    if (context.currentChar == -1) {
      return null;
    }

    // we have a token start, identify and parse it
    if (isAtStringLiteralStart(context)) {
      return parseStringLiteral(context);
    } else if (context.currentChar == '(') {
      return parseBraceOpen(context);
    } else if (context.currentChar == ')') {
      return parseBraceClose(context);
    } else if (context.currentChar == '[' && configuration.isArraysAllowed()) {
      return parseArrayOpen(context);
    } else if (context.currentChar == ']' && configuration.isArraysAllowed()) {
      return parseArrayClose(context);
    } else if (context.currentChar == '.'
        && !isNextCharNumberChar(context)
        && configuration.isStructuresAllowed()) {
      return parseStructureSeparator(context);
    } else if (context.currentChar == ',') {
      Token token = new Token(context.currentColumnIndex, ",", TokenType.COMMA);
      consumeChar(context);
      return token;
    } else if (isAtIdentifierStart(context)) {
      return parseIdentifier(context);
    } else if (isAtNumberStart(context)) {
      return parseNumberLiteral(context);
    } else {
      return parseOperator(context);
    }
  }

  private Token parseStructureSeparator(Context context) throws ParseException {
    Token token = new Token(context.currentColumnIndex, ".", TokenType.STRUCTURE_SEPARATOR);
    if (arrayOpenOrStructureSeparatorNotAllowed(context)) {
      throw new ParseException(token, "Structure separator not allowed here");
    }
    consumeChar(context);
    return token;
  }

  private Token parseArrayClose(Context context) throws ParseException {
    Token token = new Token(context.currentColumnIndex, "]", TokenType.ARRAY_CLOSE);
    if (!arrayCloseAllowed(context)) {
      throw new ParseException(token, "Array close not allowed here");
    }
    consumeChar(context);
    context.arrayBalance--;
    if (context.arrayBalance < 0) {
      throw new ParseException(token, "Unexpected closing array");
    }
    return token;
  }

  private Token parseArrayOpen(Context context) throws ParseException {
    Token token = new Token(context.currentColumnIndex, "[", TokenType.ARRAY_OPEN);
    if (arrayOpenOrStructureSeparatorNotAllowed(context)) {
      throw new ParseException(token, "Array open not allowed here");
    }
    consumeChar(context);
    context.arrayBalance++;
    return token;
  }

  private Token parseBraceClose(Context context) throws ParseException {
    Token token = new Token(context.currentColumnIndex, ")", TokenType.BRACE_CLOSE);
    consumeChar(context);
    context.braceBalance--;
    if (context.braceBalance < 0) {
      throw new ParseException(token, "Unexpected closing brace");
    }
    return token;
  }

  private Token parseBraceOpen(Context context) {
    Token token = new Token(context.currentColumnIndex, "(", BRACE_OPEN);
    consumeChar(context);
    context.braceBalance++;
    return token;
  }

  private Token getPreviousToken(Context context) {
    return context.tokens.isEmpty() ? null : context.tokens.get(context.tokens.size() - 1);
  }

  private Token parseOperator(Context context) throws ParseException {
    int tokenStartIndex = context.currentColumnIndex;
    StringBuilder tokenValue = new StringBuilder();
    while (true) {
      tokenValue.append((char) context.currentChar);
      String tokenString = tokenValue.toString();
      String possibleNextOperator = tokenString + (char) peekNextChar(context);
      boolean possibleNextOperatorFound =
          (prefixOperatorAllowed(context)
                  && operatorDictionary.hasPrefixOperator(possibleNextOperator))
              || (postfixOperatorAllowed(context)
                  && operatorDictionary.hasPostfixOperator(possibleNextOperator))
              || (infixOperatorAllowed(context)
                  && operatorDictionary.hasInfixOperator(possibleNextOperator));
      consumeChar(context);
      if (!possibleNextOperatorFound) {
        break;
      }
    }
    String tokenString = tokenValue.toString();
    if (prefixOperatorAllowed(context) && operatorDictionary.hasPrefixOperator(tokenString)) {
      OperatorIfc operator = operatorDictionary.getPrefixOperator(tokenString);
      return new Token(tokenStartIndex, tokenString, TokenType.PREFIX_OPERATOR, operator);
    } else if (postfixOperatorAllowed(context)
        && operatorDictionary.hasPostfixOperator(tokenString)) {
      OperatorIfc operator = operatorDictionary.getPostfixOperator(tokenString);
      return new Token(tokenStartIndex, tokenString, TokenType.POSTFIX_OPERATOR, operator);
    } else if (operatorDictionary.hasInfixOperator(tokenString)) {
      OperatorIfc operator = operatorDictionary.getInfixOperator(tokenString);
      return new Token(tokenStartIndex, tokenString, TokenType.INFIX_OPERATOR, operator);
    } else if (tokenString.equals(".") && configuration.isStructuresAllowed()) {
      return new Token(tokenStartIndex, tokenString, STRUCTURE_SEPARATOR);
    }
    throw new ParseException(
        tokenStartIndex,
        tokenStartIndex + tokenString.length() - 1,
        tokenString,
        "Undefined operator '" + tokenString + "'");
  }

  private boolean arrayOpenOrStructureSeparatorNotAllowed(Context context) {
    Token previousToken = getPreviousToken(context);

    if (previousToken == null) {
      return true;
    }

    switch (previousToken.getType()) {
      case BRACE_CLOSE:
      case VARIABLE_OR_CONSTANT:
      case ARRAY_CLOSE:
      case STRING_LITERAL:
        return false;
      default:
        return true;
    }
  }

  private boolean arrayCloseAllowed(Context context) {
    Token previousToken = getPreviousToken(context);

    if (previousToken == null) {
      return false;
    }

    switch (previousToken.getType()) {
      case BRACE_OPEN:
      case INFIX_OPERATOR:
      case PREFIX_OPERATOR:
      case FUNCTION:
      case COMMA:
      case ARRAY_OPEN:
        return false;
      default:
        return true;
    }
  }

  private boolean prefixOperatorAllowed(Context context) {
    Token previousToken = getPreviousToken(context);

    if (previousToken == null) {
      return true;
    }

    switch (previousToken.getType()) {
      case BRACE_OPEN:
      case INFIX_OPERATOR:
      case COMMA:
      case PREFIX_OPERATOR:
      case ARRAY_OPEN:
        return true;
      default:
        return false;
    }
  }

  private boolean postfixOperatorAllowed(Context context) {
    Token previousToken = getPreviousToken(context);

    if (previousToken == null) {
      return false;
    }

    switch (previousToken.getType()) {
      case BRACE_CLOSE:
      case NUMBER_LITERAL:
      case VARIABLE_OR_CONSTANT:
      case STRING_LITERAL:
        return true;
      default:
        return false;
    }
  }

  private boolean infixOperatorAllowed(Context context) {
    Token previousToken = getPreviousToken(context);

    if (previousToken == null) {
      return false;
    }

    switch (previousToken.getType()) {
      case BRACE_CLOSE:
      case VARIABLE_OR_CONSTANT:
      case STRING_LITERAL:
      case POSTFIX_OPERATOR:
      case NUMBER_LITERAL:
      case ARRAY_CLOSE:
        return true;
      default:
        return false;
    }
  }

  private Token parseNumberLiteral(Context context) throws ParseException {
    int nextChar = peekNextChar(context);
    if (context.currentChar == '0' && (nextChar == 'x' || nextChar == 'X')) {
      return parseHexNumberLiteral(context);
    } else {
      return parseDecimalNumberLiteral(context);
    }
  }

  private Token parseDecimalNumberLiteral(Context context) throws ParseException {
    int tokenStartIndex = context.currentColumnIndex;
    StringBuilder tokenValue = new StringBuilder();

    int lastChar = -1;
    boolean scientificNotation = false;
    boolean dotEncountered = false;
    while (context.currentChar != -1 && isAtNumberChar(context)) {
      if (context.currentChar == '.' && dotEncountered) {
        tokenValue.append((char) context.currentChar);
        throw new ParseException(
            new Token(tokenStartIndex, tokenValue.toString(), TokenType.NUMBER_LITERAL),
            "Number contains more than one decimal point");
      }
      if (context.currentChar == '.') {
        dotEncountered = true;
      }
      if (context.currentChar == 'e' || context.currentChar == 'E') {
        scientificNotation = true;
      }
      tokenValue.append((char) context.currentChar);
      lastChar = context.currentChar;
      consumeChar(context);
    }
    // illegal scientific format literal
    if (scientificNotation
        && (lastChar == 'e'
            || lastChar == 'E'
            || lastChar == '+'
            || lastChar == '-'
            || lastChar == '.')) {
      throw new ParseException(
          new Token(tokenStartIndex, tokenValue.toString(), TokenType.NUMBER_LITERAL),
          "Illegal scientific format");
    }
    return new Token(tokenStartIndex, tokenValue.toString(), TokenType.NUMBER_LITERAL);
  }

  private Token parseHexNumberLiteral(Context context) {
    int tokenStartIndex = context.currentColumnIndex;
    StringBuilder tokenValue = new StringBuilder();

    // hexadecimal number, consume "0x"
    tokenValue.append((char) context.currentChar);
    consumeChar(context);
    do {
      tokenValue.append((char) context.currentChar);
      consumeChar(context);
    } while (context.currentChar != -1 && isAtHexChar(context));
    return new Token(tokenStartIndex, tokenValue.toString(), TokenType.NUMBER_LITERAL);
  }

  private Token parseIdentifier(Context context) throws ParseException {
    int tokenStartIndex = context.currentColumnIndex;
    StringBuilder tokenValue = new StringBuilder();
    while (context.currentChar != -1 && isAtIdentifierChar(context)) {
      tokenValue.append((char) context.currentChar);
      consumeChar(context);
    }
    String tokenName = tokenValue.toString();

    if (prefixOperatorAllowed(context) && operatorDictionary.hasPrefixOperator(tokenName)) {
      return new Token(
          tokenStartIndex,
          tokenName,
          TokenType.PREFIX_OPERATOR,
          operatorDictionary.getPrefixOperator(tokenName));
    } else if (postfixOperatorAllowed(context)
        && operatorDictionary.hasPostfixOperator(tokenName)) {
      return new Token(
          tokenStartIndex,
          tokenName,
          TokenType.POSTFIX_OPERATOR,
          operatorDictionary.getPostfixOperator(tokenName));
    } else if (operatorDictionary.hasInfixOperator(tokenName)) {
      return new Token(
          tokenStartIndex,
          tokenName,
          TokenType.INFIX_OPERATOR,
          operatorDictionary.getInfixOperator(tokenName));
    }

    skipBlanks(context);
    if (context.currentChar == '(') {
      if (!functionDictionary.hasFunction(tokenName)) {
        throw new ParseException(
            tokenStartIndex,
            context.currentColumnIndex,
            tokenName,
            "Undefined function '" + tokenName + "'");
      }
      FunctionIfc function = functionDictionary.getFunction(tokenName);
      return new Token(tokenStartIndex, tokenName, TokenType.FUNCTION, function);
    } else {
      return new Token(tokenStartIndex, tokenName, TokenType.VARIABLE_OR_CONSTANT);
    }
  }

  Token parseStringLiteral(Context context) throws ParseException {
    int startChar = context.currentChar;
    int tokenStartIndex = context.currentColumnIndex;
    StringBuilder tokenValue = new StringBuilder();
    // skip starting quote
    consumeChar(context);
    boolean inQuote = true;
    while (inQuote && context.currentChar != -1) {
      if (context.currentChar == '\\') {
        consumeChar(context);
        tokenValue.append(escapeCharacter(context.currentChar, context));
      } else if (context.currentChar == startChar) {
        inQuote = false;
      } else {
        tokenValue.append((char) context.currentChar);
      }
      consumeChar(context);
    }
    if (inQuote) {
      throw new ParseException(
          tokenStartIndex,
          context.currentColumnIndex,
          tokenValue.toString(),
          "Closing quote not found");
    }
    return new Token(tokenStartIndex, tokenValue.toString(), TokenType.STRING_LITERAL);
  }

  private char escapeCharacter(int character, Context context) throws ParseException {
    switch (character) {
      case '\'':
        return '\'';
      case '"':
        return '"';
      case '\\':
        return '\\';
      case 'n':
        return '\n';
      case 'r':
        return '\r';
      case 't':
        return '\t';
      case 'b':
        return '\b';
      case 'f':
        return '\f';
      default:
        throw new ParseException(
            context.currentColumnIndex, 1, "\\" + (char) character, "Unknown escape character");
    }
  }

  private boolean isAtNumberStart(Context context) {
    if (Character.isDigit(context.currentChar)) {
      return true;
    }
    return context.currentChar == '.' && Character.isDigit(peekNextChar(context));
  }

  private boolean isAtNumberChar(Context context) {
    int previousChar = peekPreviousChar(context);

    if ((previousChar == 'e' || previousChar == 'E') && context.currentChar != '.') {
      return Character.isDigit(context.currentChar)
          || context.currentChar == '+'
          || context.currentChar == '-';
    }

    if (previousChar == '.' && context.currentChar != '.') {
      return Character.isDigit(context.currentChar)
          || context.currentChar == 'e'
          || context.currentChar == 'E';
    }

    return Character.isDigit(context.currentChar)
        || context.currentChar == '.'
        || context.currentChar == 'e'
        || context.currentChar == 'E';
  }

  private boolean isNextCharNumberChar(Context context) {
    if (peekNextChar(context) == -1) {
      return false;
    }
    consumeChar(context);
    boolean isAtNumber = isAtNumberChar(context);
    context.currentColumnIndex--;
    context.currentChar = context.expressionString.charAt(context.currentColumnIndex - 1);
    return isAtNumber;
  }

  private boolean isAtHexChar(Context context) {
    switch (context.currentChar) {
      case '0':
      case '1':
      case '2':
      case '3':
      case '4':
      case '5':
      case '6':
      case '7':
      case '8':
      case '9':
      case 'a':
      case 'b':
      case 'c':
      case 'd':
      case 'e':
      case 'f':
      case 'A':
      case 'B':
      case 'C':
      case 'D':
      case 'E':
      case 'F':
        return true;
      default:
        return false;
    }
  }

  private boolean isAtIdentifierStart(Context context) {
    return Character.isLetter(context.currentChar) || context.currentChar == '_';
  }

  private boolean isAtIdentifierChar(Context context) {
    return Character.isLetter(context.currentChar)
        || Character.isDigit(context.currentChar)
        || context.currentChar == '_';
  }

  private boolean isAtStringLiteralStart(Context context) {
    return context.currentChar == '"'
        || context.currentChar == '\'' && configuration.isSingleQuoteStringLiteralsAllowed();
  }

  private void skipBlanks(Context context) {
    if (context.currentChar == -2) {
      // consume first character of expression
      consumeChar(context);
    }
    while (context.currentChar != -1 && Character.isWhitespace(context.currentChar)) {
      consumeChar(context);
    }
  }

  private int peekNextChar(Context context) {
    return context.currentColumnIndex == context.expressionString.length()
        ? -1
        : context.expressionString.charAt(context.currentColumnIndex);
  }

  private int peekPreviousChar(Context context) {
    return context.currentColumnIndex == 1
        ? -1
        : context.expressionString.charAt(context.currentColumnIndex - 2);
  }

  private void consumeChar(Context context) {
    if (context.currentColumnIndex == context.expressionString.length()) {
      context.currentChar = -1;
    } else {
      context.currentChar = context.expressionString.charAt(context.currentColumnIndex++);
    }
  }
}
