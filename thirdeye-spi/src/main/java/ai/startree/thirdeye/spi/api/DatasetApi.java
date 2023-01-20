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
package ai.startree.thirdeye.spi.api;

import ai.startree.thirdeye.spi.datalayer.Templatable;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.Duration;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class DatasetApi implements ThirdEyeCrudApi<DatasetApi> {

  private Long id;
  private String name;
  private String namespace;
  private Boolean active;
  private Boolean additive;
  private Templatable<List<String>> dimensions;
  private TimeColumnApi timeColumn;
  @Deprecated // use completenessDelay
  private Duration expectedDelay;
  private DataSourceApi dataSource;
  /**
   * Expected delay for data to be complete. In ISO 8601. Eg P1D.
   */
  private String completenessDelay;
  /**
   * Dimensions to exclude from RCA algorithm runs.
   */
  private Templatable<List<String>> rcaExcludedDimensions;

  public Long getId() {
    return id;
  }

  public DatasetApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public DatasetApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getNamespace() {
    return namespace;
  }

  public DatasetApi setNamespace(final String namespace) {
    this.namespace = namespace;
    return this;
  }

  public Boolean getActive() {
    return active;
  }

  public DatasetApi setActive(final Boolean active) {
    this.active = active;
    return this;
  }

  public Boolean getAdditive() {
    return additive;
  }

  public DatasetApi setAdditive(final Boolean additive) {
    this.additive = additive;
    return this;
  }

  public Templatable<List<String>> getDimensions() {
    return dimensions;
  }

  public DatasetApi setDimensions(final Templatable<List<String>> dimensions) {
    this.dimensions = dimensions;
    return this;
  }

  public TimeColumnApi getTimeColumn() {
    return timeColumn;
  }

  public DatasetApi setTimeColumn(final TimeColumnApi timeColumn) {
    this.timeColumn = timeColumn;
    return this;
  }

  @Deprecated // use completenessDelay
  public Duration getExpectedDelay() {
    return expectedDelay;
  }

  @Deprecated // use completenessDelay
  public DatasetApi setExpectedDelay(final Duration expectedDelay) {
    this.expectedDelay = expectedDelay;
    return this;
  }

  public DataSourceApi getDataSource() {
    return dataSource;
  }

  public DatasetApi setDataSource(DataSourceApi dataSource) {
    this.dataSource = dataSource;
    return this;
  }

  public String getCompletenessDelay() {
    return completenessDelay;
  }

  public DatasetApi setCompletenessDelay(final String completenessDelay) {
    this.completenessDelay = completenessDelay;
    return this;
  }

  public Templatable<List<String>> getRcaExcludedDimensions() {
    return rcaExcludedDimensions;
  }

  public DatasetApi setRcaExcludedDimensions(
      final Templatable<List<String>> rcaExcludedDimensions) {
    this.rcaExcludedDimensions = rcaExcludedDimensions;
    return this;
  }
}
