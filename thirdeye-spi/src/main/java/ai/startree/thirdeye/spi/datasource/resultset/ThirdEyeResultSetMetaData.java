/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi.datasource.resultset;

import ai.startree.thirdeye.spi.detection.v2.ColumnType;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;

public class ThirdEyeResultSetMetaData {

  private final List<String> groupKeyColumnNames;
  private final List<String> metricColumnNames;
  private final List<String> allColumnNames;
  private final List<ColumnType> groupKeyColumnTypes;
  private final List<ColumnType> metricColumnTypes;
  private final List<ColumnType> allColumnTypes;

  public ThirdEyeResultSetMetaData(List<String> groupKeyColumnNames,
      List<String> metricColumnNames, List<ColumnType> groupKeyColumnTypes,
      List<ColumnType> metricColumnTypes) {
    Preconditions.checkNotNull(groupKeyColumnNames);
    Preconditions.checkNotNull(metricColumnNames);

    this.groupKeyColumnNames = ImmutableList.copyOf(groupKeyColumnNames);
    this.metricColumnNames = ImmutableList.copyOf(metricColumnNames);
    this.allColumnNames = ImmutableList.<String>builder().addAll(this.groupKeyColumnNames)
        .addAll(this.metricColumnNames)
        .build();
    this.groupKeyColumnTypes = ImmutableList.copyOf(groupKeyColumnTypes);
    this.metricColumnTypes = ImmutableList.copyOf(metricColumnTypes);
    this.allColumnTypes = ImmutableList.<ColumnType>builder().addAll(this.groupKeyColumnTypes)
        .addAll(this.metricColumnTypes)
        .build();
  }

  public List<String> getGroupKeyColumnNames() {
    return groupKeyColumnNames;
  }

  public List<ColumnType> getGroupKeyColumnTypes() {
    return groupKeyColumnTypes;
  }

  public List<String> getMetricColumnNames() {
    return metricColumnNames;
  }

  public List<ColumnType> getMetricColumnTypes() {
    return metricColumnTypes;
  }

  public List<String> getAllColumnNames() {
    return allColumnNames;
  }

  public List<ColumnType> getAllColumnTypes() {
    return allColumnTypes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThirdEyeResultSetMetaData metaData = (ThirdEyeResultSetMetaData) o;
    return Objects.equals(getGroupKeyColumnNames(), metaData.getGroupKeyColumnNames())
        && Objects.equals(getMetricColumnNames(), metaData.getMetricColumnNames())
        && Objects.equals(getAllColumnNames(), metaData.getAllColumnNames()) && Objects.equals(
        getGroupKeyColumnTypes(),
        metaData.getGroupKeyColumnTypes()) && Objects.equals(getMetricColumnTypes(),
        metaData.getMetricColumnTypes()) && Objects.equals(getAllColumnTypes(),
        metaData.getAllColumnTypes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getGroupKeyColumnNames(),
        getMetricColumnNames(),
        getAllColumnNames(),
        getGroupKeyColumnTypes(),
        getMetricColumnTypes(),
        getAllColumnTypes());
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ThirdEyeResultSetMetaData{");
    sb.append("groupKeyColumnNames=").append(groupKeyColumnNames);
    sb.append(", groupKeyColumnTypes=").append(groupKeyColumnTypes);
    sb.append(", metricColumnNames=").append(metricColumnNames);
    sb.append(", metricColumnTypes=").append(metricColumnTypes);
    sb.append(", allColumnNames=").append(allColumnNames);
    sb.append(", allColumnTypes=").append(allColumnTypes);
    sb.append('}');
    return sb.toString();
  }
}
