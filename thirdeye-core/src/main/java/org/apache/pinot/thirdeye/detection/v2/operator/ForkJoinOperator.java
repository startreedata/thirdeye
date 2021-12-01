package org.apache.pinot.thirdeye.detection.v2.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
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
  private static final int PARALLELISM = 5;

  private Map<String, Object> properties;
  private PlanNode enumerator;
  private PlanNode root;
  private PlanNode combiner;
  private ExecutorService executorService;

  @Override
  public void init(final OperatorContext context) {
    super.init(context);
    properties = context.getProperties();
    enumerator = (PlanNode) properties.get("enumerator");
    root = (PlanNode) properties.get("root");
    combiner = (PlanNode) properties.get("combiner");

    executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  @Override
  public void execute() throws Exception {
    final Operator op = enumerator.buildOperator();
    op.execute();
    final Map<String, DetectionPipelineResult> outputs = op.getOutputs();
    final EnumeratorResult enumeratorResult = (EnumeratorResult) outputs.get(EnumeratorOperator.DEFAULT_OUTPUT_KEY);
    final List<Map<Object, Object>> results = enumeratorResult.getResults();
    final List<Callable<Map<String, DetectionPipelineResult>>> callables = new ArrayList<>();

    for (final Map<Object, Object> result : results) {
      final PlanNode rootClone = deepClone(root);
      setContext(rootClone, properties);
      callables.add((() -> {
        final Operator operator = rootClone.buildOperator();
        operator.execute();
        return operator.getOutputs();
      }));
    }
    final List<Map<String, DetectionPipelineResult>> allResults = executeAll(callables);

    final Operator combinerOp = combiner.buildOperator();
    combinerOp.setInput(CombinerOperator.DEFAULT_INPUT_KEY, new ForkJoinResult(allResults));
    combinerOp.execute();

    resultMap.putAll(combinerOp.getOutputs());
  }

  private void setContext(final PlanNode rootClone, final Map<String, Object> properties) {

  }

  private List<Map<String, DetectionPipelineResult>> executeAll(
      final List<Callable<Map<String, DetectionPipelineResult>>> callables) {

    final List<Future<Map<String, DetectionPipelineResult>>> futures = callables.stream()
        .map(c -> executorService.submit(c))
        .collect(Collectors.toList());
    try {
      executorService.awaitTermination(10, TimeUnit.SECONDS);

      final List<Map<String, DetectionPipelineResult>> results = new ArrayList<>();
      for (final Future<Map<String, DetectionPipelineResult>> future : futures) {
        results.add(future.get());
      }
      return results;
    } catch (final InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private PlanNode deepClone(final PlanNode root) {
    // TODO spyne implement
    return root;
  }

  @Override
  public String getOperatorName() {
    return "forkjoin";
  }

  public static class ForkJoinResult implements DetectionPipelineResult {

    private final List<Map<String, DetectionPipelineResult>> results;

    public ForkJoinResult(final List<Map<String, DetectionPipelineResult>> results) {
      this.results = results;
    }

    @Override
    public List<DetectionResult> getDetectionResults() {
      return null;
    }

    public List<Map<String, DetectionPipelineResult>> getResults() {
      return results;
    }
  }
}
