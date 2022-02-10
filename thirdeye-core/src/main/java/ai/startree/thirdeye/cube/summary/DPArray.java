/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.cube.summary;

import ai.startree.thirdeye.cube.data.node.CubeNode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DPArray {

  double targetRatio;
  List<DPSlot> slots;
  int size;
  int shrankSize;

  public DPArray(int size) {
    slots = new ArrayList<>(size);
    for (int i = 0; i < size; ++i) {
      slots.add(new DPSlot());
    }
    this.size = size;
    this.shrankSize = size;
  }

  public DPSlot slotAt(int index) {
    return slots.get(index);
  }

  public int maxSize() {
    return this.size;
  }

  public int size() {
    return this.shrankSize;
  }

  public void setShrinkSize(int size) {
    this.shrankSize = size;
  }

  public Set<CubeNode> getAnswer() {
    return slots.get(this.shrankSize - 1).ans;
  }

  public void reset() {
    targetRatio = 0.;
    for (DPSlot slot : slots) {
      slot.cost = 0.;
      slot.ans.clear();
    }
  }

  public void fullReset() {
    reset();
    shrankSize = size;
  }

  public String toString() {
    if (slots != null) {
      StringBuilder sb = new StringBuilder();
      for (DPSlot slot : slots) {
        sb.append(ToStringBuilder.reflectionToString(slot, ToStringStyle.SHORT_PREFIX_STYLE));
        sb.append('\n');
      }
      return sb.toString();
    } else {
      return "";
    }
  }

  public static class DPSlot {

    double cost;
    Set<CubeNode> ans = new HashSet<>();

    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
  }
}
