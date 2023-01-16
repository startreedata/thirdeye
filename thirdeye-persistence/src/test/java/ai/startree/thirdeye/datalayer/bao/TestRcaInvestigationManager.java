/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.MySqlTestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.RcaInvestigationManager;
import ai.startree.thirdeye.spi.datalayer.dto.MergedAnomalyResultDTO;
import ai.startree.thirdeye.spi.datalayer.dto.RcaInvestigationDTO;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TestRcaInvestigationManager {

  public static final String INVESTIGATION_NAME = "testInvestigation";
  public static final String INVESTIGATION_DESCRIPTION = "this is a test investigation";
  public static final Map<String, Object> INVESTIGATION_UI_METADATA = Map.of("k1", "v1");
  public static final Long INVESTIGATION_ANOMALY_ID = 123L;
  private RcaInvestigationManager sessionDAO;

  private static RcaInvestigationDTO makeDefault() {
    RcaInvestigationDTO investigationDTO = new RcaInvestigationDTO();
    investigationDTO.setName(INVESTIGATION_NAME);
    investigationDTO.setText(INVESTIGATION_DESCRIPTION);
    investigationDTO.setAnomaly((MergedAnomalyResultDTO) new MergedAnomalyResultDTO().setId(
        INVESTIGATION_ANOMALY_ID));
    investigationDTO.setUiMetadata(INVESTIGATION_UI_METADATA);
    return investigationDTO;
  }

  private static RcaInvestigationDTO makeName(String name) {
    RcaInvestigationDTO session = makeDefault();
    session.setName(name);
    return session;
  }

  private static RcaInvestigationDTO makeOwner(String owner) {
    RcaInvestigationDTO session = makeDefault();
    session.setCreatedBy(owner);
    return session;
  }

  private static RcaInvestigationDTO makeCreated(long created) {
    RcaInvestigationDTO session = makeDefault();
    session.setCreateTime(new Timestamp(created));
    return session;
  }

  private static RcaInvestigationDTO makeUpdated(long updated) {
    RcaInvestigationDTO session = makeDefault();
    session.setUpdateTime(new Timestamp(updated));
    return session;
  }

  private static RcaInvestigationDTO makeAnomaly(long anomalyId) {
    RcaInvestigationDTO session = makeDefault();
    session.setAnomaly((MergedAnomalyResultDTO) new MergedAnomalyResultDTO().setId(anomalyId));
    return session;
  }

  private static RcaInvestigationDTO makeAnomalyRange(long start, long end) {
    RcaInvestigationDTO session = makeDefault();
    session.getAnomaly().setStartTime(start);
    session.getAnomaly().setEndTime(end);
    return session;
  }

  @BeforeClass
  void beforeClass() {
    sessionDAO = MySqlTestDatabase.sharedInjector().getInstance(RcaInvestigationManager.class);
  }

  @AfterMethod
  void cleanCreatedEntities() {
    sessionDAO.findAll().forEach(sessionDAO::delete);
  }

  @Test
  public void testCreateSession() {
    this.sessionDAO.save(makeDefault());
  }

  @Test
  public void testUpdateSession() {
    RcaInvestigationDTO session = makeDefault();
    this.sessionDAO.save(session);

    session.setName("mynewname");
    this.sessionDAO.save(session);

    RcaInvestigationDTO read = this.sessionDAO.findById(session.getId());

    Assert.assertEquals(read.getName(), "mynewname");
  }

  @Test
  public void testFindSessionById() {
    Long id = this.sessionDAO.save(makeDefault());

    RcaInvestigationDTO session = this.sessionDAO.findById(id);
    Assert.assertEquals(session.getName(), INVESTIGATION_NAME);
    Assert.assertEquals(session.getText(), INVESTIGATION_DESCRIPTION);
    Assert.assertEquals(session.getUiMetadata(), INVESTIGATION_UI_METADATA);
    Assert.assertEquals(session.getAnomaly().getId(), INVESTIGATION_ANOMALY_ID);
  }

  @Test
  public void testFindSessionByName() {
    this.sessionDAO.save(makeName("A"));
    this.sessionDAO.save(makeName("B"));
    this.sessionDAO.save(makeName("A"));

    List<RcaInvestigationDTO> sessionsA = this.sessionDAO.findByName("A");
    List<RcaInvestigationDTO> sessionsB = this.sessionDAO.findByName("B");
    List<RcaInvestigationDTO> sessionsC = this.sessionDAO.findByName("C");

    Assert.assertEquals(sessionsA.size(), 2);
    Assert.assertEquals(sessionsB.size(), 1);
    Assert.assertEquals(sessionsC.size(), 0);
  }

  @Test
  public void testFindSessionByNameLike() {
    this.sessionDAO.save(makeName("ABC"));
    this.sessionDAO.save(makeName("BDC"));
    this.sessionDAO.save(makeName("CB"));

    List<RcaInvestigationDTO> sessionsAB = this.sessionDAO
        .findByNameLike(new HashSet<>(Arrays.asList("A", "B")));
    List<RcaInvestigationDTO> sessionsBC = this.sessionDAO
        .findByNameLike(new HashSet<>(Arrays.asList("B", "C")));
    List<RcaInvestigationDTO> sessionsCD = this.sessionDAO
        .findByNameLike(new HashSet<>(Arrays.asList("C", "D")));
    List<RcaInvestigationDTO> sessionsABCD = this.sessionDAO
        .findByNameLike(new HashSet<>(Arrays.asList("A", "B", "C", "D")));

    Assert.assertEquals(sessionsAB.size(), 1);
    Assert.assertEquals(sessionsBC.size(), 3);
    Assert.assertEquals(sessionsCD.size(), 1);
    Assert.assertEquals(sessionsABCD.size(), 0);
  }

  @Test
  public void testFindSessionByOwner() {
    this.sessionDAO.save(makeOwner("X"));
    this.sessionDAO.save(makeOwner("Y"));
    this.sessionDAO.save(makeOwner("Y"));

    List<RcaInvestigationDTO> sessionsX = this.sessionDAO.findByOwner("X");
    List<RcaInvestigationDTO> sessionsY = this.sessionDAO.findByOwner("Y");
    List<RcaInvestigationDTO> sessionsZ = this.sessionDAO.findByOwner("Z");

    Assert.assertEquals(sessionsX.size(), 1);
    Assert.assertEquals(sessionsY.size(), 2);
    Assert.assertEquals(sessionsZ.size(), 0);
  }

  @Test
  public void testFindByCreatedRange() {
    this.sessionDAO.save(makeCreated(800));
    this.sessionDAO.save(makeCreated(900));
    this.sessionDAO.save(makeCreated(1000));

    List<RcaInvestigationDTO> sessionsBefore = this.sessionDAO.findByCreatedRange(700, 800);
    List<RcaInvestigationDTO> sessionsMid = this.sessionDAO.findByCreatedRange(800, 1000);
    List<RcaInvestigationDTO> sessionsEnd = this.sessionDAO.findByCreatedRange(1000, 1500);

    Assert.assertEquals(sessionsBefore.size(), 0);
    Assert.assertEquals(sessionsMid.size(), 2);
    Assert.assertEquals(sessionsEnd.size(), 1);
  }

  @Test
  public void testFindByUpdatedRange() {
    this.sessionDAO.save(makeUpdated(800));
    this.sessionDAO.save(makeUpdated(900));
    this.sessionDAO.save(makeUpdated(1000));

    List<RcaInvestigationDTO> sessionsBefore = this.sessionDAO.findByUpdatedRange(700, 800);
    List<RcaInvestigationDTO> sessionsMid = this.sessionDAO.findByUpdatedRange(800, 1000);
    List<RcaInvestigationDTO> sessionsEnd = this.sessionDAO.findByUpdatedRange(1000, 1500);

    Assert.assertEquals(sessionsBefore.size(), 0);
    Assert.assertEquals(sessionsMid.size(), 2);
    Assert.assertEquals(sessionsEnd.size(), 1);
  }

  @Test
  public void testFindByAnomalyRange() {
    this.sessionDAO.save(makeAnomalyRange(1000, 1200));
    this.sessionDAO.save(makeAnomalyRange(1100, 1150));
    this.sessionDAO.save(makeAnomalyRange(1150, 1300));

    List<RcaInvestigationDTO> sessionsBefore = this.sessionDAO.findByAnomalyRange(0, 1000);
    List<RcaInvestigationDTO> sessionsMid = this.sessionDAO.findByAnomalyRange(1000, 1100);
    List<RcaInvestigationDTO> sessionsEnd = this.sessionDAO.findByAnomalyRange(1100, 1175);

    Assert.assertEquals(sessionsBefore.size(), 0);
    Assert.assertEquals(sessionsMid.size(), 1);
    Assert.assertEquals(sessionsEnd.size(), 3);
  }

  @Test
  public void testFindByAnomalyId() {
    this.sessionDAO.save(makeAnomaly(0));
    this.sessionDAO.save(makeAnomaly(1));
    this.sessionDAO.save(makeAnomaly(1));
    this.sessionDAO.save(makeAnomaly(2));

    List<RcaInvestigationDTO> sessions0 = this.sessionDAO.findByAnomalyId(0);
    List<RcaInvestigationDTO> sessions1 = this.sessionDAO.findByAnomalyId(1);
    List<RcaInvestigationDTO> sessions2 = this.sessionDAO.findByAnomalyId(2);
    List<RcaInvestigationDTO> sessions3 = this.sessionDAO.findByAnomalyId(3);

    Assert.assertEquals(sessions0.size(), 1);
    Assert.assertEquals(sessions1.size(), 2);
    Assert.assertEquals(sessions2.size(), 1);
    Assert.assertEquals(sessions3.size(), 0);
  }
}
