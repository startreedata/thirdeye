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
package ai.startree.thirdeye.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityUtils {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityUtils.class);
  private static final String HMAC_SHA512 = "HmacSHA512";
  private static final String AUTH_TYPE = "Thirdeye-HMAC-SHA512";

  public static String hmacSHA512(Object entity, String key) {
    Mac sha512Hmac;
    final byte[] byteKey = key.getBytes(StandardCharsets.UTF_8);
    try {
      sha512Hmac = Mac.getInstance(HMAC_SHA512);
      SecretKeySpec keySpec = new SecretKeySpec(byteKey, HMAC_SHA512);
      sha512Hmac.init(keySpec);
      byte[] macData = sha512Hmac.doFinal(new ObjectMapper().writeValueAsBytes(entity));
      return String.format("%s %s", AUTH_TYPE, Base64.getEncoder().encodeToString(macData));
    } catch (NoSuchAlgorithmException | InvalidKeyException | JsonProcessingException e) {
      LOG.error("Signature generation failure!", e);
      return null;
    }
  }
}
