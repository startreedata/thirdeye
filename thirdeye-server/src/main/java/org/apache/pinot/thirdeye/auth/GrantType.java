package org.apache.pinot.thirdeye.auth;

import com.google.common.collect.ImmutableMap;

public enum GrantType {

  /**
   * Sets the grant type ("authorization_code", "password", "client_credentials", "refresh_token"
   * or absolute URI of the extension grant type).
   * Overriding is only supported for the purpose of calling the super implementation and
   * changing the return type, but nothing else.
   */
  AUTHORIZATION_CODE("authorization_code"),
  PASSWORD("password"),
  REFRESH_TOKEN("refresh_token");

  final String value;

  GrantType(String value) {
    this.value = value;
  }

  private static final ImmutableMap<String, GrantType> valueMap;

  static {
    final ImmutableMap.Builder<String, GrantType> builder = new ImmutableMap.Builder<>();
    for (GrantType grantType : values()) {
      builder.put(grantType.value, grantType);
    }
    valueMap = builder.build();
  }

  public static GrantType fromValue(String value) {
    return valueMap.get(value);
  }

  public String getValue() {
    return value;
  }
}
