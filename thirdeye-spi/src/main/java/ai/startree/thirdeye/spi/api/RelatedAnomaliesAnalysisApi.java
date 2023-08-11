package ai.startree.thirdeye.spi.api;

import java.util.List;

public class RelatedAnomaliesAnalysisApi {

  private List<AnomalyApi> anomalies;

  private TextualAnalysis textualAnalysis;

  public List<AnomalyApi> getAnomalies() {
    return anomalies;
  }

  public RelatedAnomaliesAnalysisApi setAnomalies(
      final List<AnomalyApi> anomalies) {
    this.anomalies = anomalies;
    return this;
  }

  public TextualAnalysis getTextualAnalysis() {
    return textualAnalysis;
  }

  public RelatedAnomaliesAnalysisApi setTextualAnalysis(
      final TextualAnalysis textualAnalysis) {
    this.textualAnalysis = textualAnalysis;
    return this;
  }
}
