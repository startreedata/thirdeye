package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.util.ResourceUtils.ensureExists;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutionException;
import org.apache.pinot.thirdeye.spi.api.AlertApi;
import org.apache.pinot.thirdeye.spi.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.spi.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertEvaluator {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertEvaluator.class);

  private final AlertManager alertManager;
  private final AlertEvaluatorV2 alertEvaluatorV2;

  @Inject
  public AlertEvaluator(
      final AlertManager alertManager,
      final AlertEvaluatorV2 alertEvaluatorV2) {
    this.alertManager = alertManager;
    this.alertEvaluatorV2 = alertEvaluatorV2;
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
   * todo cyril remove this check a few months after legacy pipeline is removed
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
