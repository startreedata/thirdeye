package ai.startree.thirdeye.spi.api;

import java.util.Map;

public class AnomalyLabelApi {
  private String name;
  private String sourcePostProcessor;
  private String sourceNodeName;
  private boolean ignore;
  private Map<String, Object> metadata;

  public String getName() {
    return name;
  }

  public AnomalyLabelApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getSourcePostProcessor() {
    return sourcePostProcessor;
  }

  public AnomalyLabelApi setSourcePostProcessor(final String sourcePostProcessor) {
    this.sourcePostProcessor = sourcePostProcessor;
    return this;
  }

  public String getSourceNodeName() {
    return sourceNodeName;
  }

  public AnomalyLabelApi setSourceNodeName(final String sourceNodeName) {
    this.sourceNodeName = sourceNodeName;
    return this;
  }

  public boolean isIgnore() {
    return ignore;
  }

  public AnomalyLabelApi setIgnore(final boolean ignore) {
    this.ignore = ignore;
    return this;
  }

  public Map<String, Object> getMetadata() {
    return metadata;
  }

  public AnomalyLabelApi setMetadata(final Map<String, Object> metadata) {
    this.metadata = metadata;
    return this;
  }
}
