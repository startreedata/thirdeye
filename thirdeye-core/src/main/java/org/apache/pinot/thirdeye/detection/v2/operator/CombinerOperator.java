package org.apache.pinot.thirdeye.detection.v2.operator;

import static java.util.Collections.emptyMap;
import static org.apache.pinot.thirdeye.spi.util.SpiUtils.optional;

import java.util.List;
import java.util.Map;
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
    setOutput(DEFAULT_OUTPUT_KEY, new CombinerResult("CombinerOutput"));
  }

  @Override
  public String getOperatorName() {
    return "CombinerOperator";
  }

  public static class CombinerResult implements DetectionPipelineResult {

    private final String text;

    public CombinerResult(final String text) {
      this.text = text;
    }

    @Override
    public List<DetectionResult> getDetectionResults() {
      return null;
    }

    public String text() {
      return text;
    }
  }
}
