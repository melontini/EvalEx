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
package me.melontini.mevalex;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.parser.ParseException;
import org.junit.jupiter.api.Test;

class ExpressionEvaluatorCombinedTest extends BaseExpressionEvaluatorTest {

  @Test
  void testOrderPositionExample() throws ParseException, EvaluationException {
    Map<String, Object> order = new HashMap<>();
    order.put("id", 12345);
    order.put("name", "Mary");
    Map<String, Object> position = new HashMap<>();
    position.put("article", 3114);
    position.put("amount", 3);
    position.put("price", new BigDecimal("14.95"));
    order.put("positions", List.of(position));

    Expression expression =
        ExpressionConfiguration.defaultExpressionParser()
            .parse("order.positions[x].amount * order.positions[x].price");

    assertThat(
            expression
                .evaluate(builder -> builder.parameter("order", order).parameter("x", 0))
                .getStringValue())
        .isEqualTo("44.85");
  }
}
