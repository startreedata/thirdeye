package org.apache.pinot.thirdeye.alert;

import static com.google.common.base.Preconditions.checkState;
import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_DATA_UNAVAILABLE;
import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_OBJECT_DOES_NOT_EXIST;
import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_TIMEOUT;
import static org.apache.pinot.thirdeye.ThirdEyeStatus.ERR_UNKNOWN;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.badRequest;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.ensureExists;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.serverError;
import static org.apache.pinot.thirdeye.resources.ResourceUtils.statusListApi;

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Longs;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.pinot.thirdeye.ThirdEyeException;
import org.apache.pinot.thirdeye.api.AlertApi;
import org.apache.pinot.thirdeye.api.AlertEvaluationApi;
import org.apache.pinot.thirdeye.api.DetectionDataApi;
import org.apache.pinot.thirdeye.api.DetectionEvaluationApi;
import org.apache.pinot.thirdeye.dataframe.DataFrame;
import org.apache.pinot.thirdeye.dataframe.DoubleSeries;
import org.apache.pinot.thirdeye.dataframe.LongSeries;
import org.apache.pinot.thirdeye.datalayer.bao.AlertManager;
import org.apache.pinot.thirdeye.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.datalayer.dto.EvaluationDTO;
import org.apache.pinot.thirdeye.datalayer.dto.MergedAnomalyResultDTO;
import org.apache.pinot.thirdeye.detection.DataProvider;
import org.apache.pinot.thirdeye.detection.DataProviderException;
import org.apache.pinot.thirdeye.detection.DetectionPipeline;
import org.apache.pinot.thirdeye.detection.DetectionPipelineContext;
import org.apache.pinot.thirdeye.detection.DetectionPipelineException;
import org.apache.pinot.thirdeye.detection.DetectionPipelineFactory;
import org.apache.pinot.thirdeye.detection.DetectionPipelineResultV1;
import org.apache.pinot.thirdeye.detection.PredictionResult;
import org.apache.pinot.thirdeye.util.ApiBeanMapper;
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

  @Inject
  public AlertEvaluator(
      final DataProvider dataProvider,
      final AlertManager alertManager,
      final AlertApiBeanMapper alertApiBeanMapper) {
    this.dataProvider = dataProvider;
    this.alertManager = alertManager;
    this.alertApiBeanMapper = alertApiBeanMapper;

    this.executorService = Executors.newFixedThreadPool(PARALLELISM);
  }

  private static String name(final String detectorName) {
    final String[] split = detectorName.split(":");
    checkState(split.length == 2, "Malformed detector name: " + detectorName);
    return split[0];
  }

  public AlertEvaluationApi evaluate(final AlertEvaluationApi request)
      throws ExecutionException {
    try {
      final DetectionPipelineResultV1 result = runPipeline(request);
      return toApi(result);
    } catch (ThirdEyeException e) {
      throw badRequest(statusListApi(e.getStatus(), e.getMessage()));
    } catch (InterruptedException e) {
      LOG.error("Error occurred during evaluate", e);
      throw serverError(ERR_UNKNOWN, e.getMessage());
    } catch (TimeoutException e) {
      LOG.error("Error occurred during evaluate", e);
      throw serverError(ERR_TIMEOUT);
    } catch (ExecutionException e) {
      LOG.error("Error occurred during evaluate", e);
      handleExecutionException(e);
      throw e;
    }
  }

  private void handleExecutionException(final ExecutionException e) {
    final Throwable cause = e.getCause();
    if (cause instanceof DetectionPipelineException) {
      final Throwable innerCause = cause.getCause();
      if (innerCause instanceof DataProviderException) {
        throw serverError(ERR_DATA_UNAVAILABLE, innerCause.getMessage());
      }
      throw serverError(ERR_UNKNOWN, cause.getMessage());
    }
  }

  private DetectionPipelineResultV1 runPipeline(final AlertEvaluationApi request)
      throws InterruptedException, ExecutionException, TimeoutException {
    final AlertDTO alert = getAlert(ensureExists(request.getAlert()));
    final DetectionPipeline pipeline = new DetectionPipelineFactory(dataProvider).get(
        new DetectionPipelineContext()
            .setAlert(alert)
            .setStart(request.getStart().getTime())
            .setEnd(request.getEnd().getTime())
    );

    return executorService
        .submit(pipeline::run)
        .get(TIMEOUT, TimeUnit.MILLISECONDS);
  }

  private AlertEvaluationApi toApi(final DetectionPipelineResultV1 result) {

    final Map<String, DetectionEvaluationApi> map = new HashMap<>();

    for (EvaluationDTO dto : result.getEvaluations()) {
      final String name = name(dto.getDetectorName());
      map
          .computeIfAbsent(name, v -> new DetectionEvaluationApi())
          .setMape(dto.getMape());
    }

    for (MergedAnomalyResultDTO anomaly : result.getAnomalies()) {
      final String name = name(anomaly.getProperties().get("detectorComponentName"));
      map
          .computeIfAbsent(name, v -> new DetectionEvaluationApi())
          .getAnomalies()
          .add(ApiBeanMapper.toApi(anomaly));
    }

    for (PredictionResult predictionResult : result.getPredictions()) {
      final String name = name(predictionResult.getDetectorName());
      map
          .computeIfAbsent(name, v -> new DetectionEvaluationApi())
          .setData(toDetectionDataApi(predictionResult.getPredictedTimeSeries()));
    }

    return new AlertEvaluationApi()
        .setDetectionEvaluations(map);
  }

  private DetectionDataApi toDetectionDataApi(final DataFrame df) {
    return new DetectionDataApi()
        .setTimestamp(Longs.asList(((LongSeries) df.get("timestamp")).values()))
        .setCurrent(Doubles.asList(((DoubleSeries) df.get("current")).values()))
        .setExpected(Doubles.asList(((DoubleSeries) df.get("value")).values()))
        .setUpperBound(Doubles.asList(((DoubleSeries) df.get("upper_bound")).values()))
        .setLowerBound(Doubles.asList(((DoubleSeries) df.get("lower_bound")).values()));
  }

  private AlertDTO getAlert(final AlertApi api) {
    final AlertDTO dto;
    if (api.getId() != null) {
      dto = alertManager.findById(api.getId());
    } else {
      ensureExists(api.getNodes(), ERR_OBJECT_DOES_NOT_EXIST, "alert.nodes missing");
      dto = alertApiBeanMapper.toAlertDTO(api);
      dto.setId(System.currentTimeMillis());
    }

    return dto;
  }
}
