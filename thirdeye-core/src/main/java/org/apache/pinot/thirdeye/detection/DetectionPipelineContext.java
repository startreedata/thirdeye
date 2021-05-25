package org.apache.pinot.thirdeye.detection;

import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;

public class DetectionPipelineContext {

  private AlertDTO alert;
  private long start;
  private long end;

  public AlertDTO getAlert() {
    return alert;
  }

  public DetectionPipelineContext setAlert(final AlertDTO alert) {
    this.alert = alert;
    return this;
  }

  public long getStart() {
    return start;
  }

  public DetectionPipelineContext setStart(final long start) {
    this.start = start;
    return this;
  }

  public long getEnd() {
    return end;
  }

  public DetectionPipelineContext setEnd(final long end) {
    this.end = end;
    return this;
  }
}
