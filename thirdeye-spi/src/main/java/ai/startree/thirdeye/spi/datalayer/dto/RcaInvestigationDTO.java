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

/**
 * RootCauseSessionBean holds information for stored rootCause investigation reports.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class RcaInvestigationDTO extends AbstractDTO {

  private String name;
  private String text;
  private Map<String, Object> uiMetadata;
  private MergedAnomalyResultDTO anomaly;

  // below is legacy - may be used in the future
  private String compareMode;
  private String granularity;
  private Long analysisRangeStart;
  private Long analysisRangeEnd;

  public String getName() {
    return name;
  }

  public RcaInvestigationDTO setName(final String name) {
    this.name = name;
    return this;
  }

  public String getText() {
    return text;
  }

  public RcaInvestigationDTO setText(final String text) {
    this.text = text;
    return this;
  }

  public MergedAnomalyResultDTO getAnomaly() {
    return anomaly;
  }

  public RcaInvestigationDTO setAnomaly(final MergedAnomalyResultDTO anomaly) {
    this.anomaly = anomaly;
    return this;
  }

  public Map<String, Object> getUiMetadata() {
    return uiMetadata;
  }

  public RcaInvestigationDTO setUiMetadata(final Map<String, Object> uiMetadata) {
    this.uiMetadata = uiMetadata;
    return this;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RcaInvestigationDTO that = (RcaInvestigationDTO) o;
    return Objects.equals(name, that.name) &&
        Objects.equals(text, that.text) &&
        Objects.equals(anomaly, that.anomaly) &&
        Objects.equals(uiMetadata, that.uiMetadata) &&
        Objects.equals(compareMode, that.compareMode) &&
        Objects.equals(granularity, that.granularity) &&
        Objects.equals(analysisRangeStart, that.analysisRangeStart) &&
        Objects.equals(analysisRangeEnd, that.analysisRangeEnd);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name,
        text,
        anomaly,
        uiMetadata,
        compareMode,
        granularity,
        analysisRangeStart,
        analysisRangeEnd);
  }
}
