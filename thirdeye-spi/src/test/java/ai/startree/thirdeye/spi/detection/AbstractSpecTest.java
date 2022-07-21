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
package ai.startree.thirdeye.spi.detection;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AbstractSpecTest {

  @Test
  public void testAbstractSpecMappingDefaultValue() {
    TestSpec spec = AbstractSpec.fromProperties(ImmutableMap.of(), TestSpec.class);
    Assert.assertEquals(spec.getA(), 123);
    Assert.assertEquals(spec.getB(), 456.7);
    Assert.assertEquals(spec.getC(), "default");
  }

  @Test
  public void testAbstractSpecMappingIncompleteProperty() {
    TestSpec spec = AbstractSpec.fromProperties(ImmutableMap.of("a", 321), TestSpec.class);
    Assert.assertEquals(spec.getA(), 321);
    Assert.assertEquals(spec.getB(), 456.7);
    Assert.assertEquals(spec.getC(), "default");
  }

  @Test
  public void testAbstractSpecMappingExtraField() {
    TestSpec spec = AbstractSpec
        .fromProperties(ImmutableMap.of("a", 321), TestSpec.class);
    Assert.assertEquals(spec.getA(), 321);
    Assert.assertEquals(spec.getB(), 456.7);
    Assert.assertEquals(spec.getC(), "default");
  }

  @Test
  public void testAbstractSpecMappingNestedMap() {
    TestSpec spec = AbstractSpec.fromProperties(ImmutableMap
        .of("a", 321, "configuration",
            ImmutableMap.of("k1", "v1", "k2", "v2")), TestSpec.class);
    Assert.assertEquals(spec.getA(), 321);
    Assert.assertEquals(spec.getB(), 456.7);
    Assert.assertEquals(spec.getC(), "default");
    Assert.assertEquals(spec.getConfiguration(), ImmutableMap.of("k1", "v1", "k2", "v2"));
  }

  @Test
  public void testAbstractSpecMappingAmbiguityFalse() {
    TestSpec spec = AbstractSpec
        .fromProperties(ImmutableMap.of("upThreshold", 0.2, "downThreshold", 0.3), TestSpec.class);
    Assert.assertEquals(spec.getUpThreshold(), 0.2);
    Assert.assertEquals(spec.getThreshold(), 0.1);
    Assert.assertEquals(spec.getDownThreshold(), 0.3);
  }

  private static class TestSpec extends AbstractSpec {

    private int a = 123;
    private double b = 456.7;
    private String c = "default";
    private Map<String, String> configuration;
    private double threshold = 0.1;
    private double upThreshold;
    private double downThreshold;

    public Map<String, String> getConfiguration() {
      return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
      this.configuration = configuration;
    }

    public int getA() {
      return a;
    }

    public void setA(int a) {
      this.a = a;
    }

    public double getB() {
      return b;
    }

    public void setB(double b) {
      this.b = b;
    }

    public String getC() {
      return c;
    }

    public void setC(String c) {
      this.c = c;
    }

    public double getThreshold() {
      return threshold;
    }

    public void setThreshold(double threshold) {
      this.threshold = threshold;
    }

    public double getUpThreshold() {
      return upThreshold;
    }

    public void setUpThreshold(double upThreshold) {
      this.upThreshold = upThreshold;
    }

    public double getDownThreshold() {
      return downThreshold;
    }

    public void setDownThreshold(double downThreshold) {
      this.downThreshold = downThreshold;
    }
  }
}

