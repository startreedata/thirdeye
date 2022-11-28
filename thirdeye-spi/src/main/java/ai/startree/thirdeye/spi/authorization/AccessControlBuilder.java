package ai.startree.thirdeye.spi.authorization;

public class AccessControlBuilder {

  public static AccessController build() {
    return new AlwaysAllow();
  }
}
