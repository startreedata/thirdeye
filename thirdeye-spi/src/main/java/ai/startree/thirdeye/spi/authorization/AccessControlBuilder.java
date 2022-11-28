package ai.startree.thirdeye.spi.authorization;

public class AccessControlBuilder {

  public static AccessControl build() {
    return new AlwaysAllow();
  }
}
