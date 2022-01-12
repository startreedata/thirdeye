/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.cube.data.dbrow;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections4.ListUtils;
import org.testng.annotations.Test;

public class DimensionsTest {

  @Test
  public void testDefaultCreation() {
    Dimensions dimensions = new Dimensions();
    assertThat(dimensions.size()).isEqualTo(0);
  }

  @Test
  public void testListCreation() {
    List<String> names = List.of("a", "b");
    Dimensions dimensions = new Dimensions(names);
    assertThat(dimensions.size()).isEqualTo(2);
    assertThat(dimensions.names()).isEqualTo(names);
    assertThat(dimensions.get(0)).isEqualTo("a");
    assertThat(dimensions.get(1)).isEqualTo("b");
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void testNullListCreation() {
    new Dimensions(null);
  }

  @Test
  public void testGroupByStringsAtLevel() {
    List<String> names = List.of("a", "b");
    Dimensions dimensions = new Dimensions(names);
    List<String> subDimensions = dimensions.namesToDepth(1);

    assertThat(ListUtils.isEqualList(subDimensions, Collections.singletonList("a"))).isTrue();
  }

  @Test
  public void testNamesToDepth() {
    List<String> names = List.of("a", "b");
    Dimensions dimensions = new Dimensions(names);

    assertThat(dimensions.namesToDepth(0)).isEqualTo(Collections.<String>emptyList());
    assertThat(dimensions.namesToDepth(1)).isEqualTo(Collections.singletonList("a"));
    assertThat(dimensions.namesToDepth(2)).isEqualTo(names);
  }

  @Test
  public void testIsParentOf() {
    Dimensions dimensions1 = new Dimensions();
    assertThat(dimensions1.isParentOf(null)).isFalse();

    Dimensions dimensions2 = new Dimensions(List.of("country"));
    assertThat(dimensions2.isParentOf(dimensions2)).isFalse();
    assertThat(dimensions1.isParentOf(dimensions2)).isTrue();
    assertThat(dimensions2.isParentOf(dimensions1)).isFalse();

    Dimensions dimensions3 = new Dimensions(List.of("country", "page"));
    assertThat(dimensions2.isParentOf(dimensions3)).isTrue();
    assertThat(dimensions3.isParentOf(dimensions2)).isFalse();

    Dimensions dimensions4 = new Dimensions(List.of("page"));
    assertThat(dimensions4.isParentOf(dimensions3)).isTrue();
    assertThat(dimensions3.isParentOf(dimensions4)).isFalse();

    Dimensions dimensions5 = new Dimensions(List.of("random"));
    assertThat(dimensions5.isParentOf(dimensions3)).isFalse();
    assertThat(dimensions3.isParentOf(dimensions5)).isFalse();
  }

  @Test
  public void testEquals() {
    List<String> names = List.of("a", "b");
    Dimensions dimensions1 = new Dimensions(names);
    Dimensions dimensions2 = new Dimensions(names);

    assertThat(dimensions1.equals(dimensions2)).isTrue();
  }

  @Test
  public void testHashCode() {
    List<String> names = List.of("a", "b");
    Dimensions dimensions = new Dimensions(names);
    assertThat(dimensions.hashCode()).isEqualTo(Objects.hash(names));
  }
}
