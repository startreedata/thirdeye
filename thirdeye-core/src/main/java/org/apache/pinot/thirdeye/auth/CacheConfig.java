package org.apache.pinot.thirdeye.auth;

public class CacheConfig {

  public static final long DEFAULT_SIZE = 64;
  public static final long DEFAULT_TTL = 60000;
  private long size = DEFAULT_SIZE;
  private long ttl = DEFAULT_TTL;

  public long getSize() {
    return size;
  }

  public CacheConfig setSize(final long size) {
    this.size = size;
    return this;
  }

  public long getTtl() {
    return ttl;
  }

  public CacheConfig setTtl(final long ttl) {
    this.ttl = ttl;
    return this;
  }
}
