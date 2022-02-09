/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.startree.thirdeye.detection.cache;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Config file for cache-related stuff.
 * Mapped from cache-config.yml
 */
public class CacheConfig {

  public static CacheConfig INSTANCE = new CacheConfig();

  @JsonProperty("centralizedCacheSettings")
  private CentralizedCacheConfig centralizedCacheConfig = new CentralizedCacheConfig();

  private boolean useInMemoryCache = true;
  private boolean useCentralizedCache = false;

  /**
   * Deprecated in favor of using an injected instance. This will be removed.
   *
   * @return singleton instance of {@link CacheConfig}
   */
  @Deprecated
  public static CacheConfig getInstance() {
    return INSTANCE;
  }

  public static void setINSTANCE(final CacheConfig INSTANCE) {
    CacheConfig.INSTANCE = INSTANCE;
  }

  public boolean useCentralizedCache() {
    return useCentralizedCache;
  }

  public boolean useInMemoryCache() {
    return useInMemoryCache;
  }

  public CentralizedCacheConfig getCentralizedCacheConfig() {
    return centralizedCacheConfig;
  }

  public CacheConfig setCentralizedCacheConfig(CentralizedCacheConfig centralizedCacheConfig) {
    this.centralizedCacheConfig = centralizedCacheConfig;
    return this;
  }

  public CacheConfig setUseCentralizedCache(boolean useCentralizedCache) {
    this.useCentralizedCache = useCentralizedCache;
    return this;
  }

  public CacheConfig setUseInMemoryCache(boolean useInMemoryCache) {
    this.useInMemoryCache = useInMemoryCache;
    return this;
  }
}
