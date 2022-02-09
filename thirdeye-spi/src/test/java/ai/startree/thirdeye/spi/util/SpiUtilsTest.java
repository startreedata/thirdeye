package ai.startree.thirdeye.spi.util;

import java.util.Properties;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SpiUtilsTest {

  @Test
  public void testDecodeCompactedProperties() {
    String propertiesString = "a=a;b=";
    Properties props = SpiUtils.decodeCompactedProperties(propertiesString);

    Assert.assertEquals(2, props.size());
    Assert.assertEquals("a", props.getProperty("a"));
    Assert.assertEquals("", props.getProperty("b"));
  }
}
