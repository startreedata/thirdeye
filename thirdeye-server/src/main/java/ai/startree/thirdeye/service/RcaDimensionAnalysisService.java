/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.service;

import static ai.startree.thirdeye.rca.RcaDimensionFilterHelper.getRcaDimensions;
import static ai.startree.thirdeye.spi.util.SpiUtils.optional;
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;
import static ai.startree.thirdeye.util.ResourceUtils.ensureExists;
import static ai.startree.thirdeye.util.StringUtils.timeFormatterFor;

import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.rca.RcaInfo;
import ai.startree.thirdeye.rca.RcaInfoFetcher;
import ai.startree.thirdeye.rootcause.ContributorsFinderRunner;
import ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi;
import ai.startree.thirdeye.spi.api.TextualAnalysis;
import ai.startree.thirdeye.spi.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.rca.ContributorsFinderResult;
import ai.startree.thirdeye.spi.rca.ContributorsSearchConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.text.DecimalFormat;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormatter;

@Singleton
public class RcaDimensionAnalysisService {
  private static final DecimalFormat PERCENTAGE_FORMAT = new DecimalFormat("#.##");
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.###");

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ContributorsFinderRunner contributorsFinderRunner;
  private final RcaInfoFetcher rcaInfoFetcher;
  private final AnomalyManager anomalyDao;
  private final AuthorizationManager authorizationManager;

  @Inject
  public RcaDimensionAnalysisService(final ContributorsFinderRunner contributorsFinderRunner,
      final RcaInfoFetcher rcaInfoFetcher,
      final AnomalyManager anomalyDao, final AuthorizationManager authorizationManager) {
    this.contributorsFinderRunner = contributorsFinderRunner;
    this.rcaInfoFetcher = rcaInfoFetcher;
    this.anomalyDao = anomalyDao;
    this.authorizationManager = authorizationManager;
  }

  private static List<List<String>> parseHierarchiesPayload(final String hierarchiesPayload)
      throws JsonProcessingException {
    return OBJECT_MAPPER.readValue(hierarchiesPayload, new TypeReference<>() {});
  }

  public DimensionAnalysisResultApi dataCubeSummary(final ThirdEyePrincipal principal, final long anomalyId,
      final String baselineOffset, final List<String> filters, final int summarySize,
      final int depth, final boolean doOneSideError, final List<String> dimensions,
      final List<String> excludedDimensions, final String hierarchiesPayload) throws Exception {
    final AnomalyDTO anomalyDto = ensureExists(anomalyDao.findById(anomalyId),
        String.format("Anomaly ID: %d", anomalyId));
    authorizationManager.ensureCanRead(principal, anomalyDto);
    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(anomalyDto);
    final Interval currentInterval = new Interval(rcaInfo.anomaly().getStartTime(),
        rcaInfo.anomaly().getEndTime(), rcaInfo.chronology());

    Period baselineOffsetPeriod = isoPeriod(baselineOffset);
    final Interval baselineInterval = new Interval(
        currentInterval.getStart().minus(baselineOffsetPeriod),
        currentInterval.getEnd().minus(baselineOffsetPeriod));

    // override dimensions
    final DatasetConfigDTO datasetConfigDTO = rcaInfo.dataset();
    List<String> rcaDimensions = getRcaDimensions(dimensions, excludedDimensions, datasetConfigDTO);
    datasetConfigDTO.setDimensions(Templatable.of(rcaDimensions));

    final List<List<String>> hierarchies = parseHierarchiesPayload(hierarchiesPayload);

    final ContributorsSearchConfiguration searchConfiguration = new ContributorsSearchConfiguration(
        rcaInfo.metric(), datasetConfigDTO, rcaInfo.dataSourceDto(), currentInterval, baselineInterval, summarySize,
        depth, doOneSideError, Predicate.parseAndCombinePredicates(filters), hierarchies);

    final ContributorsFinderResult result = contributorsFinderRunner.run(searchConfiguration);

    final DimensionAnalysisResultApi dimensionAnalysisResult = result.getDimensionAnalysisResult();
    if (dimensionAnalysisResult != null) {
      final TextualAnalysis textualAnalysis = optional(
          dimensionAnalysisResult.getTextualAnalysis()).orElse(new TextualAnalysis());
      final String currentText = optional(textualAnalysis.getText()).orElse("");
      final String anomalyDescriptionText = generateAnomalyDescriptionText(rcaInfo);
      textualAnalysis.setText(anomalyDescriptionText + currentText);
      dimensionAnalysisResult.setTextualAnalysis(textualAnalysis);
    }

    return dimensionAnalysisResult;
  }

  private String generateAnomalyDescriptionText(final RcaInfo rcaInfo) {
    final StringBuilder text = new StringBuilder();
    final double expected = rcaInfo.anomaly().getAvgBaselineVal();
    final double current = rcaInfo.anomaly().getAvgCurrentVal();
    final DateTimeFormatter timeFormatter = timeFormatterFor(rcaInfo.granularity(),
        rcaInfo.chronology());
    text.append("An anomaly was detected on ")
        .append(new DateTime(rcaInfo.anomaly().getStartTime(), rcaInfo.chronology()).toString(
            timeFormatter))
        .append(". ")
        // TODO prefer giving the expected range rather than the expected mean
        .append("The metric value was expected to be close to ")
        .append(DECIMAL_FORMAT.format(expected)) // TODO cyril ensure the decimal format should depend on the value
        .append(" but it is ")
        .append(DECIMAL_FORMAT.format(current));
    if (expected != 0) {
      text.append(": a ")
          .append(PERCENTAGE_FORMAT.format(100 * (current - expected) / expected))
          .append("% difference");
    }
    text.append(". \n");

    return text.toString();
  }
}
