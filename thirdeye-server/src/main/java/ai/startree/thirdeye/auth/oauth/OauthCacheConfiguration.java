/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.auth.oauth;

public class OauthCacheConfiguration {

  public static final long DEFAULT_SIZE = 64;
  public static final long DEFAULT_TTL = 60000;
  private long size = DEFAULT_SIZE;
  private long ttl = DEFAULT_TTL;

  public long getSize() {
    return size;
  }

  public OauthCacheConfiguration setSize(final long size) {
    this.size = size;
    return this;
  }

  public long getTtl() {
    return ttl;
  }

  public OauthCacheConfiguration setTtl(final long ttl) {
    this.ttl = ttl;
    return this;
  }
}
