package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class RcaInvestigationApi implements ThirdEyeCrudApi<RcaInvestigationApi> {

  private Long id;
  private String name;
  private String text;
  private Map<String, Object> uiMetadata;
  private AnomalyApi anomaly;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public RcaInvestigationApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public RcaInvestigationApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getText() {
    return text;
  }

  public RcaInvestigationApi setText(final String text) {
    this.text = text;
    return this;
  }

  public Map<String, Object> getUiMetadata() {
    return uiMetadata;
  }

  public RcaInvestigationApi setUiMetadata(final Map<String, Object> uiMetadata) {
    this.uiMetadata = uiMetadata;
    return this;
  }

  public AnomalyApi getAnomaly() {
    return anomaly;
  }

  public RcaInvestigationApi setAnomaly(final AnomalyApi anomaly) {
    this.anomaly = anomaly;
    return this;
  }
}
