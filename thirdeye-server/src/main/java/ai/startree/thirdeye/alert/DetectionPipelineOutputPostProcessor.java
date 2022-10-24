/*
 * Copyright 2022 StarTree Inc
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

package ai.startree.thirdeye.alert;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.detectionpipeline.operator.CombinerResult;
import ai.startree.thirdeye.spi.Constants;
import ai.startree.thirdeye.spi.api.AlertEvaluationApi;
import ai.startree.thirdeye.spi.dataframe.DataFrame;
import ai.startree.thirdeye.spi.dataframe.Series.LongConditional;
import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.detection.model.TimeSeries;
import ai.startree.thirdeye.spi.detection.v2.OperatorResult;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;

public class DetectionPipelineOutputPostProcessor {

  public Map<String, OperatorResult> process(final Map<String, OperatorResult> result,
      final AlertEvaluationApi request) {
    final Map<String, OperatorResult> m = new HashMap<>(result.size());
    for (final Entry<String, OperatorResult> e : result.entrySet()) {
      final OperatorResult processedOperatorResult = processOperatorResult(request,
          e.getValue());
      m.put(e.getKey(), processedOperatorResult);
    }
    return m;
  }

  private OperatorResult processOperatorResult(final AlertEvaluationApi request,
      final OperatorResult operatorResult) {
    if (operatorResult instanceof CombinerResult) {
      // process the combiner delegate results
      final Map<String, OperatorResult> postProcessResults = process(((CombinerResult) operatorResult).getResults(), request);
      return new CombinerResult(postProcessResults);
    }
    final Optional<DataFrame> dfOptional = optional(operatorResult)
        .map(OperatorResult::getTimeseries)
        .map(TimeSeries::getDataFrame);
    if (dfOptional.isPresent()) {
      return processOperatorResultWithDataframe(operatorResult, request);
    } else {
      return operatorResult;
    }
  }

  private OperatorResult processOperatorResultWithDataframe(final OperatorResult delegate,
      final AlertEvaluationApi request) {
    return new OperatorResult() {
      @Override
      public long getLastTimestamp() {
        return delegate.getLastTimestamp();
      }

      @Override
      public List<MergedAnomalyResultDTO> getAnomalies() {
        return delegate.getAnomalies();
      }

      @Override
      public @Nullable EnumerationItemDTO getEnumerationItem() {
        return delegate.getEnumerationItem();
      }

      @Override
      public @Nullable Map<String, List> getRawData() {
        return delegate.getRawData();
      }

      @Override
      public @Nullable TimeSeries getTimeseries() {
        return optional(delegate.getTimeseries())
            .map(ts -> filterStartEnd(ts, request))
            .orElse(null);
      }
    };
  }

  private TimeSeries filterStartEnd(final TimeSeries timeseries, final AlertEvaluationApi request) {
    return TimeSeries.fromDataFrame(filterStartEnd(timeseries.getDataFrame(),
        request.getStart().getTime(),
        request.getEnd().getTime()));
  }

  private DataFrame filterStartEnd(final DataFrame df, final long start, final long end) {
    return df
        .filter((LongConditional) v -> v[0] >= start && v[0] <= end, Constants.COL_TIME)
        .dropNull(Constants.COL_TIME);
  }
}
