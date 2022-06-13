/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.OnboardDatasetMetricDTO;
import java.util.List;

public interface OnboardDatasetMetricManager extends AbstractManager<OnboardDatasetMetricDTO> {

  List<OnboardDatasetMetricDTO> findByDataSource(String dataSource);

  List<OnboardDatasetMetricDTO> findByDataSourceAndOnboarded(String dataSource, boolean onboarded);

  List<OnboardDatasetMetricDTO> findByDataset(String datasetName);

  List<OnboardDatasetMetricDTO> findByMetric(String metricName);

  List<OnboardDatasetMetricDTO> findByDatasetAndDatasource(String datasetName, String dataSource);
}
