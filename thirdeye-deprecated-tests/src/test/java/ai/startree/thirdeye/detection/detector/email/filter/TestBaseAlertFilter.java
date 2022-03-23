/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.detector.email.filter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.datalayer.DaoTestUtils;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyFunctionDTO;
import java.util.Map;
import org.testng.annotations.Test;

public class TestBaseAlertFilter {

  private static final String collection = "my dataset";
  private static final String metricName = "__counts";

  // test set up double, Double, string for alpha_beta
  // also test missing input parameter, will use default value defined in the specified class
  @Test
  public void testSetAlphaBetaParamter() {
    AnomalyFunctionDTO anomalyFunctionSpec = DaoTestUtils
        .getTestFunctionAlphaBetaAlertFilterSpec(metricName, collection);
    Map<String, String> alertfilter = anomalyFunctionSpec.getAlertFilter();
    AlphaBetaAlertFilter alphaBetaAlertFilter = new AlphaBetaAlertFilter();
    alphaBetaAlertFilter.setParameters(alertfilter);
    assertThat(alphaBetaAlertFilter.getAlpha()).isEqualTo(Double.valueOf(alertfilter.get("alpha")));
    assertThat(alphaBetaAlertFilter.getBeta()).isEqualTo(Double.valueOf(alertfilter.get("beta")));
    assertThat(alphaBetaAlertFilter.getType()).isEqualTo(alertfilter.get("type"));
    assertThat(alphaBetaAlertFilter.getThreshold()).isEqualTo(
        Double.valueOf(alertfilter.get("threshold")));

    // test scientific decimal
    double threshold = 1E-10;
    alertfilter.put("threshold", String.valueOf(threshold));
    alphaBetaAlertFilter.setParameters(alertfilter);
    assertThat(alphaBetaAlertFilter.getThreshold()).isEqualTo(
        Double.valueOf(alertfilter.get("threshold")));

    // test missing field
    alertfilter.remove("threshold");
    AlphaBetaAlertFilter alphaBetaAlertFilter1 = new AlphaBetaAlertFilter();
    alphaBetaAlertFilter1.setParameters(alertfilter);
    assertThat(alphaBetaAlertFilter1.getThreshold()).isEqualTo(
        Double.valueOf(AlphaBetaAlertFilter.DEFAULT_THRESHOLD));
  }
}
