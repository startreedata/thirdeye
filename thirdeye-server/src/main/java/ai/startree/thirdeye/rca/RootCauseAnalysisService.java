/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.rca;

import ai.startree.thirdeye.rootcause.RCAFramework;
import ai.startree.thirdeye.rootcause.impl.RCAConfiguration;
import ai.startree.thirdeye.rootcause.impl.RCAFrameworkLoader;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RootCauseAnalysisService {

  private static final Logger LOG = LoggerFactory.getLogger(RootCauseAnalysisService.class);

  private final RCAFrameworkLoader rcaFrameworkLoader;
  private final List<RootCauseEntityFormatter> formatters;
  private final Map<String, RCAFramework> frameworks;

  @Inject
  public RootCauseAnalysisService(
      final RCAConfiguration configuration,
      final RCAFrameworkLoader rcaFrameworkLoader) {
    this.rcaFrameworkLoader = rcaFrameworkLoader;
    formatters = createFormatters(configuration);
    frameworks = createRcaFrameworkMap();
  }

  private static List<RootCauseEntityFormatter> createFormatters(
      RCAConfiguration config) {
    List<RootCauseEntityFormatter> formatters = new ArrayList<>();
    if (config.getFormatters() != null) {
      for (String className : config.getFormatters()) {
        try {
          formatters.add(FormatterLoader.fromClassName(className));
        } catch (ClassNotFoundException e) {
          LOG.warn("Could not find formatter class '{}'. Skipping.", className, e);
        } catch (Exception e) {
          LOG.error(String.format("Error initializing RCA formatter: %s", className), e);
        }
      }
    }
    formatters.add(new DefaultEntityFormatter());
    return formatters;
  }

  private Map<String, RCAFramework> createRcaFrameworkMap() {
    return rcaFrameworkLoader.getFrameworksFromConfig();
  }

  public List<RootCauseEntityFormatter> getFormatters() {
    return formatters;
  }

  public Map<String, RCAFramework> getFrameworks() {
    return frameworks;
  }
}
