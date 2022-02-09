package ai.startree.thirdeye.spi.detection.v2;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract DataTable with properties management implemented.
 */
public abstract class AbstractDataTableImpl implements DataTable {

  private final Map<String, String> properties = new HashMap<>();

  @Override
  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public void addProperties(final Map<String, String> metadata) {
    this.properties.putAll(metadata);
  }
}
