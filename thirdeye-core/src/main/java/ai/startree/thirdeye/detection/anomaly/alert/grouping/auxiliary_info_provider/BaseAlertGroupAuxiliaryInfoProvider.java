/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.detection.anomaly.alert.grouping.auxiliary_info_provider;

import java.util.Collections;
import java.util.Map;

public abstract class BaseAlertGroupAuxiliaryInfoProvider implements
    AlertGroupAuxiliaryInfoProvider {

  public static AuxiliaryAlertGroupInfo EMPTY_AUXILIARY_ALERT_GROUP_INFO = new AuxiliaryAlertGroupInfo();
  public static String RECIPIENTS_SEPARATOR = ",";

  Map<String, String> props = Collections.emptyMap();

  @Override
  public void setParameters(Map<String, String> props) {
    this.props = props;
  }
}
