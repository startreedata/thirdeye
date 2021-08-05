package org.apache.pinot.thirdeye.notification.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.MoreObjects;
import java.util.List;
import org.apache.pinot.thirdeye.detection.alert.DetectionAlertFilterResult;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent.AnomalyReportEntity;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class WebhookEntity {
  private String subscriptionGroup;
  private List<AnomalyReportEntity> result;

  public String getSubscriptionGroup() {
    return subscriptionGroup;
  }

  public WebhookEntity setSubscriptionGroup(final String subscriptionGroup) {
    this.subscriptionGroup = subscriptionGroup;
    return this;
  }

  public List<AnomalyReportEntity> getResult() {
    return result;
  }

  public WebhookEntity setResult(
      final List<AnomalyReportEntity> result) {
    for(AnomalyReportEntity res : result){
      if(res.getAnomalyURL().matches(".*/")){
        res.setAnomalyURL(res.getAnomalyURL()+res.getAnomalyId());
      }
    }
    this.result = result;
    return this;
  }

  @Override
  public String toString() {
    try {
      return  (new ObjectMapper()).writeValueAsString(this);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return "";
  }
}
