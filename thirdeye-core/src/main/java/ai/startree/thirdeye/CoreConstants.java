/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye;

import java.util.concurrent.TimeUnit;

public interface CoreConstants {

  String TWO_DECIMALS_FORMAT = "#,###.##";
  String MAX_DECIMALS_FORMAT = "#,###.#####";
  String DECIMALS_FORMAT_TOKEN = "#";
  String PROP_DETECTOR_COMPONENT_NAME_DELIMETER = ",";

  // disable minute level cache warm up
  long DETECTION_TASK_MAX_LOOKBACK_WINDOW = TimeUnit.DAYS.toMillis(7);

  // default onboarding replay period
  // todo cyril this is not used - alert is replayed from epoch 0
  // also this should depend on the granularity
  long ONBOARDING_REPLAY_LOOKBACK = TimeUnit.DAYS.toMillis(60);
}
