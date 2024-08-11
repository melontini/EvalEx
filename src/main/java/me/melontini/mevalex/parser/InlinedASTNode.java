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
package me.melontini.mevalex.parser;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import me.melontini.mevalex.EvaluationContext;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.data.EvaluationValue;

@Getter
@Accessors(fluent = true)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class InlinedASTNode extends ASTNode implements Solvable {

  private final EvaluationValue value;

  public InlinedASTNode(Token token, EvaluationValue value, ASTNode... parameters) {
    super(token, parameters);
    this.value = value;
  }

  public static InlinedASTNode of(Token token, EvaluationValue constant) {
    return new InlinedASTNode(token, constant, EMPTY);
  }

  public static InlinedASTNode of(Token token, EvaluationValue constant, ASTNode... nodes) {
    return new InlinedASTNode(token, constant, nodes);
  }

  static InlinedASTNode trusted(Token token, EvaluationValue constant, ASTNode... nodes) {
    return new InlinedASTNode(token, constant, nodes);
  }

  @Override
  public EvaluationValue solve(EvaluationContext context) throws EvaluationException {
    return context.expression().tryRoundValue(value());
  }
}
