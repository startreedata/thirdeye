/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.spec;

import ai.startree.thirdeye.spi.detection.AbstractSpec;
import com.google.common.collect.ImmutableMap;
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
        .fromProperties(ImmutableMap.of("a", 321, "className", "org.test.Test"), TestSpec.class);
    Assert.assertEquals(spec.getA(), 321);
    Assert.assertEquals(spec.getB(), 456.7);
    Assert.assertEquals(spec.getC(), "default");
  }

  @Test
  public void testAbstractSpecMappingNestedMap() {
    TestSpec spec = AbstractSpec.fromProperties(ImmutableMap
        .of("a", 321, "className", "org.test.Test", "configuration",
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
}

