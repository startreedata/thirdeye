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
package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Entity;
import ai.startree.thirdeye.rootcause.entity.EntityType;
import ai.startree.thirdeye.util.ParsedUrn;
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
