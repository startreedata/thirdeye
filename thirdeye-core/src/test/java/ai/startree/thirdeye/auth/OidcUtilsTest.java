package org.apache.pinot.thirdeye.auth;

import static org.apache.pinot.thirdeye.auth.AuthTestUtils.getJWK;
import static org.apache.pinot.thirdeye.auth.OidcUtils.fetchKeys;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.nimbusds.jose.jwk.JWKSet;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class OidcUtilsTest {

  private static final String KID = RandomStringUtils.randomAlphanumeric(16);
  private static final String FILENAME = RandomStringUtils.randomAlphabetic(8);
  private static final String DIR = "authtest";

  private File dir;

  @BeforeClass
  private void before(){
    dir = new File(DIR);
    dir.mkdir();
  }

  @AfterClass
  private void after(){
    Arrays.stream(dir.listFiles()).forEach(file -> file.delete());
    dir.delete();
  }

  @Test
  public void fetchKeysTest() throws Exception {
    JWKSet keySet = new JWKSet(getJWK(KID));
    assertNotNull(keySet);
    assertTrue(dir.exists());
    FileWriter jwkFileWriter = new FileWriter(String.format("%s/%s.json", DIR, FILENAME));
    jwkFileWriter.write(keySet.toString());
    jwkFileWriter.close();
    JWKSet actualKeySet = fetchKeys(String.format("file://%s/%s.json", dir.getAbsolutePath(), FILENAME));
    assertEquals(keySet.toString(), actualKeySet.toString());
  }
}
