package org.apache.pinot.thirdeye.detection.v2.results;

import org.apache.pinot.thirdeye.datasource.pinot.resultset.ThirdEyeResultSetGroup;
import org.apache.pinot.thirdeye.detection.v2.DetectionPipelineResult;

public class DataTable implements DetectionPipelineResult {

  private final ThirdEyeResultSetGroup thirdEyeResultSetGroup;

  public DataTable(final ThirdEyeResultSetGroup thirdEyeResultSetGroup) {
    this.thirdEyeResultSetGroup = thirdEyeResultSetGroup;
  }

  public ThirdEyeResultSetGroup getThirdEyeResultSetGroup() {
    return thirdEyeResultSetGroup;
  }
}
