package org.apache.pinot.thirdeye.spi.datalayer.util;

import java.util.Properties;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ThirdEyeSpiUtilsTest {

  @Test
  public void testDecodeCompactedProperties() {
    String propertiesString = "a=a;b=";
    Properties props = ThirdEyeSpiUtils.decodeCompactedProperties(propertiesString);

    Assert.assertEquals(2, props.size());
    Assert.assertEquals("a", props.getProperty("a"));
    Assert.assertEquals("", props.getProperty("b"));
  }
}
