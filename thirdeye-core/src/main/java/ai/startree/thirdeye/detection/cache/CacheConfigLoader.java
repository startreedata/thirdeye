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
package ai.startree.thirdeye.detection.cache;

import java.lang.reflect.Constructor;

/**
 * Helper methods to load cache config
 */
public class CacheConfigLoader {

  public static CacheDAO loadCacheDAO(CacheConfig config) throws Exception {
    String className = config.getCentralizedCacheConfig().getDataSourceConfig().getClassName();
    Constructor<?> constructor = Class.forName(className).getConstructor();
    return (CacheDAO) constructor.newInstance();
  }
}
