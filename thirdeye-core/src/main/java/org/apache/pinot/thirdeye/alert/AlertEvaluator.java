package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.pinot.thirdeye.mapper.AlertApiBeanMapper;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertEvaluator {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertEvaluator.class);
  // 5 detection previews are running at the same time at most
  private static final int PARALLELISM = 5;
  // max time allowed for a preview task
  private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);

  private final DataProvider dataProvider;
  private final AlertManager alertManager;
  private final AlertApiBeanMapper alertApiBeanMapper;
  private final ExecutorService executorService;
  private final AlertEvaluatorV2 alertEvaluatorV2;

  @Inject
  public AlertEvaluator(
      final DataProvider dataProvider,
      final AlertManager alertManager,
      final AlertApiBeanMapper alertApiBeanMapper,
      final AlertEvaluatorV2 alertEvaluatorV2) {
    this.dataProvider = dataProvider;
    this.alertManager = alertManager;
    this.alertApiBeanMapper = alertApiBeanMapper;
    this.alertEvaluatorV2 = alertEvaluatorV2;

    this.executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  public AlertEvaluationApi evaluate(final AlertEvaluationApi request)
      throws ExecutionException {
    if (isV2Evaluation(request.getAlert())) {
      return alertEvaluatorV2.evaluate(request);
    } else {
      throw new RuntimeException("V1 detection pipeline is not supported anymore.");
    }
  }

  /**
   * For compatibility with Heatmap, legacy pipeline can contain template:rca field
   * template name and template node is specific to v2.
   *
   * todo cyril remove this check a few months after legacy pipleine is removed
   */
  public boolean isV2Evaluation(final AlertApi alert) {
    if (alert.getId() != null) {
      AlertDTO alertDTO = ensureExists(alertManager.findById(alert.getId()));
      return PlanExecutor.isV2Alert(alertDTO);
    }
    if (alert.getTemplate() == null) {
      return false;
    }
    return alert.getTemplate().getName() != null || alert.getTemplate().getNodes() != null;
  }
}
