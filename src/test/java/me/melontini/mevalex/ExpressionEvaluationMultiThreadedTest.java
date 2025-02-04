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
package me.melontini.mevalex;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.data.EvaluationValue;
import me.melontini.mevalex.parser.ParseException;
import org.junit.jupiter.api.Test;

class ExpressionEvaluationMultiThreadedTest {
  @Test
  void testThreadLocal() throws InterruptedException, ParseException, EvaluationException {

    AtomicInteger errorCount = new AtomicInteger();

    Expression expression = ExpressionConfiguration.defaultExpressionParser().parse("a+b");

    SecureRandom random = new SecureRandom();

    // start 100 threads
    ExecutorService es = Executors.newCachedThreadPool();
    for (int t = 0; t < 100; t++) {
      es.execute(
          () -> {
            try {
              for (int i = 0; i < 100; i++) {

                BigDecimal a = new BigDecimal(random.nextInt());
                BigDecimal b = new BigDecimal(random.nextInt());
                EvaluationValue result =
                    expression
                        .copy()
                        .evaluate(builder -> builder.parameter("a", a).parameter("b", b));

                BigDecimal sum = a.add(b);

                if (sum.compareTo(result.getNumberValue()) != 0) {
                  errorCount.getAndIncrement();
                  System.err.printf(
                      "Error adding decimals: %s + %s should be %s but is %s%n",
                      a.toPlainString(),
                      b.toPlainString(),
                      sum.toPlainString(),
                      result.getNumberValue().toPlainString());
                }
              }
            } catch (EvaluationException e) {
              System.err.printf("Exception adding decimals: %s%n", e.getMessage());
              errorCount.getAndIncrement();
            }
          });
    }
    es.shutdown();

    // normal termination, no timeout
    assertThat(es.awaitTermination(60, TimeUnit.SECONDS)).isTrue();

    assertThat(errorCount).hasValue(0);
  }
}
