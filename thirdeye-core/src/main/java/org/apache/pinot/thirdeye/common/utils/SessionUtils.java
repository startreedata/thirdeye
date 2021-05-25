package org.apache.pinot.thirdeye.common.utils;

import org.apache.pinot.thirdeye.spi.datalayer.dto.SessionDTO;
import org.apache.pinot.thirdeye.spi.datalayer.pojo.SessionBean;

public class SessionUtils {

  public static SessionDTO buildServiceAccount(String user, String sessionKey,
      long expiryInMillis) {
    SessionDTO sessionDTO = new SessionDTO();
    sessionDTO.setPrincipal(user);
    sessionDTO.setPrincipalType(SessionBean.PrincipalType.SERVICE);
    sessionDTO.setSessionKey(sessionKey);
    sessionDTO.setExpirationTime(expiryInMillis);
    return sessionDTO;
  }
}
