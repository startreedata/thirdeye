/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.detection.v2;

import java.util.Map;


public interface Operator {

  void init(OperatorContext context);

  void execute()
      throws Exception;

  String getOperatorName();

  void setProperty(String key, Object value);


  /**
   * Set keyed input
   * @param key
   * @param input
   */
  void setInput(String key, DetectionPipelineResult input);

  /**
   * Get keyed output
   * @param key
   */
  DetectionPipelineResult getOutput(String key);

  /**
   * Get all keyed outputs
   */
  Map<String, DetectionPipelineResult> getOutputs();
}
