package ai.startree.thirdeye.spi.util;

import static ai.startree.thirdeye.spi.util.SpiUtils.optional;

import ai.startree.thirdeye.spi.datalayer.dto.AnomalyLabelDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;

public class AnomalyUtils {

  public static boolean isIgnore(final MergedAnomalyResultDTO parent) {
    return optional(parent.getAnomalyLabels())
        .map(labels -> labels.stream().anyMatch(AnomalyLabelDTO::isIgnore))
        .orElse(false);
  }
}
