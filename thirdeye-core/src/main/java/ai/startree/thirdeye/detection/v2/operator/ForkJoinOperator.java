package ai.startree.thirdeye.detection.v2.operator;

import ai.startree.thirdeye.detection.v2.operator.EnumeratorOperator.EnumeratorResult;
import ai.startree.thirdeye.detection.v2.plan.PlanNodeFactory;
import ai.startree.thirdeye.spi.detection.model.DetectionResult;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.detection.v2.Operator;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import ai.startree.thirdeye.spi.detection.v2.PlanNode;
import ai.startree.thirdeye.spi.detection.v2.PlanNodeContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

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
      /* Clone all nodes for execution */
      final Map<String, PlanNode> clonedPipelinePlanNodes = clonePipelinePlanNodes(root
          .getContext()
          .getPipelinePlanNodes());

      /* Get the new root node in the cloned DAG */
      final PlanNode rootClone = clonedPipelinePlanNodes.get(root.getName());

      /* Create a callable for parallel execution */
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

  private Map<String, PlanNode> clonePipelinePlanNodes(
      final Map<String, PlanNode> pipelinePlanNodes) {
    final Map<String, PlanNode> clonedPipelinePlanNodes = new HashMap<>();
    for (Map.Entry<String, PlanNode> key : pipelinePlanNodes.entrySet()) {
      final PlanNode planNode = deepCloneWithNewContext(key.getValue(),
          properties,
          clonedPipelinePlanNodes);
      clonedPipelinePlanNodes.put(key.getKey(), planNode);
    }
    return clonedPipelinePlanNodes;
  }

  private List<Map<String, DetectionPipelineResult>> executeAll(
      final List<Callable<Map<String, DetectionPipelineResult>>> callables) {

    final List<Future<Map<String, DetectionPipelineResult>>> futures = callables.stream()
        .map(c -> executorService.submit(c))
        .collect(Collectors.toList());
    try {

      final List<Map<String, DetectionPipelineResult>> results = new ArrayList<>();
      for (final Future<Map<String, DetectionPipelineResult>> future : futures) {
        results.add(future.get(10, TimeUnit.SECONDS));
      }

      return results;
    } catch (final InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  private PlanNode deepCloneWithNewContext(final PlanNode sourceNode,
      final Map<String, Object> properties,
      final Map<String, PlanNode> clonedPipelinePlanNodes) {
    try {
      /* Cloned context should contain the new nodes */
      final PlanNodeContext clonedContext = cloneContext(sourceNode.getContext())
          .setPipelinePlanNodes(clonedPipelinePlanNodes);

      return PlanNodeFactory.build(sourceNode.getClass(), clonedContext);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Failed to clone PlanNode: " + sourceNode.getName(), e);
    }
  }

  private PlanNodeContext cloneContext(final PlanNodeContext context) {
    return new PlanNodeContext()
        .setName(context.getName())
        .setPlanNodeBean(context.getPlanNodeBean())
        .setProperties(context.getProperties())
        .setStartTime(context.getStartTime())
        .setEndTime(context.getEndTime());
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
