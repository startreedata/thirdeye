package org.apache.pinot.thirdeye.detection.v2.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.pinot.thirdeye.detection.v2.operator.EnumeratorOperator.EnumeratorResult;
import org.apache.pinot.thirdeye.spi.detection.model.DetectionResult;
import org.apache.pinot.thirdeye.spi.detection.v2.DetectionPipelineResult;
import org.apache.pinot.thirdeye.spi.detection.v2.Operator;
import org.apache.pinot.thirdeye.spi.detection.v2.OperatorContext;
import org.apache.pinot.thirdeye.spi.detection.v2.PlanNode;

public class ForkJoinOperator extends DetectionPipelineOperator {

  public static final String K_ENUMERATOR = "enumerator";
  public static final String K_ROOT = "root";
  public static final String K_COMBINER = "combiner";

  private Map<String, Object> properties;
  private PlanNode enumerator;
  private PlanNode root;
  private PlanNode combiner;

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    properties = context.getProperties();
    enumerator = (PlanNode) properties.get("enumerator");
    root = (PlanNode) properties.get("root");
    combiner = (PlanNode) properties.get("combiner");
  }

  @Override
  public void execute() throws Exception {
    final Operator op = enumerator.run();
    op.execute();
    final Map<String, DetectionPipelineResult> outputs = op.getOutputs();
    final EnumeratorResult enumeratorResult = (EnumeratorResult) outputs.get(EnumeratorOperator.DEFAULT_OUTPUT_KEY);
    final List<Map<Object, Object>> results = enumeratorResult.getResults();
    final List<Callable> callables = new ArrayList<>();

    for (final Map<Object, Object> result : results) {
      final PlanNode rootClone = deepClone(root);
      setContext(rootClone, properties);
      callables.add((() -> {
        final Operator operator = rootClone.run();
        operator.execute();
        return operator.getOutputs();
      }));
    }
    final List<Map<String, Object>> allResults = executeAll(callables);

    final Operator combinerOp = combiner.run();
    combinerOp.setInput("allResults", new ForkJoinResult(allResults));
    combinerOp.execute();

    resultMap.putAll(combinerOp.getOutputs());
  }

  private void setContext(final PlanNode rootClone, final Map<String, Object> properties) {

  }

  private List<Map<String, Object>> executeAll(final List<Callable> l) {
    return null;
  }

  private PlanNode deepClone(final PlanNode root) {
    return null;
  }

  @Override
  public String getOperatorName() {
    return "forkjoin";
  }

  public static class ForkJoinResult implements DetectionPipelineResult {

    private final List<Map<String, Object>> results;

    public ForkJoinResult(final List<Map<String, Object>> results) {
      this.results = results;
    }

    @Override
    public List<DetectionResult> getDetectionResults() {
      return null;
    }

    public List<Map<String, Object>> getResults() {
      return results;
    }
  }
}
