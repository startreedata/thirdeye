/**
 * Copyright (C) 2014-2018 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pinot.thirdeye.datalayer.bao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

import org.apache.pinot.thirdeye.datalayer.TestDatabase;
import org.apache.pinot.thirdeye.spi.datalayer.DaoFilter;
import org.apache.pinot.thirdeye.spi.datalayer.Predicate;
import org.apache.pinot.thirdeye.spi.datalayer.bao.ApplicationManager;
import org.apache.pinot.thirdeye.spi.datalayer.dto.ApplicationDTO;
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
