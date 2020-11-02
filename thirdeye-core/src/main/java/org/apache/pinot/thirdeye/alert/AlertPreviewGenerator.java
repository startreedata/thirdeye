package org.apache.pinot.thirdeye.alert;

import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.DetectionPipeline;
import org.apache.pinot.thirdeye.detection.DetectionPipelineLoader;
import org.apache.pinot.thirdeye.detection.DetectionPipelineResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class AlertPreviewGenerator {

  protected static final Logger LOG = LoggerFactory.getLogger(AlertPreviewGenerator.class);
  // 5 detection previews are running at the same time at most
  private static final int PARALLELISM = 5;
  // max time allowed for a preview task
  private static final long TIMEOUT = TimeUnit.MINUTES.toMillis(5);

  private final DataProvider dataProvider;
  private final AlertManager alertManager;
  private final AlertCreater alertCreater;
  private final ExecutorService executorService;

  @Inject
  public AlertPreviewGenerator(
      final DataProvider dataProvider,
      final AlertManager alertManager,
      final AlertCreater alertCreater) {
    this.dataProvider = dataProvider;
    this.alertManager = alertManager;
    this.alertCreater = alertCreater;

    this.executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  public DetectionPipelineResult runPreview(final AlertEvaluationApi request)
      throws InterruptedException, ExecutionException, TimeoutException {
    final AlertDTO alert = getAlert(ensureExists(request.getAlert()));

    final DetectionPipeline pipeline = new DetectionPipelineLoader().from(
        dataProvider,
        alert,
        request.getStart().getTime(),
        request.getEnd().getTime());

    return executorService
        .submit(pipeline::run)
        .get(TIMEOUT, TimeUnit.MILLISECONDS);
  }

  private AlertDTO getAlert(final AlertApi api) {
    final AlertDTO dto;
    if (api.getId() != null) {
      dto = alertManager.findById(api.getId());
    } else {
      dto = alertCreater.toAlertDTO(api);
      dto.setId(System.currentTimeMillis());
    }

    return dto;
  }
}
