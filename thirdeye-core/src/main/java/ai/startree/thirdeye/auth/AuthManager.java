package ai.startree.thirdeye.auth;

import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import com.google.common.cache.LoadingCache;

public interface AuthManager {

  LoadingCache<String, ThirdEyePrincipal> getDefaultCache();

}
