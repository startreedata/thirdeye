package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class RootCauseSessionApi implements ThirdEyeCrudApi<RootCauseSessionApi> {

  private Long id;
  private String name;
  private String text;
  private String uiMetadata;
  private Long anomalyId;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public RootCauseSessionApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public RootCauseSessionApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getText() {
    return text;
  }

  public RootCauseSessionApi setText(final String text) {
    this.text = text;
    return this;
  }

  public String getUiMetadata() {
    return uiMetadata;
  }

  public RootCauseSessionApi setUiMetadata(final String uiMetadata) {
    this.uiMetadata = uiMetadata;
    return this;
  }

  public Long getAnomalyId() {
    return anomalyId;
  }

  public RootCauseSessionApi setAnomalyId(final Long anomalyId) {
    this.anomalyId = anomalyId;
    return this;
  }
}
