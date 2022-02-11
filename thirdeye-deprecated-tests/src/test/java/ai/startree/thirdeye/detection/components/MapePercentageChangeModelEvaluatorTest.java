/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.components;

import ai.startree.thirdeye.detection.DefaultInputDataFetcher;
import ai.startree.thirdeye.detection.MockDataProvider;
import ai.startree.thirdeye.detection.spec.MapeAveragePercentageChangeModelEvaluatorSpec;
import ai.startree.thirdeye.detection.spi.model.ModelEvaluationResult;
import ai.startree.thirdeye.detection.spi.model.ModelStatus;
import ai.startree.thirdeye.spi.datalayer.dto.EvaluationDTO;
import ai.startree.thirdeye.spi.detection.InputDataFetcher;
import java.util.Arrays;
import org.joda.time.Instant;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MapePercentageChangeModelEvaluatorTest {

  private InputDataFetcher dataFetcher;

  @BeforeMethod
  public void setUp() {
    MockDataProvider dataProvider = new MockDataProvider();
    long mockDetectionConfigId = 100L;
    String mockMetricUrn = "thirdeye:metric:1";
    EvaluationDTO eval1 = makeMockEvaluationDTO(mockDetectionConfigId, mockMetricUrn,
        1557187200000L, 1557273600000L, 0.06);
    EvaluationDTO eval2 = makeMockEvaluationDTO(mockDetectionConfigId, mockMetricUrn,
        1555368321000L, 1555454721000L, 0.055);
    dataProvider.setEvaluations(Arrays.asList(eval1, eval2));
    dataFetcher = new DefaultInputDataFetcher(dataProvider, mockDetectionConfigId);
  }

  private EvaluationDTO makeMockEvaluationDTO(long mockDetectionConfigId, String mockMetricUrn,
      long start, long end, Double mape) {
    EvaluationDTO eval = new EvaluationDTO();
    eval.setStartTime(start);
    eval.setEndTime(end);
    eval.setMetricUrn(mockMetricUrn);
    eval.setMape(mape);
    eval.setDetectionConfigId(mockDetectionConfigId);
    return eval;
  }

  @Test
  public void testEvaluateModelGood() {
    MapeAveragePercentageChangeModelEvaluatorSpec spec = new MapeAveragePercentageChangeModelEvaluatorSpec();
    spec.setThreshold(0.1);
    MapeAveragePercentageChangeModelEvaluator evaluator = new MapeAveragePercentageChangeModelEvaluator();
    evaluator.init(spec, dataFetcher);
    ModelEvaluationResult result = evaluator
        .evaluateModel(Instant.parse("2019-05-08T20:00:00.000Z"));
    Assert.assertEquals(result.getStatus(), ModelStatus.GOOD);
  }

  @Test
  public void testEvaluateModelBad() {
    MapeAveragePercentageChangeModelEvaluatorSpec spec = new MapeAveragePercentageChangeModelEvaluatorSpec();
    spec.setThreshold(0.01);
    MapeAveragePercentageChangeModelEvaluator evaluator = new MapeAveragePercentageChangeModelEvaluator();
    evaluator.init(spec, dataFetcher);
    ModelEvaluationResult result = evaluator
        .evaluateModel(Instant.parse("2019-05-08T20:00:00.000Z"));
    Assert.assertEquals(result.getStatus(), ModelStatus.BAD);
  }
}
