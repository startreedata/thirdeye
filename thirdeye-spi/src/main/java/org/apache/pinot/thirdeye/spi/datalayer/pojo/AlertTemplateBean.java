package org.apache.pinot.thirdeye.spi.datalayer.pojo;

import java.util.Date;
import java.util.List;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AbstractDTO;

public class AlertTemplateBean extends AbstractDTO {

  private String name;
  private String description;
  private String cron;
  private Date created;
  private Date updated;
  private UserBean owner;
  private List<PlanNodeBean> nodes;

  public String getName() {
    return name;
  }

  public AlertTemplateBean setName(final String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public AlertTemplateBean setDescription(final String description) {
    this.description = description;
    return this;
  }

  public String getCron() {
    return cron;
  }

  public AlertTemplateBean setCron(final String cron) {
    this.cron = cron;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public AlertTemplateBean setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public Date getUpdated() {
    return updated;
  }

  public AlertTemplateBean setUpdated(final Date updated) {
    this.updated = updated;
    return this;
  }

  public UserBean getOwner() {
    return owner;
  }

  public AlertTemplateBean setOwner(final UserBean owner) {
    this.owner = owner;
    return this;
  }

  public List<PlanNodeBean> getNodes() {
    return nodes;
  }

  public AlertTemplateBean setNodes(
      final List<PlanNodeBean> nodes) {
    this.nodes = nodes;
    return this;
  }
}
