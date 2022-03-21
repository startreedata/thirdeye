/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
