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

/*
  Constants used for centralized-cache related code.
 */
public class CacheConstants {

  // couchbase document field names
  public static final String BUCKET = "bucket";

  // metricId
  public static final String METRIC_ID = "metricId";

  // used to designate start and end timestamps for Couchbase query
  public static final String START = "start";
  public static final String END = "end";

  // refers to hashed metricURN which is used as the key to data for a dimension combination.
  public static final String DIMENSION_KEY = "dimensionKey";
}
