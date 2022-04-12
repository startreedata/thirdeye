/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.entity.EntityType;
import ai.startree.thirdeye.spi.rootcause.Entity;
import ai.startree.thirdeye.spi.rootcause.util.ParsedUrn;
import java.util.Arrays;
import java.util.Collections;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EntityTypeTest {

  private final static EntityType TYPE = new EntityType("thirdeye:entity:");

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testRequiresDoubleColon() {
    new EntityType("missing:double:colon:terminator");
  }

  @Test
  public void testIsTypeString() {
    Assert.assertTrue(TYPE.isType("thirdeye:entity:abc:123:key=value"));
  }

  @Test
  public void testIsTypeStringFail() {
    Assert.assertFalse(TYPE.isType("thirdeye:notentity:abc"));
  }

  @Test
  public void testIsTypeEntity() {
    Entity e = new Entity("thirdeye:entity:abc", 1.0, Collections.emptyList());
    Assert.assertTrue(TYPE.isType(e));
  }

  @Test
  public void testIsTypeEntityFail() {
    Entity e = new Entity("thirdeye:notentity:abc", 1.0, Collections.emptyList());
    Assert.assertFalse(TYPE.isType(e));
  }

  @Test
  public void testIsTypeParsedUrn() {
    ParsedUrn p = new ParsedUrn(Arrays.asList("thirdeye", "entity", "abc"));
    Assert.assertTrue(TYPE.isType(p));
  }

  @Test
  public void testIsTypeParsedUrnFail() {
    ParsedUrn p = new ParsedUrn(Arrays.asList("thirdeye", "notentity", "abc"));
    Assert.assertFalse(TYPE.isType(p));
  }
}
