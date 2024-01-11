/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.notification;

import static ai.startree.thirdeye.datalayer.DatalayerTestUtils.collectIds;
import static ai.startree.thirdeye.spi.Constants.NOTIFICATION_ANOMALY_MAX_LOOKBACK_MS;
import static ai.startree.thirdeye.spi.util.SpiUtils.alertRef;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.bao.AnomalyManager;
import ai.startree.thirdeye.spi.datalayer.bao.SubscriptionGroupManager;
import ai.startree.thirdeye.spi.datalayer.dto.AlertAssociationDto;
import ai.startree.thirdeye.spi.datalayer.dto.AlertDTO;
import ai.startree.thirdeye.spi.datalayer.dto.AnomalyDTO;
import ai.startree.thirdeye.spi.datalayer.dto.SubscriptionGroupDTO;
import com.google.inject.Injector;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SubscriptionGroupFilterIntegrationTest {

  private static final String CRON = "0 0 * * * ? *";
  private static final long POINT_IN_TIME = System.currentTimeMillis();
  private AnomalyManager anomalyManager;
  private SubscriptionGroupManager subscriptionGroupManager;
  private SubscriptionGroupWatermarkManager watermarkManager;
  private SubscriptionGroupFilter instance;
  private AlertManager alertManager;

  private static AnomalyDTO anomalyWithCreateTime(final long createTime) {
    final AnomalyDTO anomaly = new AnomalyDTO().setChild(false);
    anomaly.setCreateTime(new Timestamp(createTime));
    return anomaly;
  }

  private static long minutesAgo(final long nMinutes) {
    return POINT_IN_TIME - nMinutes * 60 * 1000;
  }

  private static AlertAssociationDto aaRef(final Long id) {
    return new AlertAssociationDto().setAlert(alertRef(id));
  }

  /**
   * Persist an entity
   * TODO spyne: move persist to AbstractManager interface
   *
   * @param entity the entity to persist
   * @return the same entity
   */
  private SubscriptionGroupDTO persist(final SubscriptionGroupDTO entity) {
    final long id = subscriptionGroupManager.save(entity);
    assertThat(id).isNotNull();
    return entity;
  }

  private AnomalyDTO persist(final AnomalyDTO anomaly) {
    final long id = anomalyManager.save(anomaly);
    assertThat(id).isNotNull();

    return anomaly;
  }

  private AlertDTO persist(final AlertDTO alert) {
    final long id = alertManager.save(alert);
    assertThat(id).isNotNull();
    return alert;
  }

  @BeforeClass
  void beforeClass() {
    final Injector injector = MySqlTestDatabase.sharedInjector();
    anomalyManager = injector.getInstance(AnomalyManager.class);
    subscriptionGroupManager = injector.getInstance(SubscriptionGroupManager.class);
    watermarkManager = injector.getInstance(SubscriptionGroupWatermarkManager.class);
    alertManager = injector.getInstance(AlertManager.class);

    instance = injector.getInstance(SubscriptionGroupFilter.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
    alertManager.findAll().forEach(alertManager::delete);
    subscriptionGroupManager.findAll().forEach(subscriptionGroupManager::delete);
    anomalyManager.findAll().forEach(anomalyManager::delete);
  }
  @Test
  public void testFilter() {
    final AlertDTO alert = persist(new AlertDTO()
        .setName("alert1")
        .setActive(true));

    final SubscriptionGroupDTO sg = persist(new SubscriptionGroupDTO()
        .setName("name1")
        .setCronExpression(CRON)
    );

    // base case
    assertThat(instance.filter(sg, POINT_IN_TIME).isEmpty()).isTrue();

    final long superOldCreateTime = POINT_IN_TIME - NOTIFICATION_ANOMALY_MAX_LOOKBACK_MS - 100_000L;
    persist(anomalyWithCreateTime(superOldCreateTime)
        .setDetectionConfigId(alert.getId())
        .setStartTime(minutesAgo(100))
        .setEndTime(minutesAgo(80))
    );

    final AnomalyDTO anomaly1 = persist(anomalyWithCreateTime(minutesAgo(2))
        .setDetectionConfigId(alert.getId())
        .setStartTime(minutesAgo(100))
        .setEndTime(minutesAgo(80))
    );

    persist(sg.setAlertAssociations(List.of(aaRef(alert.getId()))));

    assertThat(collectIds(instance.filter(sg, POINT_IN_TIME)))
        .isEqualTo(collectIds(Set.of(anomaly1)));

    watermarkManager.updateWatermarks(sg, List.of(anomaly1));

    final AnomalyDTO anomaly2 = persist(anomalyWithCreateTime(minutesAgo(3))
        .setDetectionConfigId(alert.getId())
        .setStartTime(minutesAgo(100))
        .setEndTime(minutesAgo(80))
    );
    final AnomalyDTO anomaly3 = persist(anomalyWithCreateTime(minutesAgo(1))
        .setDetectionConfigId(alert.getId())
        .setStartTime(minutesAgo(100))
        .setEndTime(minutesAgo(80))
    );
    assertThat(collectIds(instance.filter(sg, POINT_IN_TIME)))
        .isEqualTo(collectIds(Set.of(anomaly3)));
  }
}