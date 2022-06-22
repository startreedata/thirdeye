/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.detection.alert;

import java.util.ArrayList;
import java.util.Arrays;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

public class AlertUtilsTest {

  @Test
  public void testToAddress() throws AddressException {
    Assertions.assertThat(new ArrayList<>(AlertUtils.toAddress(Arrays.asList(
        "email1@domain.com",
        "email2@domain.com"
    )))).isEqualTo(Arrays.asList(
        new InternetAddress("email1@domain.com"),
        new InternetAddress("email2@domain.com")
    ));
  }
}
