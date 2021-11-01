package org.apache.pinot.thirdeye.auth;

public class CacheConfig {

  private long size = 64;
  private long ttl = 60000;

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
