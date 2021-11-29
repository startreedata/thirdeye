package org.apache.pinot.thirdeye.detection.v2.operator;

import static java.util.Collections.emptyMap;
import static java.util.Objects.requireNonNull;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.detection.v2.operator.ForkJoinOperator.ForkJoinResult;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;

public class CombinerOperator extends DetectionPipelineOperator {

  public static final String DEFAULT_INPUT_KEY = "input_Combiner";
  public static final String DEFAULT_OUTPUT_KEY = "output_Combiner";
  private Map<String, Object> params;

  public CombinerOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    params = optional(getPlanNode().getParams()).orElse(emptyMap());
  }

  @Override
  public void execute() throws Exception {
    final ForkJoinResult forkJoinResult = (ForkJoinResult) requireNonNull(inputMap.get(
        DEFAULT_INPUT_KEY), "No input to combiner");
    final List<Map<String, DetectionPipelineResult>> forkJoinResults = forkJoinResult.getResults();

    final Map<String, DetectionPipelineResult> results = new HashMap<>();
    for (int i = 0; i < forkJoinResults.size(); i++) {
      final Map<String, DetectionPipelineResult> result = forkJoinResults.get(i);
      final String prefix = i + ".";
      result.forEach((k, v) -> results.put(prefix + k, v));
    }
    setOutput(DEFAULT_OUTPUT_KEY, new CombinerResult(results));
  }

  @Override
  public String getOperatorName() {
    return "CombinerOperator";
  }

  public static class CombinerResult implements DetectionPipelineResult {

    private final Map<String, DetectionPipelineResult> results;

    public CombinerResult(final Map<String, DetectionPipelineResult> results) {
      this.results = results;
    }

    @Override
    public List<DetectionResult> getDetectionResults() {
      return null;
    }

    public Map<String, DetectionPipelineResult> getResults() {
      return results;
    }
  }
}
