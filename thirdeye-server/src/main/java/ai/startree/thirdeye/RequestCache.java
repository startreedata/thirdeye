/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye;

import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.function.Function;

/**
 * This class is a container for caching dtos for the life of a single request. The intention of
 * this class is to not support use cases where a cache is persisted across multiple requests.
 */
public class RequestCache {

  private LoadingCache<Long, AlertDTO> alerts;

  public static <T> LoadingCache<Long, T> buildCache(final Function<Long, T> function) {
    return CacheBuilder.newBuilder().build(createCacheLoader(function));
  }

  private static <T> CacheLoader<Long, T> createCacheLoader(final Function<Long, T> function) {
    return new CacheLoader<>() {
      @Override
      public T load(final Long id) {
        return function.apply(id);
      }
    };
  }

  public LoadingCache<Long, AlertDTO> getAlerts() {
    return alerts;
  }

  public RequestCache setAlerts(
      final LoadingCache<Long, AlertDTO> alerts) {
    this.alerts = alerts;
    return this;
  }
}
