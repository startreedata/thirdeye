/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.spi.datalayer.DaoFilter;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.ApplicationManager;
import ai.startree.thirdeye.spi.datalayer.dto.ApplicationDTO;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestApplicationManager {

  public static final String APPLICATION_NAME = "MY_APP";
  public static final String APPLICATION_EMAIL = "abc@abc.in";
  private Long applicationId;

  private TestDatabase db;
  private ApplicationManager applicationManager;

  private static ApplicationDTO newApplication(final String name) {
    ApplicationDTO request = new ApplicationDTO();
    request.setApplication(name);
    request.setRecipients(APPLICATION_EMAIL);
    return request;
  }

  @BeforeClass
  void beforeClass() throws Exception {
    db = new TestDatabase();
    applicationManager = db.createInjector().getInstance(ApplicationManager.class);
  }

  @AfterClass(alwaysRun = true)
  void afterClass() {
    db.cleanup();
  }

  @Test
  public void testCreateApplication() {
    ApplicationDTO request = newApplication(APPLICATION_NAME);
    applicationId = applicationManager.save(request);
    assertThat(applicationId).isGreaterThan(0);
  }

  @Test(dependsOnMethods = {"testCreateApplication"})
  public void testFetchApplication() {
    // find by id
    ApplicationDTO response = applicationManager.findById(applicationId);

    assertThat(response).isNotNull();
    assertThat(response.getId()).isEqualTo(applicationId);
    assertThat(response.getApplication()).isEqualTo(APPLICATION_NAME);
    assertThat(response.getRecipients()).isEqualTo(APPLICATION_EMAIL);
  }

  @Test(dependsOnMethods = {"testCreateApplication"})
  public void testFilter() {
    assertThat(applicationManager.findAll().size())
        .isEqualTo(1);
    assertThat(applicationManager.filter(new DaoFilter()
        .setPredicate(Predicate.EQ("application", APPLICATION_NAME))).size())
        .isEqualTo(1);
    assertThat(applicationManager.filter(new DaoFilter()
        .setPredicate(Predicate.EQ("baseId", applicationId))).size())
        .isEqualTo(1);
    assertThat(applicationManager.filter(new DaoFilter()
        .setPredicate(Predicate.EQ("baseId", applicationId))).size())
        .isEqualTo(1);

    final Long id2 = applicationManager.save(newApplication("app2"));
    assertThat(applicationManager.findAll().size())
        .isEqualTo(2);

    assertThat(applicationManager.filter(new DaoFilter()
        .setPredicate(Predicate.GT("baseId", 0))
        .setLimit(1))
        .size())
        .isEqualTo(1);
    assertThat(id2).isGreaterThan(applicationId); // else the next assert will fail!
    assertThat(applicationManager.filter(new DaoFilter()
        .setPredicate(Predicate.GT("baseId", applicationId)))
        .size())
        .isEqualTo(1);
    applicationManager.deleteById(id2);
  }

  @Test(dependsOnMethods = {"testFetchApplication"})
  public void testDeleteApplication() {
    assertThat(applicationManager.findAll().size()).isEqualTo(1);
    applicationManager.deleteById(applicationId);
    assertEquals(applicationManager.findAll().size(), 0);
  }
}
