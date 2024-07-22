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
package com.ezylang.evalex.data.types;

import com.ezylang.evalex.data.EvaluationValue;
import com.ezylang.evalex.parser.ASTNode;
import lombok.*;

@ToString()
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionNodeValue extends EvaluationValue {

  private final ASTNode value;

  public static ExpressionNodeValue of(@NonNull ASTNode node) {
    return new ExpressionNodeValue(node);
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public boolean isExpressionNode() {
    return true;
  }

  @Override
  public ASTNode getExpressionNode() {
    return value;
  }
}
