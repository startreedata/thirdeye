/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AlertTemplateDTO extends AbstractDTO {

  private String name;
  private String description;
  private String cron;
  private Date created;
  private Date updated;
  private UserBean owner;
  private List<PlanNodeBean> nodes;
  @Deprecated  // use AlertMetadataDTO
  @JsonIgnore
  private RcaMetadataDTO rca;
  private AlertMetadataDTO metadata;
  private Map<String, Object> defaultProperties;

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

  @Deprecated
  public RcaMetadataDTO getRca() {
    return rca;
  }

  @Deprecated
  public AlertTemplateDTO setRca(
      final RcaMetadataDTO rca) {
    this.rca = rca;
    return this;
  }

  public AlertMetadataDTO getMetadata() {
    return metadata;
  }

  public AlertTemplateDTO setMetadata(
      final AlertMetadataDTO metadata) {
    this.metadata = metadata;
    return this;
  }

  public Map<String, Object> getDefaultProperties() {
    return defaultProperties;
  }

  public AlertTemplateDTO setDefaultProperties(
      final Map<String, Object> defaultProperties) {
    this.defaultProperties = defaultProperties;
    return this;
  }
}
