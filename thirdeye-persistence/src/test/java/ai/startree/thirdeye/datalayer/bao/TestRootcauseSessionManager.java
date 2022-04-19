/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.DatalayerTestUtils;
import ai.startree.thirdeye.datalayer.TestDatabase;
import ai.startree.thirdeye.spi.datalayer.bao.RootcauseSessionManager;
import ai.startree.thirdeye.spi.datalayer.dto.RootCauseSessionDTO;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestRootcauseSessionManager {

  private RootcauseSessionManager sessionDAO;

  private static RootCauseSessionDTO makeDefault() {
    return DatalayerTestUtils.getTestRootcauseSessionResult(1000,
        1100,
        1500,
        2000,
        "myname",
        "myowner",
        "mytext",
        "mygranularity",
        "mycomparemode",
        12345L,
        1234L);
  }

  private static RootCauseSessionDTO makeName(String name) {
    RootCauseSessionDTO session = makeDefault();
    session.setName(name);
    return session;
  }

  private static RootCauseSessionDTO makeOwner(String owner) {
    RootCauseSessionDTO session = makeDefault();
    session.setOwner(owner);
    return session;
  }

  private static RootCauseSessionDTO makeCreated(long created) {
    RootCauseSessionDTO session = makeDefault();
    session.setCreated(created);
    return session;
  }

  private static RootCauseSessionDTO makeUpdated(long updated) {
    RootCauseSessionDTO session = makeDefault();
    session.setUpdated(updated);
    return session;
  }

  private static RootCauseSessionDTO makeAnomaly(long anomalyId) {
    RootCauseSessionDTO session = makeDefault();
    session.setAnomalyId(anomalyId);
    return session;
  }

  private static RootCauseSessionDTO makeAnomalyRange(long start, long end) {
    RootCauseSessionDTO session = makeDefault();
    session.setAnomalyRangeStart(start);
    session.setAnomalyRangeEnd(end);
    return session;
  }

  private static RootCauseSessionDTO makePrevious(long previousId) {
    RootCauseSessionDTO session = makeDefault();
    session.setPreviousId(previousId);
    return session;
  }

  @BeforeMethod
  void beforeMethod() {
    sessionDAO = new TestDatabase().createInjector().getInstance(RootcauseSessionManager.class);
  }

  @Test
  public void testCreateSession() {
    this.sessionDAO.save(makeDefault());
  }

  @Test
  public void testUpdateSession() throws Exception {
    RootCauseSessionDTO session = makeDefault();
    this.sessionDAO.save(session);

    session.setName("mynewname");
    this.sessionDAO.save(session);

    RootCauseSessionDTO read = this.sessionDAO.findById(session.getId());

    Assert.assertEquals(read.getName(), "mynewname");
  }

  @Test
  public void testFindSessionById() {
    Long id = this.sessionDAO.save(makeDefault());

    RootCauseSessionDTO session = this.sessionDAO.findById(id);
    Assert.assertEquals(session.getAnomalyRangeStart(), (Long) 1000L);
    Assert.assertEquals(session.getAnomalyRangeEnd(), (Long) 1100L);
    Assert.assertEquals(session.getAnalysisRangeStart(), (Long) 900L);
    Assert.assertEquals(session.getAnalysisRangeEnd(), (Long) 1200L);
    Assert.assertEquals(session.getCreated(), (Long) 1500L);
    Assert.assertEquals(session.getName(), "myname");
    Assert.assertEquals(session.getOwner(), "myowner");
    Assert.assertEquals(session.getText(), "mytext");
    Assert.assertEquals(session.getGranularity(), "mygranularity");
    Assert.assertEquals(session.getCompareMode(), "mycomparemode");
    Assert.assertEquals(session.getPreviousId(), (Long) 12345L);
  }

  @Test
  public void testFindSessionByName() {
    this.sessionDAO.save(makeName("A"));
    this.sessionDAO.save(makeName("B"));
    this.sessionDAO.save(makeName("A"));

    List<RootCauseSessionDTO> sessionsA = this.sessionDAO.findByName("A");
    List<RootCauseSessionDTO> sessionsB = this.sessionDAO.findByName("B");
    List<RootCauseSessionDTO> sessionsC = this.sessionDAO.findByName("C");

    Assert.assertEquals(sessionsA.size(), 2);
    Assert.assertEquals(sessionsB.size(), 1);
    Assert.assertEquals(sessionsC.size(), 0);
  }

  @Test
  public void testFindSessionByNameLike() {
    this.sessionDAO.save(makeName("ABC"));
    this.sessionDAO.save(makeName("BDC"));
    this.sessionDAO.save(makeName("CB"));

    List<RootCauseSessionDTO> sessionsAB = this.sessionDAO
        .findByNameLike(new HashSet<>(Arrays.asList("A", "B")));
    List<RootCauseSessionDTO> sessionsBC = this.sessionDAO
        .findByNameLike(new HashSet<>(Arrays.asList("B", "C")));
    List<RootCauseSessionDTO> sessionsCD = this.sessionDAO
        .findByNameLike(new HashSet<>(Arrays.asList("C", "D")));
    List<RootCauseSessionDTO> sessionsABCD = this.sessionDAO
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

    List<RootCauseSessionDTO> sessionsX = this.sessionDAO.findByOwner("X");
    List<RootCauseSessionDTO> sessionsY = this.sessionDAO.findByOwner("Y");
    List<RootCauseSessionDTO> sessionsZ = this.sessionDAO.findByOwner("Z");

    Assert.assertEquals(sessionsX.size(), 1);
    Assert.assertEquals(sessionsY.size(), 2);
    Assert.assertEquals(sessionsZ.size(), 0);
  }

  @Test
  public void testFindByCreatedRange() {
    this.sessionDAO.save(makeCreated(800));
    this.sessionDAO.save(makeCreated(900));
    this.sessionDAO.save(makeCreated(1000));

    List<RootCauseSessionDTO> sessionsBefore = this.sessionDAO.findByCreatedRange(700, 800);
    List<RootCauseSessionDTO> sessionsMid = this.sessionDAO.findByCreatedRange(800, 1000);
    List<RootCauseSessionDTO> sessionsEnd = this.sessionDAO.findByCreatedRange(1000, 1500);

    Assert.assertEquals(sessionsBefore.size(), 0);
    Assert.assertEquals(sessionsMid.size(), 2);
    Assert.assertEquals(sessionsEnd.size(), 1);
  }

  @Test
  public void testFindByUpdatedRange() {
    this.sessionDAO.save(makeUpdated(800));
    this.sessionDAO.save(makeUpdated(900));
    this.sessionDAO.save(makeUpdated(1000));

    List<RootCauseSessionDTO> sessionsBefore = this.sessionDAO.findByUpdatedRange(700, 800);
    List<RootCauseSessionDTO> sessionsMid = this.sessionDAO.findByUpdatedRange(800, 1000);
    List<RootCauseSessionDTO> sessionsEnd = this.sessionDAO.findByUpdatedRange(1000, 1500);

    Assert.assertEquals(sessionsBefore.size(), 0);
    Assert.assertEquals(sessionsMid.size(), 2);
    Assert.assertEquals(sessionsEnd.size(), 1);
  }

  @Test
  public void testFindByAnomalyRange() {
    this.sessionDAO.save(makeAnomalyRange(1000, 1200));
    this.sessionDAO.save(makeAnomalyRange(1100, 1150));
    this.sessionDAO.save(makeAnomalyRange(1150, 1300));

    List<RootCauseSessionDTO> sessionsBefore = this.sessionDAO.findByAnomalyRange(0, 1000);
    List<RootCauseSessionDTO> sessionsMid = this.sessionDAO.findByAnomalyRange(1000, 1100);
    List<RootCauseSessionDTO> sessionsEnd = this.sessionDAO.findByAnomalyRange(1100, 1175);

    Assert.assertEquals(sessionsBefore.size(), 0);
    Assert.assertEquals(sessionsMid.size(), 1);
    Assert.assertEquals(sessionsEnd.size(), 3);
  }

  @Test
  public void testFindByPreviousId() {
    this.sessionDAO.save(makePrevious(0));
    this.sessionDAO.save(makePrevious(1));
    this.sessionDAO.save(makePrevious(1));
    this.sessionDAO.save(makePrevious(2));

    List<RootCauseSessionDTO> sessions0 = this.sessionDAO.findByPreviousId(0);
    List<RootCauseSessionDTO> sessions1 = this.sessionDAO.findByPreviousId(1);
    List<RootCauseSessionDTO> sessions2 = this.sessionDAO.findByPreviousId(2);
    List<RootCauseSessionDTO> sessions3 = this.sessionDAO.findByPreviousId(3);

    Assert.assertEquals(sessions0.size(), 1);
    Assert.assertEquals(sessions1.size(), 2);
    Assert.assertEquals(sessions2.size(), 1);
    Assert.assertEquals(sessions3.size(), 0);
  }

  @Test
  public void testFindByAnomalyId() {
    this.sessionDAO.save(makeAnomaly(0));
    this.sessionDAO.save(makeAnomaly(1));
    this.sessionDAO.save(makeAnomaly(1));
    this.sessionDAO.save(makeAnomaly(2));

    List<RootCauseSessionDTO> sessions0 = this.sessionDAO.findByAnomalyId(0);
    List<RootCauseSessionDTO> sessions1 = this.sessionDAO.findByAnomalyId(1);
    List<RootCauseSessionDTO> sessions2 = this.sessionDAO.findByAnomalyId(2);
    List<RootCauseSessionDTO> sessions3 = this.sessionDAO.findByAnomalyId(3);

    Assert.assertEquals(sessions0.size(), 1);
    Assert.assertEquals(sessions1.size(), 2);
    Assert.assertEquals(sessions2.size(), 1);
    Assert.assertEquals(sessions3.size(), 0);
  }
}
