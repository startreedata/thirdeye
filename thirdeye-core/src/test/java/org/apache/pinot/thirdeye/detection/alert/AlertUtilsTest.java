package org.apache.pinot.thirdeye.detection.alert;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.testng.annotations.Test;

public class AlertUtilsTest {

  @Test
  public void testToAddress() throws AddressException {
    assertThat(new ArrayList<>(AlertUtils.toAddress(Arrays.asList(
        "email1@domain.com",
        "email2@domain.com"
    )))).isEqualTo(Arrays.asList(
        new InternetAddress("email1@domain.com"),
        new InternetAddress("email2@domain.com")
    ));
  }
}
