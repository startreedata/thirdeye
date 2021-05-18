package org.apache.pinot.thirdeye.detection.v2;

import java.util.Map;


public interface Operator<T extends DetectionPipelineResult> {

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
