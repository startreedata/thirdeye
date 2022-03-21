package org.apache.pinot.thirdeye.spi.api;

import java.util.Date;
import java.util.List;

public class AlertTemplateApi implements ThirdEyeCrudApi<AlertTemplateApi> {

  private Long id;
  private String name;
  private String description;
  private String cron;
  private Date created;
  private Date updated;
  private UserApi owner;
  private List<PlanNodeApi> nodes;
  private RcaMetadataApi rca;

  public Long getId() {
    return id;
  }

  public AlertTemplateApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public AlertTemplateApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public AlertTemplateApi setDescription(final String description) {
    this.description = description;
    return this;
  }

  public String getCron() {
    return cron;
  }

  public AlertTemplateApi setCron(final String cron) {
    this.cron = cron;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public AlertTemplateApi setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public Date getUpdated() {
    return updated;
  }

  public AlertTemplateApi setUpdated(final Date updated) {
    this.updated = updated;
    return this;
  }

  public UserApi getOwner() {
    return owner;
  }

  public AlertTemplateApi setOwner(final UserApi owner) {
    this.owner = owner;
    return this;
  }

  public List<PlanNodeApi> getNodes() {
    return nodes;
  }

  public AlertTemplateApi setNodes(
      final List<PlanNodeApi> nodes) {
    this.nodes = nodes;
    return this;
  }

  public RcaMetadataApi getRca() {
    return rca;
  }

  public AlertTemplateApi setRca(final RcaMetadataApi rca) {
    this.rca = rca;
    return this;
  }
}
