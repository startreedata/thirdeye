package ai.startree.thirdeye.spi.datasource.loader;

import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.Interval;

public interface MinMaxTimeLoader {

  @Nullable Long fetchMinTime(final DatasetConfigDTO datasetConfigDTO,
      final @Nullable Interval timeFilterInterval) throws Exception;

  @Nullable Long fetchMaxTime(final DatasetConfigDTO datasetConfigDTO,
      final @Nullable Interval timeFilterInterval) throws Exception;
}
