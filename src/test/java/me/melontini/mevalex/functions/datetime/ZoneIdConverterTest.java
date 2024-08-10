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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.ZoneId;
import me.melontini.mevalex.EvaluationException;
import me.melontini.mevalex.parser.Token;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ZoneIdConverterTest {

  @Test
  void testConversionOk() throws Exception {
    Token testToken = new Token(1, "Europe/Berlin", Token.TokenType.STRING_LITERAL);

    Assertions.assertThat(ZoneIdConverter.convert(testToken, testToken.getValue()))
        .isEqualTo(ZoneId.of(testToken.getValue()));
  }

  @Test
  void testConversionFails() {
    Token testToken = new Token(1, "Mars/Olympus_Mons", Token.TokenType.STRING_LITERAL);

    assertThatThrownBy(() -> ZoneIdConverter.convert(testToken, testToken.getValue()))
        .isInstanceOf(EvaluationException.class)
        .hasMessage(
            "Unable to convert zone string 'Mars/Olympus_Mons' to a zone ID: Unknown time-zone ID:"
                + " Mars/Olympus_Mons");
  }
}
