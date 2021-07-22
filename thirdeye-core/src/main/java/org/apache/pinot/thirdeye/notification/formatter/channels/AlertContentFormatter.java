package org.apache.pinot.thirdeye.notification.formatter.channels;

import java.util.Properties;
import org.apache.pinot.thirdeye.config.ThirdEyeCoordinatorConfiguration;
import org.apache.pinot.thirdeye.notification.content.BaseNotificationContent;
import org.apache.pinot.thirdeye.spi.Constants.SubjectType;
import org.apache.pinot.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;

/**
 * This generic class is responsible for formatting the contents across notification channels
 */
public abstract class AlertContentFormatter {

  protected static final String PROP_SUBJECT_STYLE = "subject";

  protected Properties alertClientConfig;
  protected SubscriptionGroupDTO subsConfig;
  protected ThirdEyeCoordinatorConfiguration teConfig;
  protected BaseNotificationContent notificationContent;

  public AlertContentFormatter(final Properties alertClientConfig,
      final BaseNotificationContent content,
      final ThirdEyeCoordinatorConfiguration teConfig,
      final SubscriptionGroupDTO subsConfig) {
    this.alertClientConfig = alertClientConfig;
    this.teConfig = teConfig;
    notificationContent = content;
    this.subsConfig = subsConfig;

    notificationContent.init(alertClientConfig, teConfig);
  }

  /**
   * Plug the appropriate subject style based on configuration
   */
  SubjectType getSubjectType(final Properties alertSchemeClientConfigs) {
    final SubjectType subjectType;
    if (alertSchemeClientConfigs != null && alertSchemeClientConfigs
        .containsKey(PROP_SUBJECT_STYLE)) {
      subjectType = SubjectType
          .valueOf(alertSchemeClientConfigs.get(PROP_SUBJECT_STYLE).toString());
    } else {
      // To support the legacy email subject configuration
      subjectType = subsConfig.getSubjectType();
    }

    return subjectType;
  }
}
