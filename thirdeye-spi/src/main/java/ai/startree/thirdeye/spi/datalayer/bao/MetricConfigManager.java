/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import java.util.List;
import java.util.Set;

public interface MetricConfigManager extends AbstractManager<MetricConfigDTO> {

  List<MetricConfigDTO> findByDataset(String dataset);

  MetricConfigDTO findByMetricAndDataset(String metricName, String dataset);

  List<MetricConfigDTO> findActiveByDataset(String dataset);

  List<MetricConfigDTO> findWhereNameOrAliasLikeAndActive(String name);

  List<MetricConfigDTO> findWhereAliasLikeAndActive(Set<String> aliasParts);

  List<MetricConfigDTO> findByMetricName(String metricName);
}
