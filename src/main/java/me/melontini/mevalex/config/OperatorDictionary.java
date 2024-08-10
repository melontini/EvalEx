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
package me.melontini.mevalex.config;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import me.melontini.mevalex.operators.OperatorIfc;

/**
 * A default case-insensitive implementation of the operator dictionary that uses a local <code>
 * Map.Entry&lt;String,OperatorIfc&gt;</code> for storage.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class OperatorDictionary {

  private final Map<String, OperatorIfc> prefixOperators;
  private final Map<String, OperatorIfc> postfixOperators;
  private final Map<String, OperatorIfc> infixOperators;

  public OperatorIfc getPrefixOperator(String operatorString) {
    return prefixOperators.get(operatorString);
  }

  public boolean hasPrefixOperator(String operator) {
    return prefixOperators.containsKey(operator);
  }

  public OperatorIfc getPostfixOperator(String operatorString) {
    return postfixOperators.get(operatorString);
  }

  public boolean hasPostfixOperator(String operator) {
    return postfixOperators.containsKey(operator);
  }

  public OperatorIfc getInfixOperator(String operatorString) {
    return infixOperators.get(operatorString);
  }

  public boolean hasInfixOperator(String operator) {
    return infixOperators.containsKey(operator);
  }

  public void forEach(BiConsumer<String, OperatorIfc> consumer) {
    this.prefixOperators.forEach(consumer);
    this.postfixOperators.forEach(consumer);
    this.infixOperators.forEach(consumer);
  }

  public Builder toBuilder(Supplier<Map<String, OperatorIfc>> supplier) {
    Map<String, OperatorIfc> prefixOperators = supplier.get();
    prefixOperators.putAll(this.prefixOperators);
    Map<String, OperatorIfc> postfixOperators = supplier.get();
    postfixOperators.putAll(this.postfixOperators);
    Map<String, OperatorIfc> infixOperators = supplier.get();
    infixOperators.putAll(this.infixOperators);
    return new Builder(prefixOperators, postfixOperators, infixOperators);
  }

  public static Builder builder(Supplier<Map<String, OperatorIfc>> supplier) {
    return new Builder(supplier.get(), supplier.get(), supplier.get());
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Builder {
    private final Map<String, OperatorIfc> prefixOperators;
    private final Map<String, OperatorIfc> postfixOperators;
    private final Map<String, OperatorIfc> infixOperators;

    public Builder prefix(String string, OperatorIfc definition) {
      if (!definition.isPrefix())
        throw new IllegalStateException(
            String.format("Non-prefix operator passed to Builder#prefix! '%s'", string));
      this.prefixOperators.put(string, definition);
      return this;
    }

    public Builder postfix(String string, OperatorIfc definition) {
      if (!definition.isPostfix())
        throw new IllegalStateException(
            String.format("Non-postfix operator passed to Builder#postfix! '%s'", string));
      this.postfixOperators.put(string, definition);
      return this;
    }

    public Builder infix(String string, OperatorIfc definition) {
      if (!definition.isInfix())
        throw new IllegalStateException(
            String.format("Non-infix operator passed to Builder#infix! '%s'", string));
      this.infixOperators.put(string, definition);
      return this;
    }

    public OperatorDictionary build() {
      return new OperatorDictionary(
          Collections.unmodifiableMap(this.prefixOperators),
          Collections.unmodifiableMap(this.postfixOperators),
          Collections.unmodifiableMap(this.infixOperators));
    }
  }
}
