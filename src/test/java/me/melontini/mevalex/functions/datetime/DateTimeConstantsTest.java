/*
  Copyright 2012-2023 Udo Klimaschewski

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
package me.melontini.mevalex.functions.datetime;

import java.time.ZoneId;
import me.melontini.mevalex.BaseEvaluationTest;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.config.ExpressionConfiguration;
import me.melontini.mevalex.config.TestConfigurationProvider;
import me.melontini.mevalex.parser.ParseException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DateTimeConstantsTest extends BaseEvaluationTest {

  private static final ExpressionConfiguration DateTimeTestConfiguration =
      TestConfigurationProvider.StandardConfigurationWithAdditionalTestOperators.toBuilder()
          .zoneId(ZoneId.of("Europe/Berlin"))
          .build();

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "DT_DATE_PARSE(\"2011-12-03T10:15:30\", NULL, DT_FORMAT_ISO_DATE_TIME) |"
            + " 2011-12-03T09:15:30Z",
        "DT_DATE_PARSE(\"2011-12-03T10:15:30.123\", NULL, DT_FORMAT_ISO_DATE_TIME) |"
            + " 2011-12-03T09:15:30.123Z",
        "DT_DATE_PARSE(\"2011-12-03T10:15:30+03:30\", NULL, DT_FORMAT_ISO_DATE_TIME) |"
            + " 2011-12-03T06:45:30Z",
        "DT_DATE_PARSE(\"2011-12-03T10:15:30.456+03:30\", NULL, DT_FORMAT_ISO_DATE_TIME) |"
            + " 2011-12-03T06:45:30.456Z",
        "DT_DATE_PARSE(\"2011-12-03T10:15:30[America/New_York]\", NULL, DT_FORMAT_ISO_DATE_TIME) |"
            + " 2011-12-03T15:15:30Z",
        "DT_DATE_PARSE(\"2011-12-03T10:15:30.345[America/New_York]\", NULL,"
            + " DT_FORMAT_ISO_DATE_TIME) | 2011-12-03T15:15:30.345Z",
        "DT_DATE_PARSE(\"2011-12-03T10:15:30.345-05:00[America/New_York]\", NULL,"
            + " DT_FORMAT_ISO_DATE_TIME) | 2011-12-03T15:15:30.345Z",
        "DT_DATE_PARSE(\"2011-12-03T10:15:30-05:00[America/New_York]\", NULL,"
            + " DT_FORMAT_ISO_DATE_TIME) | 2011-12-03T15:15:30Z",
        "DT_DATE_PARSE(\"2011-12-03\", NULL, DT_FORMAT_LOCAL_DATE) | 2011-12-02T23:00:00Z",
        "DT_DATE_PARSE(\"2011-12-03\", \"Z\", DT_FORMAT_LOCAL_DATE) | 2011-12-03T00:00:00Z",
        "DT_DATE_PARSE(\"2011-12-03T17:30:25\", NULL, DT_FORMAT_LOCAL_DATE_TIME) |"
            + " 2011-12-03T16:30:25Z",
        "DT_DATE_PARSE(\"2011-12-03T17:30:25.456\", NULL, DT_FORMAT_LOCAL_DATE_TIME) |"
            + " 2011-12-03T16:30:25.456Z",
      })
  void testDateTimeParse(String expression, String expectedResult)
      throws EvaluationException, ParseException {
    assertExpressionHasExpectedResult(expression, expectedResult, DateTimeTestConfiguration);
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "DT_DATE_FORMAT(DT_DATE_PARSE(\"2011-12-03T10:15:30.345-05:00[America/New_York]\"),DT_FORMAT_ISO_DATE_TIME)"
            + " | 2011-12-03T16:15:30.345+01:00[Europe/Berlin]",
        "DT_DATE_FORMAT(DT_DATE_PARSE(\"2011-12-03T10:15:30.345-05:00[America/New_York]\"),DT_FORMAT_ISO_DATE_TIME,"
            + " \"America/New_York\") | 2011-12-03T10:15:30.345-05:00[America/New_York]",
        "DT_DATE_FORMAT(DT_DATE_PARSE(\"2011-12-03T10:15:30.345-05:00[America/New_York]\"),DT_FORMAT_LOCAL_DATE)"
            + " | 2011-12-03",
        "DT_DATE_FORMAT(DT_DATE_PARSE(\"2011-12-03T10:15:30.345-05:00[America/New_York]\"),DT_FORMAT_LOCAL_DATE_TIME)"
            + " | 2011-12-03T16:15:30.345",
      })
  void testDateTimeFormat(String expression, String expectedResult)
      throws EvaluationException, ParseException {
    assertExpressionHasExpectedResult(expression, expectedResult, DateTimeTestConfiguration);
  }
}
