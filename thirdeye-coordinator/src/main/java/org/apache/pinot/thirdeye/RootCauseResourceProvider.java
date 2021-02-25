package org.apache.pinot.thirdeye;


import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.pinot.thirdeye.datalayer.bao.MergedAnomalyResultManager;
import org.apache.pinot.thirdeye.rca.DataCubeSummaryCalculator;
import org.apache.pinot.thirdeye.rca.DefaultEntityFormatter;
import org.apache.pinot.thirdeye.rca.FormatterLoader;
import org.apache.pinot.thirdeye.rca.RootCauseEntityFormatter;
import org.apache.pinot.thirdeye.resources.RootCauseResource;
import org.apache.pinot.thirdeye.rootcause.RCAFramework;
import org.apache.pinot.thirdeye.rootcause.impl.RCAConfiguration;
import org.apache.pinot.thirdeye.rootcause.impl.RCAFrameworkLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootCauseResourceProvider implements Provider<RootCauseResource> {

  private static final Logger LOG = LoggerFactory.getLogger(RootCauseResourceProvider.class);
  private final ThirdEyeCoordinatorConfiguration config;

  @Inject
  private MergedAnomalyResultManager mergedAnomalyResultManager;

  @Inject
  private DataCubeSummaryCalculator dataCubeSummaryCalculator;

  public RootCauseResourceProvider(
      final ThirdEyeCoordinatorConfiguration config) {
    this.config = config;
  }

  private static File getRootCauseDefinitionsFile(ThirdEyeCoordinatorConfiguration config) {
    File rcaConfigFile = new File("rca.yml");
    if (!rcaConfigFile.isAbsolute()) {
      return new File(config.getConfigPath() + File.separator + rcaConfigFile);
    }
    return rcaConfigFile;
  }

  private static Map<String, RCAFramework> makeRootCauseFrameworks(RCAConfiguration config,
      File definitionsFile) throws Exception {
    ExecutorService executor = Executors.newFixedThreadPool(config.getParallelism());
    return RCAFrameworkLoader.getFrameworksFromConfig(definitionsFile, executor);
  }

  private static List<RootCauseEntityFormatter> makeRootCauseFormatters(
      RCAConfiguration config) throws Exception {
    List<RootCauseEntityFormatter> formatters = new ArrayList<>();
    if (config.getFormatters() != null) {
      for (String className : config.getFormatters()) {
        try {
          formatters.add(FormatterLoader.fromClassName(className));
        } catch (ClassNotFoundException e) {
          LOG.warn("Could not find formatter class '{}'. Skipping.", className, e);
        }
      }
    }
    formatters.add(new DefaultEntityFormatter());
    return formatters;
  }

  private RootCauseResource makeRootCauseResource() throws Exception {
    File definitionsFile = getRootCauseDefinitionsFile(config);
    if (!definitionsFile.exists()) {
      throw new IllegalArgumentException(
          String.format("Could not find definitions file '%s'", definitionsFile));
    }

    RCAConfiguration rcConfig = new RCAConfiguration();
    return new RootCauseResource(
        makeRootCauseFrameworks(rcConfig, definitionsFile),
        makeRootCauseFormatters(rcConfig),
        mergedAnomalyResultManager,
        dataCubeSummaryCalculator);
  }

  @Override
  public RootCauseResource get() {
    try {
      return makeRootCauseResource();
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
}
