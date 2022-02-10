package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.Duration;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class DatasetApi implements ThirdEyeCrudApi<DatasetApi> {

  private Long id;
  private String name;
  private Boolean active;
  private Boolean additive;
  private List<String> dimensions;
  private TimeColumnApi timeColumn;
  private Duration expectedDelay;
  private DataSourceApi dataSource;

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

  public List<String> getDimensions() {
    return dimensions;
  }

  public DatasetApi setDimensions(final List<String> dimensions) {
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

  public Duration getExpectedDelay() {
    return expectedDelay;
  }

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
}
