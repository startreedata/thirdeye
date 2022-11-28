package ai.startree.thirdeye.spi.authorization;

public interface AccessControllable {

  String getName();

  default String getNamespace() {
    return "default";
  }

  default EntityType getEntityType() {
    return EntityType.Any;
  }
}
