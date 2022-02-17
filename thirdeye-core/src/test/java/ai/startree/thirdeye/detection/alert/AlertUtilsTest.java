/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
