/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.rca;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.pinot.thirdeye.config.ThirdEyeServerConfiguration;
import org.apache.pinot.thirdeye.datalayer.DataSourceBuilder;
import org.apache.pinot.thirdeye.datalayer.ThirdEyePersistenceModule;
import org.apache.pinot.thirdeye.datalayer.util.DatabaseConfiguration;
import org.apache.pinot.thirdeye.datasource.ThirdEyeCacheRegistry;
import org.apache.pinot.thirdeye.rootcause.Pipeline;
import org.apache.pinot.thirdeye.rootcause.RCAFramework;
import org.apache.pinot.thirdeye.rootcause.RCAFrameworkExecutionResult;
import org.apache.pinot.thirdeye.rootcause.impl.RCAFrameworkLoader;
import org.apache.pinot.thirdeye.spi.rootcause.Entity;
import org.apache.pinot.thirdeye.spi.rootcause.impl.TimeRangeEntity;
import org.apache.pinot.thirdeye.spi.rootcause.util.EntityUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Console interface for performing root cause search using a sample pipeline configuration.
 * The user can specify the TimeRange and Baseline entities, as well as arbitrary URNs to
 * populate the search context with. The console interface allows one-off or interactive REPL
 * execution modes.
 *
 * <br/><b>Example:</b> {@code java -cp target/thirdeye-dashboard-1.0-SNAPSHOT.jar
 * org.apache.pinot.thirdeye.rootcause.impl.RCAFrameworkRunner
 * --config-dir local-configs/ --window-size 28 --baseline-offset 28 --entities
 * thirdeye:metric:pageViews,thirdeye:metric:logins}
 */
public class RCAFrameworkRunner {

  private static final String CLI_THIRDEYE_CONFIG = "thirdeye-config";
  private static final String CLI_WINDOW_SIZE = "window-size";
  private static final String CLI_BASELINE_OFFSET = "baseline-offset";
  private static final String CLI_ENTITIES = "entities";
  private static final String CLI_ROOTCAUSE_CONFIG = "rootcause-config";
  private static final String CLI_TIME_START = "time-start";
  private static final String CLI_TIME_END = "time-end";
  private static final String CLI_INTERACTIVE = "interactive";
  private static final String CLI_VERBOSE = "verbose";
  private static final String CLI_FRAMEWORK = "framework";
  private static final DateTimeFormatter ISO8601 = ISODateTimeFormat.basicDateTimeNoMillis();

  private static final long DAY_IN_MS = 24 * 3600 * 1000;

  public static void main(String[] args) throws Exception {
    Options options = new Options();

    addReqOption(options, "c", CLI_THIRDEYE_CONFIG, true, "ThirdEye configuration file path");
    addReqOption(options, null, CLI_ROOTCAUSE_CONFIG, true, "RootCause framework config file path");
    addReqOption(options, null, CLI_FRAMEWORK, true, "framework name in RCA configuration.");

    options.addOption(null, CLI_WINDOW_SIZE, true,
        "window size for search window (in days, defaults to '7')");
    options.addOption(null, CLI_BASELINE_OFFSET, true,
        "baseline offset (in days, from start of window)");
    options.addOption(null, CLI_ENTITIES, true, "search context metric entities");
    options.addOption(null, CLI_TIME_START, true,
        "start time of the search window (ISO 8601, e.g. '20170701T150000Z')");
    options.addOption(null, CLI_TIME_END, true,
        "end time of the search window (ISO 8601, e.g. '20170831T030000Z', defaults to now)");
    options.addOption(null, CLI_INTERACTIVE, false,
        "enters interacive REPL mode (specified entities will be added automatically)");
    options.addOption("v", CLI_VERBOSE, false, "verbose output mode (set RCA log level to DEBUG)");

    Parser parser = new BasicParser();
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(RCAFrameworkRunner.class.getSimpleName(), options);
      System.exit(1);
    }

    if (cmd.hasOption(CLI_WINDOW_SIZE) && cmd.hasOption(CLI_TIME_START)) {
      System.out.println(
          String.format("--%s and --%s mutually exclusive", CLI_WINDOW_SIZE, CLI_TIME_START));
      System.exit(1);
    }

    // config
    File config = new File(cmd.getOptionValue(CLI_THIRDEYE_CONFIG));

    File daoConfig = new File(config.getAbsolutePath() + "/persistence.yml");
    final DatabaseConfiguration dbConfig = new DatabaseConfiguration();

    final DataSource dataSource = new DataSourceBuilder().build(dbConfig);
    Injector injector = Guice.createInjector(new ThirdEyePersistenceModule(dataSource));

    ThirdEyeServerConfiguration thirdEyeConfig = new ThirdEyeServerConfiguration();
    thirdEyeConfig.setRootDir(config.getAbsolutePath());

    injector.getInstance(ThirdEyeCacheRegistry.class)
        .initializeCaches();

    // ************************************************************************
    // Framework setup
    // ************************************************************************
    List<Pipeline> pipelines = injector.getInstance(RCAFrameworkLoader.class)
        .getPipelinesFromConfig(cmd.getOptionValue(CLI_FRAMEWORK));

    // Executor
    ExecutorService executor = Executors.newSingleThreadExecutor();

    RCAFramework framework = new RCAFramework(pipelines, executor);

    // ************************************************************************
    // Entities
    // ************************************************************************
    Set<Entity> entities = new HashSet<>();

    // time range and baseline
    long now = System.currentTimeMillis();
    long windowEnd = now;
    if (cmd.hasOption(CLI_TIME_END)) {
      windowEnd = ISO8601.parseDateTime(cmd.getOptionValue(CLI_TIME_END)).getMillis();
    }

