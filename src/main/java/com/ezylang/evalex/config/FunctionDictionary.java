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
package com.ezylang.evalex.config;

import com.ezylang.evalex.functions.FunctionIfc;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * A default case-insensitive implementation of the function dictionary that uses a local <code>
 * Map.Entry&lt;String, FunctionIfc&gt;</code> for storage.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FunctionDictionary {

  private final Map<String, FunctionIfc> functions;

  public FunctionIfc getFunction(String functionName) {
    return functions.get(functionName);
  }

  public boolean hasFunction(String functionName) {
    return this.functions.containsKey(functionName);
  }

  public void forEach(BiConsumer<String, FunctionIfc> consumer) {
    this.functions.forEach(consumer);
  }

  public Builder toBuilder(Supplier<Map<String, FunctionIfc>> supplier) {
    Map<String, FunctionIfc> map = supplier.get();
    map.putAll(this.functions);
    return new Builder(map);
  }

  public static Builder builder(Supplier<Map<String, FunctionIfc>> supplier) {
    return new Builder(supplier.get());
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static final class Builder {
    private final Map<String, FunctionIfc> functions;

    public Builder add(String name, FunctionIfc definition) {
      this.functions.put(name, definition);
      return this;
    }

    public Builder add(Map<String, FunctionIfc> functions) {
      this.functions.putAll(functions);
      return this;
    }

    public FunctionDictionary build() {
      return new FunctionDictionary(Collections.unmodifiableMap(functions));
    }
  }
}
