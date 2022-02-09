package ai.startree.thirdeye.spi.datalayer.dto;

import java.util.Date;
import java.util.List;

public class AlertTemplateDTO extends AbstractDTO {

  private String name;
  private String description;
  private String cron;
  private Date created;
  private Date updated;
  private UserBean owner;
  private List<PlanNodeBean> nodes;
  private RcaMetadataDTO rca;

  public String getName() {
    return name;
  }

  public AlertTemplateDTO setName(final String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public AlertTemplateDTO setDescription(final String description) {
    this.description = description;
    return this;
  }

  public String getCron() {
    return cron;
  }

  public AlertTemplateDTO setCron(final String cron) {
    this.cron = cron;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public AlertTemplateDTO setCreated(final Date created) {
    this.created = created;
    return this;
  }

  public Date getUpdated() {
    return updated;
  }

  public AlertTemplateDTO setUpdated(final Date updated) {
    this.updated = updated;
    return this;
  }

  public UserBean getOwner() {
    return owner;
  }

  public AlertTemplateDTO setOwner(final UserBean owner) {
    this.owner = owner;
    return this;
  }

  public List<PlanNodeBean> getNodes() {
    return nodes;
  }

  public AlertTemplateDTO setNodes(
      final List<PlanNodeBean> nodes) {
    this.nodes = nodes;
    return this;
  }

  public RcaMetadataDTO getRca() {
    return rca;
  }

  public AlertTemplateDTO setRca(
      final RcaMetadataDTO rca) {
    this.rca = rca;
    return this;
  }
}