    long windowSize = 7 * DAY_IN_MS;
    if (cmd.hasOption(CLI_TIME_START)) {
      windowSize =
          windowEnd - ISO8601.parseDateTime(cmd.getOptionValue(CLI_TIME_START)).getMillis();
    } else if (cmd.hasOption(CLI_WINDOW_SIZE)) {
      windowSize = Long.parseLong(cmd.getOptionValue(CLI_WINDOW_SIZE)) * DAY_IN_MS;
    }

    long baselineOffset = 0;
    if (cmd.hasOption(CLI_BASELINE_OFFSET)) {
      baselineOffset = Long.parseLong(cmd.getOptionValue(CLI_BASELINE_OFFSET)) * DAY_IN_MS;
    }

    long windowStart = windowEnd - windowSize;

    long baselineEnd = windowStart - baselineOffset;
    long baselineStart = baselineEnd - windowSize;

    // TODO get from CLI
    long displayOffset = windowSize / 2;
    displayOffset = Math.max(displayOffset, TimeUnit.MINUTES.toMillis(30));
    long displayStart = windowStart - displayOffset;
    long displayEnd = windowStart + windowSize + displayOffset;

    System.out.println(String.format("Using current time range '%d' (%s) to '%d' (%s)", windowStart,
        ISO8601.print(windowStart), windowEnd, ISO8601.print(windowEnd)));
    System.out.println(String
        .format("Using baseline time range '%d' (%s) to '%d' (%s)", baselineStart,
            ISO8601.print(baselineStart), baselineEnd, ISO8601.print(baselineEnd)));

    entities
        .add(TimeRangeEntity.fromRange(1.0, TimeRangeEntity.TYPE_ANOMALY, windowStart, windowEnd));
    entities.add(
        TimeRangeEntity.fromRange(0.8, TimeRangeEntity.TYPE_BASELINE, baselineStart, baselineEnd));
    entities.add(
        TimeRangeEntity.fromRange(1.0, TimeRangeEntity.TYPE_ANALYSIS, displayStart, displayEnd));

    if (cmd.hasOption(CLI_ENTITIES)) {
      entities.addAll(parseURNSequence(cmd.getOptionValue(CLI_ENTITIES), 1.0));
    }

    // ************************************************************************
    // Framework execution
    // ************************************************************************
    if (cmd.hasOption(CLI_INTERACTIVE)) {
      try {
        readExecutePrintLoop(framework, entities);
      } catch (InterruptedIOException ignore) {
        // left blank, exit
      }
    } else {
      runFramework(framework, entities);
    }

    System.out.println("done.");

    // Pinot connection workaround
    System.exit(0);
  }

  private static void readExecutePrintLoop(RCAFramework framework, Collection<Entity> baseEntities)
      throws IOException {
    // search loop
    System.out.println("Enter search context metric entities' URNs (separated by comma \",\"):");
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    String line;
    while ((line = br.readLine()) != null) {
      Set<Entity> entities = new HashSet<>();
      entities.addAll(baseEntities);
      entities.addAll(parseURNSequence(line, 1.0));

      runFramework(framework, entities);

      System.out.println("Enter search context metric entities' URNs (separated by comma \",\"):");
    }
  }

  private static void runFramework(RCAFramework framework, Set<Entity> entities) {
    System.out.println("*** Search context:");
    for (Entity e : entities) {
      System.out.println(formatEntity(e));
    }

    RCAFrameworkExecutionResult result = null;
    try {
      result = framework.run(entities);
    } catch (Exception e) {
      System.out.println("*** Exception while running framework:");
      e.printStackTrace();
      return;
    }

    System.out.println("*** Linear results:");
    List<Entity> results = new ArrayList<>(result.getResults());
    Collections.sort(results, Entity.HIGHEST_SCORE_FIRST);

    for (Entity e : results) {
      System.out.println(formatEntity(e));
    }

    System.out.println("*** Grouped results:");
    Map<String, Collection<Entity>> grouped = topKPerType(results, 5);
    for (Map.Entry<String, Collection<Entity>> entry : grouped.entrySet()) {
      System.out.println(entry.getKey());
      for (Entity e : entry.getValue()) {
        System.out.println(formatEntity(e));
      }
    }
  }

  private static Collection<Entity> parseURNSequence(String urns, double score) {
    if (urns.isEmpty()) {
      return Collections.emptySet();
    }
    Set<Entity> entities = new HashSet<>();
    String[] parts = urns.split(",");
    for (String part : parts) {
      entities.add(EntityUtils.parseURN(part, score));
    }
    return entities;
  }

  /**
   * Returns the top K (first K) results per entity type from a collection of entities.
   *
   * @param entities aggregated entities
   * @param k maximum number of entities per entity type
   * @return mapping of entity types to list of entities
   */
  private static Map<String, Collection<Entity>> topKPerType(Collection<Entity> entities, int k) {
    Map<String, Collection<Entity>> map = new HashMap<>();
    for (Entity e : entities) {
      String prefix = extractPrefix(e);

      if (!map.containsKey(prefix)) {
        map.put(prefix, new ArrayList<Entity>());
      }

      Collection<Entity> current = map.get(prefix);
      if (current.size() < k) {
        current.add(e);
      }
    }

    return map;
  }

  private static String formatEntity(Entity e) {
    return String.format("%.3f [%s] %s", e.getScore(), e.getClass().getSimpleName(), e.getUrn());
  }

  private static String extractPrefix(Entity e) {
    String[] parts = e.getUrn().split(":", 3);
    return parts[0] + ":" + parts[1] + ":";
  }

  private static void addReqOption(Options options, String opt, String longOpt, boolean hasArg,
      String description) {
    Option optConfig = new Option(opt, longOpt, hasArg, description);
    optConfig.setRequired(true);
    options.addOption(optConfig);
  }
}
