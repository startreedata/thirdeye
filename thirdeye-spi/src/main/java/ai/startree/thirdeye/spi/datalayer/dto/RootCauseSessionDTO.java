/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * RootCauseSessionBean holds information for stored rootCause investigation reports.
 * Supports backpointers to previous versions.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class RootCauseSessionDTO extends AbstractDTO {

  public enum PermissionType {
    READ,
    READ_WRITE
  }

  private String name;
  private String text;
  private String uiMetadata;
  private Long anomalyId;

  // below is legacy - may be used in the future
  private String owner;
  private String compareMode;
  private String granularity;
  private Long previousId;
  private Long anomalyRangeStart;
  private Long anomalyRangeEnd;
  private Long analysisRangeStart;
  private Long analysisRangeEnd;
  private Long created;
  private Long updated;
  private Set<String> contextUrns;
  private Set<String> anomalyUrns;
  private Set<String> selectedUrns;
  private String permissions = PermissionType.READ_WRITE.toString();
  private Map<String, Object> customTableSettings;
  private Boolean isUserCustomizingRequest;

  public String getName() {
    return name;
  }

  public RootCauseSessionDTO setName(final String name) {
    this.name = name;
    return this;
  }

  public String getText() {
    return text;
  }

  public RootCauseSessionDTO setText(final String text) {
    this.text = text;
    return this;
  }

  public Long getAnomalyId() {
    return anomalyId;
  }

  public RootCauseSessionDTO setAnomalyId(final Long anomalyId) {
    this.anomalyId = anomalyId;
    return this;
  }

  public String getUiMetadata() {
    return uiMetadata;
  }

  public RootCauseSessionDTO setUiMetadata(final String uiMetadata) {
    this.uiMetadata = uiMetadata;
    return this;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public String getCompareMode() {
    return compareMode;
  }

  public void setCompareMode(String compareMode) {
    this.compareMode = compareMode;
  }

  public String getGranularity() {
    return granularity;
  }

  public void setGranularity(String granularity) {
    this.granularity = granularity;
  }

  public Long getPreviousId() {
    return previousId;
  }

  public void setPreviousId(Long previousId) {
    this.previousId = previousId;
  }

  public Long getAnomalyRangeStart() {
    return anomalyRangeStart;
  }

  public void setAnomalyRangeStart(Long anomalyRangeStart) {
    this.anomalyRangeStart = anomalyRangeStart;
  }

  public Long getAnomalyRangeEnd() {
    return anomalyRangeEnd;
  }

  public void setAnomalyRangeEnd(Long anomalyRangeEnd) {
    this.anomalyRangeEnd = anomalyRangeEnd;
  }

  public Long getAnalysisRangeStart() {
    return analysisRangeStart;
  }

  public void setAnalysisRangeStart(Long analysisRangeStart) {
    this.analysisRangeStart = analysisRangeStart;
  }

  public Long getAnalysisRangeEnd() {
    return analysisRangeEnd;
  }

  public void setAnalysisRangeEnd(Long analysisRangeEnd) {
    this.analysisRangeEnd = analysisRangeEnd;
  }

  public Long getCreated() {
    return created;
  }

  public void setCreated(Long created) {
    this.created = created;
  }

  public Long getUpdated() {
    return updated;
  }

  public void setUpdated(Long updated) {
    this.updated = updated;
  }

  public Set<String> getContextUrns() {
    return contextUrns;
  }

  public void setContextUrns(Set<String> contextUrns) {
    this.contextUrns = contextUrns;
  }

  public Set<String> getAnomalyUrns() {
    return anomalyUrns;
  }

  public void setAnomalyUrns(Set<String> anomalyUrns) {
    this.anomalyUrns = anomalyUrns;
  }

  public Set<String> getSelectedUrns() {
    return selectedUrns;
  }

  public void setSelectedUrns(Set<String> selectedUrns) {
    this.selectedUrns = selectedUrns;
  }

  public String getPermissions() {
    return permissions;
  }

  public void setPermissions(String permissions) {
    this.permissions = permissions;
  }

  public Map<String, Object> getCustomTableSettings() {
    return customTableSettings;
  }

  public void setCustomTableSettings(Map<String, Object> customTableSettings) {
    this.customTableSettings = customTableSettings;
  }

  public Boolean getIsUserCustomizingRequest() {
    return isUserCustomizingRequest;
  }

  public void setIsUserCustomizingRequest(Boolean isUserCustomizingRequest) {
    this.isUserCustomizingRequest = isUserCustomizingRequest;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RootCauseSessionDTO that = (RootCauseSessionDTO) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(text, that.text) &&
        Objects.equals(anomalyId, that.anomalyId) &&
        Objects.equals(uiMetadata, that.uiMetadata) &&
        Objects.equals(owner, that.owner) &&
        Objects.equals(compareMode, that.compareMode) &&
        Objects.equals(granularity, that.granularity) &&
        Objects.equals(previousId, that.previousId) &&
        Objects.equals(anomalyRangeStart, that.anomalyRangeStart) &&
        Objects.equals(anomalyRangeEnd, that.anomalyRangeEnd) &&
        Objects.equals(analysisRangeStart, that.analysisRangeStart) &&
        Objects.equals(analysisRangeEnd, that.analysisRangeEnd) &&
        Objects.equals(created, that.created) &&
        Objects.equals(updated, that.updated) &&
        Objects.equals(contextUrns, that.contextUrns) &&
        Objects.equals(anomalyUrns, that.anomalyUrns) &&
        Objects.equals(selectedUrns, that.selectedUrns) &&
        Objects.equals(permissions, that.permissions) &&
        Objects.equals(customTableSettings, that.customTableSettings) &&
        Objects.equals(isUserCustomizingRequest, that.isUserCustomizingRequest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name,
        text,
        anomalyId,
        uiMetadata,
        owner,
        compareMode,
        granularity,
        previousId,
        anomalyRangeStart,
        anomalyRangeEnd,
        analysisRangeStart,
        analysisRangeEnd,
        created,
        updated,
        contextUrns,
        anomalyUrns,
        selectedUrns,
        permissions,
        customTableSettings,
        isUserCustomizingRequest);
  }
}
