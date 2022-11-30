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

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.testng.annotations.Test;

public class AbstractSpecTest {

  @Test
  public void testAbstractSpecMappingDefaultValue() {
    TestSpec spec = AbstractSpec.fromProperties(ImmutableMap.of(), TestSpec.class);
    assertThat(spec.getA()).isEqualTo(123);
    assertThat(spec.getB()).isEqualTo(456.7);
    assertThat(spec.getC()).isEqualTo("default");
  }

  @Test
  public void testAbstractSpecMappingIncompleteProperty() {
    TestSpec spec = AbstractSpec.fromProperties(ImmutableMap.of("a", 321), TestSpec.class);
    assertThat(spec.getA()).isEqualTo(321);
    assertThat(spec.getB()).isEqualTo(456.7);
    assertThat(spec.getC()).isEqualTo("default");
  }

  @Test
  public void testAbstractSpecMappingExtraField() {
    TestSpec spec = AbstractSpec
        .fromProperties(ImmutableMap.of("a", 321), TestSpec.class);
    assertThat(spec.getA()).isEqualTo(321);
    assertThat(spec.getB()).isEqualTo(456.7);
    assertThat(spec.getC()).isEqualTo("default");
  }

  @Test
  public void testAbstractSpecMappingNestedMap() {
    TestSpec spec = AbstractSpec.fromProperties(ImmutableMap
        .of("a", 321, "configuration",
            ImmutableMap.of("k1", "v1", "k2", "v2")), TestSpec.class);
    assertThat(spec.getA()).isEqualTo(321);
    assertThat(spec.getB()).isEqualTo(456.7);
    assertThat(spec.getC()).isEqualTo("default");
    assertThat(spec.getConfiguration()).isEqualTo(ImmutableMap.of("k1", "v1", "k2", "v2"));
  }

  @Test
  public void testAbstractSpecMappingAmbiguityFalse() {
    TestSpec spec = AbstractSpec
        .fromProperties(ImmutableMap.of("upThreshold", 0.2, "downThreshold", 0.3), TestSpec.class);
    assertThat(spec.getUpThreshold()).isEqualTo(0.2);
    assertThat(spec.getThreshold()).isEqualTo(0.1);
    assertThat(spec.getDownThreshold()).isEqualTo(0.3);
  }

  // does not ignore unknown with @JsonIgnoreProperties(ignoreUnknown = true) - for testing
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

