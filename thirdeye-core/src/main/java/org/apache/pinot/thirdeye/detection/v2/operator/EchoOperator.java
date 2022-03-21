package org.apache.pinot.thirdeye.detection.v2.operator;

import java.util.List;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;

public class EchoOperator extends DetectionPipelineOperator {

  public static final String DEFAULT_INPUT_KEY = "input_Echo";
  public static final String DEFAULT_OUTPUT_KEY = "output_Echo";

  public EchoOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
  }

  @Override
  public void execute() throws Exception {
    final String echoText = getPlanNode().getParams().get(DEFAULT_INPUT_KEY).toString();
    setOutput(DEFAULT_OUTPUT_KEY, new EchoResult(echoText));
  }

  @Override
  public String getOperatorName() {
    return "EchoOperator";
  }

  public static class EchoResult implements DetectionPipelineResult {

    private final String text;

    public EchoResult(final String text) {
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
