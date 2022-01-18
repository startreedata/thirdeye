package org.apache.pinot.thirdeye.spi.auth;

import org.apache.pinot.thirdeye.spi.api.AuthInfoApi;

public interface AuthManager {
  AuthInfoApi getInfo();
}
