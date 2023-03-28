/*
 * Copyright 2023 StarTree Inc
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
import static ai.startree.thirdeye.spi.util.TimeUtils.isoPeriod;

import ai.startree.thirdeye.rca.RcaInfo;
import ai.startree.thirdeye.rca.RcaInfoFetcher;
import ai.startree.thirdeye.rootcause.ContributorsFinderRunner;
import ai.startree.thirdeye.spi.api.DimensionAnalysisResultApi;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Templatable;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.rca.ContributorsFinderResult;
import ai.startree.thirdeye.spi.rca.ContributorsSearchConfiguration;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.joda.time.Interval;
import org.joda.time.Period;

@Singleton
public class RcaDimensionAnalysisService {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private final ContributorsFinderRunner contributorsFinderRunner;
  private final RcaInfoFetcher rcaInfoFetcher;

  @Inject
  public RcaDimensionAnalysisService(final ContributorsFinderRunner contributorsFinderRunner,
      final RcaInfoFetcher rcaInfoFetcher) {
    this.contributorsFinderRunner = contributorsFinderRunner;
    this.rcaInfoFetcher = rcaInfoFetcher;
  }

  private static List<List<String>> parseHierarchiesPayload(final String hierarchiesPayload)
      throws JsonProcessingException {
    return OBJECT_MAPPER.readValue(hierarchiesPayload, new TypeReference<>() {});
  }

  public DimensionAnalysisResultApi dataCubeSummary(final long anomalyId,
      final String baselineOffset, final List<String> filters, final int summarySize,
      final int depth, final boolean doOneSideError, final List<String> dimensions,
      final List<String> excludedDimensions, final String hierarchiesPayload) throws Exception {
    final RcaInfo rcaInfo = rcaInfoFetcher.getRcaInfo(
        anomalyId);
    final Interval currentInterval = new Interval(
        rcaInfo.getAnomaly().getStartTime(),
        rcaInfo.getAnomaly().getEndTime(),
        rcaInfo.getChronology());

    Period baselineOffsetPeriod = isoPeriod(baselineOffset);
    final Interval baselineInterval = new Interval(
        currentInterval.getStart().minus(baselineOffsetPeriod),
        currentInterval.getEnd().minus(baselineOffsetPeriod)
    );

    // override dimensions
    final DatasetConfigDTO datasetConfigDTO = rcaInfo.getDataset();
    List<String> rcaDimensions = getRcaDimensions(dimensions,
        excludedDimensions,
        datasetConfigDTO);
    datasetConfigDTO.setDimensions(Templatable.of(rcaDimensions));

    final List<List<String>> hierarchies = parseHierarchiesPayload(hierarchiesPayload);

    final ContributorsSearchConfiguration searchConfiguration = new ContributorsSearchConfiguration(
        rcaInfo.getMetric(),
        datasetConfigDTO,
        currentInterval,
        baselineInterval,
        summarySize,
        depth,
        doOneSideError,
        Predicate.parseAndCombinePredicates(filters),
        hierarchies);

    final ContributorsFinderResult result = contributorsFinderRunner.run(searchConfiguration);

    return result.getDimensionAnalysisResult();
  }
}
