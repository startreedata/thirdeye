/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.plugins.datasource.pinot.resultset;

import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.pinot.client.ResultSetGroup;

/**
 * The ThirdEye's own {@link ResultSetGroup} for storing multiple {@link ThirdEyeResultSet} (i.e.,
 * an equivalent class
 * to Pinot's {@link ResultSet}).
 */
public class ThirdEyeResultSetGroup {

  // the list implementation should be immutable
  private List<ThirdEyeResultSet> resultSets = List.of();

  public ThirdEyeResultSetGroup(List<ThirdEyeResultSet> resultSets) {
    this.setResultSets(resultSets);
  }

  public int size() {
    return resultSets.size();
  }

  public ThirdEyeResultSet get(int idx) {
    return resultSets.get(idx);
  }

  public void setResultSets(List<ThirdEyeResultSet> resultSets) {
    this.resultSets = List.copyOf(resultSets);
  }

  public List<ThirdEyeResultSet> getResultSets() {
    return resultSets;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this).append("resultSets", resultSets).toString();
  }
}
