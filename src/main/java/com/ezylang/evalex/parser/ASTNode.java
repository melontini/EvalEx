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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Expressions are parsed into an abstract syntax tree (AST). The tree has one root node and each
 * node has zero or more children (parameters), depending on the operation. A leaf node is a
 * numerical or string constant that has no more children (parameters). Other nodes define
 * operators, functions and special operations like array index and structure separation.
 *
 * <p>The tree is evaluated from bottom (leafs) to top, in a recursive way, until the root node is
 * evaluated, which then holds the result of the complete expression.
 *
 * <p>To be able to visualize the tree, a <code>toJSON</code> method is provided. The produced JSON
 * string can be used to visualize the tree. OE.g. with this online tool:
 *
 * <p><a href="https://vanya.jp.net/vtree/">Online JSON to Tree Diagram Converter</a>
 */
@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class ASTNode {

  /** The children od the tree. */
  List<ASTNode> parameters;

  /** The token associated with this tree node. */
  Token token;

  protected ASTNode(Token token, List<ASTNode> parameters) {
    this.token = token;
    this.parameters = parameters;
  }

  public static ASTNode of(Token token) {
    return new ASTNode(token, Collections.emptyList());
  }

  public static ASTNode of(Token token, List<ASTNode> nodes) {
    return new ASTNode(token, List.copyOf(nodes));
  }

  static ASTNode trusted(Token token, List<ASTNode> nodes) {
    return new ASTNode(token, Collections.unmodifiableList(nodes));
  }

  /**
   * Produces a JSON string representation of this node ad all its children.
   *
   * @return A JSON string of the tree structure starting at this node.
   */
  public String toJSON() {
    if (parameters.isEmpty()) {
      return String.format(
          "{" + "\"type\":\"%s\",\"value\":\"%s\"}", token.getType(), token.getValue());
    } else {
      String childrenJson =
          parameters.stream().map(ASTNode::toJSON).collect(Collectors.joining(","));
      return String.format(
          "{" + "\"type\":\"%s\",\"value\":\"%s\",\"children\":[%s]}",
          token.getType(), token.getValue(), childrenJson);
    }
  }
}
