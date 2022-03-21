package org.apache.pinot.thirdeye.detection.v2.operator;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;

public class EnumeratorOperator extends DetectionPipelineOperator {

  public static final String DEFAULT_INPUT_KEY = "input_Enumerator";
  public static final String DEFAULT_OUTPUT_KEY = "output_Enumerator";

  public EnumeratorOperator() {
    super();
  }

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
  }

  @Override
  public void execute() throws Exception {
    setOutput(DEFAULT_OUTPUT_KEY, new EnumeratorResult(Arrays.asList(
        ImmutableMap.of("key", 1),
        ImmutableMap.of("key", 2),
        ImmutableMap.of("key", 3)
    )));
  }

  @Override
  public String getOperatorName() {
    return "EnumeratorOperator";
  }

  public static class EnumeratorResult implements DetectionPipelineResult {

    private final List<Map<Object, Object>> results;

    public EnumeratorResult(final List<Map<Object, Object>> results) {
      this.results = results;
    }

    @Override
    public List<DetectionResult> getDetectionResults() {
      return null;
    }

    public List<Map<Object, Object>> getResults() {
      return results;
    }
  }
}
