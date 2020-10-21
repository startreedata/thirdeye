package org.apache.pinot.thirdeye.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;

@JsonInclude(Include.NON_NULL)
public class MetricApi {

  private Long id;
  private String name;
  private String urn;
  private DatasetApi dataset;
  private Boolean active;
  private Date created;
  private Date updated;

  public Long getId() {
    return id;
  }

  public MetricApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public MetricApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getUrn() {
    return urn;
  }

  public MetricApi setUrn(final String urn) {
    this.urn = urn;
    return this;
  }

  public DatasetApi getDataset() {
    return dataset;
  }

  public MetricApi setDataset(final DatasetApi dataset) {
    this.dataset = dataset;
    return this;
  }

  public Boolean getActive() {
    return active;
  }

  public MetricApi setActive(final Boolean active) {
    this.active = active;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public MetricApi setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public Date getUpdated() {
    return updated;
  }

  public MetricApi setUpdated(final Date updated) {
    this.updated = updated;
    return this;
  }
}
