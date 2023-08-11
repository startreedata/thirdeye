package ai.startree.thirdeye.spi.api;

import jdk.jfr.Experimental;

/**
 * Placeholder for text analysis.
 * Experimental: the API will change.
 * */
@Experimental
public class TextualAnalysis {
  private String text;

  public String getText() {
    return text;
  }

  public TextualAnalysis setText(final String text) {
    this.text = text;
    return this;
  }
}
