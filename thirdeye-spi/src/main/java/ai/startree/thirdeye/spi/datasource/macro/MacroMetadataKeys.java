package ai.startree.thirdeye.spi.datasource.macro;

/**
 * String keys to use by a macro function when it writes to the properties map.
 * */
public enum MacroMetadataKeys {
  TIME_COLUMN("metadata.timeColumn"),
  MIN_TIME_MILLIS("metadata.minTimeMillis"),
  MAX_TIME_MILLIS("metadata.maxTimeMillis"),
  GRANULARITY("metadata.granularity");

  private final String key;

  MacroMetadataKeys(String key) {
    this.key = key;
  }

  @Override
  public String toString() {
    return key;
  }
}
