/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.scheduler.events;

import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.math3.distribution.AbstractRealDistribution;
import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.random.Well19937c;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock data generator for RCA events similar to mock time series generator. Produces series of
 * events with pseudo-randomly
 * chosen arrival times and durations based on user config with a deterministic RNG seed. Events are
 * generated
 * and persistent into ThirdEye's database after deduplication with pre-existing events.
 *
 * Events are generated from a fixed point in the past (Jan 1st 2019 12am GMT) into the future
 * relative to the generator's
 * time of execution (now + 365 days).
 *
 * @see ai.startree.thirdeye.datasource.mock.MockThirdEyeDataSource
 */
@Singleton
public class MockEventsLoader implements Runnable {

  public static final String DIST_TYPE_GAUSSIAN = "gaussian";
  public static final String DIST_TYPE_EXPONENTIAL = "exponential";
  public static final String DIST_TYPE_LOGNORMAL = "lognormal";
  public static final String DIST_TYPE_FIXED = "fixed";

  private static final Logger LOG = LoggerFactory.getLogger(MockEventsLoader.class);

  private static final Comparator<EventDTO> MOCK_EVENT_COMPARATOR = Comparator
      .comparingLong(EventDTO::getStartTime)
      .thenComparingLong(EventDTO::getEndTime)
      .thenComparing(EventDTO::getEventType);

  private static final long START_TIMESTAMP = 1546300800000L; // January 1, 2019 12:00:00 AM GMT
  private static final long END_OFFSET = 31536000000L; // 365d in ms

  private final MockEventsConfiguration configuration;
  private final EventManager eventDAO;

  @Inject
  public MockEventsLoader(MockEventsConfiguration configuration, EventManager eventDAO) {
    this.configuration = configuration;
    this.eventDAO = eventDAO;
  }

  @Override
  public void run() {
    if (configuration.isEnabled()) {
      loadEvents();
    } else {
      LOG.info("MockEventsLoader disabled! Skipping Execution.");
    }
  }

  private void loadEvents() {
    final long cutoff = System.currentTimeMillis() + END_OFFSET;

    for (MockEventsConfiguration.EventGeneratorConfig conf : this.configuration
        .getGenerators()) {
      LOG.info("Generating '{}' events from {} to {}", conf.getType(), START_TIMESTAMP, cutoff);

      List<EventDTO> generated = generateEvents(conf, cutoff);
      List<EventDTO> existing = this.eventDAO
          // namespacing is not implemented for this feature - todo cyril authz - consider implementing namespace logic for this feature
          .findEventsBetweenTimeRangeInNamespace(START_TIMESTAMP, cutoff, conf.getType(), null);

      Set<EventDTO> deduplicated = deduplicate(generated, existing);
      LOG.info("Generated '{}' events: {} generated, {} pre-existing, {} saved after deduplication",
          conf.getType(), generated.size(), existing.size(), deduplicated.size());

      deduplicated.forEach(this.eventDAO::save);
    }
  }

  /**
   * Generate an event time series based on a given config, up to a cutoff timestamp.
   *
   * @param conf generator config
   * @param cutoff cutoff timestamp
   * @return series of mock events
   */
  List<EventDTO> generateEvents(MockEventsConfiguration.EventGeneratorConfig conf,
      long cutoff) {
    List<EventDTO> generated = new ArrayList<>();

    AbstractRealDistribution arrivalDist = makeDist(conf.getArrivalType(), conf.getArrivalMean(),
        conf.getSeed());
    AbstractRealDistribution durationDist = makeDist(conf.getDurationType(), conf.getDurationMean(),
        conf.getSeed());
    Random nameDist = new Random(conf.getSeed());

    EventGenerator generator = new EventGenerator(conf.getType(), arrivalDist, durationDist,
        nameDist, conf.getNamePrefixes(), conf.getNameSuffixes());

    EventDTO event;
    while ((event = generator.next()).getStartTime() < cutoff) {
      generated.add(event);
    }

    return generated;
  }

  /**
   * Deduplicates a list of generated events relative to a (larger, streamed) set of existing
   * events. Uses a custom
   * comparator to avoid polluting the <code>equals()</code> implementation of EventDTO.
   *
   * @param generated generated events
   * @param existing existing events (as per ThirdEye DB)
   * @return deduplicated (newly) generated events
   */
  Set<EventDTO> deduplicate(Collection<EventDTO> generated, Collection<EventDTO> existing) {
    Set<EventDTO> sorted = new TreeSet<>(MOCK_EVENT_COMPARATOR);
    sorted.addAll(generated);
    existing.forEach(sorted::remove);
    return sorted;
  }

  /**
   * Return a pre-configured distribution given a set of configuration parameters.
   *
   * @param type distribution type
   * @param param main param (usually mean)
   * @param seed RNG seed
   * @return distribution
   */
  AbstractRealDistribution makeDist(String type, double param, int seed) {
    switch (type.toLowerCase()) {
      case DIST_TYPE_FIXED:
        return new UniformRealDistribution(param, param + 0.001);
      case DIST_TYPE_GAUSSIAN:
        return new NormalDistribution(new Well19937c(seed), param, 1.0d, 1.0E-9D);
      case DIST_TYPE_EXPONENTIAL:
        return new ExponentialDistribution(new Well19937c(seed), param, 1.0E-9D);
      case DIST_TYPE_LOGNORMAL:
        return new LogNormalDistribution(new Well19937c(seed), param, 1.0d, 1.0E-9D);
      default:
        throw new IllegalArgumentException(
            String.format("Unsupported distribution type '%s'", type));
    }
  }

  /**
   * Iterator-like wrapper to generate an endless series of events given an event type, an arrival
   * time
   * distribution, an event duration distribution, and a random name generator.
   */
  static class EventGenerator {

    String type;
    AbstractRealDistribution arrivalDist;
    AbstractRealDistribution durationDist;
    Random nameDist;
    List<String> namePrefixes;
    List<String> nameSuffixes;

    int eventCount = 0;
    long lastTimestamp = START_TIMESTAMP;

    public EventGenerator(String type, AbstractRealDistribution arrivalDist,
        AbstractRealDistribution durationDist,
        Random nameDist, List<String> namePrefixes, List<String> nameSuffixes) {
      this.type = type;
      this.arrivalDist = arrivalDist;
      this.durationDist = durationDist;
      this.nameDist = nameDist;
      this.namePrefixes = namePrefixes;
      this.nameSuffixes = nameSuffixes;
    }

    static String makeString(Random rng, List<String> strings, String defaultString) {
      if (strings.isEmpty()) {
        return defaultString;
      }
      return strings.get(rng.nextInt(strings.size()));
    }

    public EventDTO next() {
      long arrival = lastTimestamp + (long) this.arrivalDist.sample();
      long duration = (long) this.durationDist.sample();

      this.eventCount++;
      this.lastTimestamp = arrival;

      String prefix = makeString(this.nameDist, this.namePrefixes, this.type);
      String suffix = makeString(this.nameDist, this.nameSuffixes, String.valueOf(this.eventCount));

      EventDTO event = new EventDTO();
      event.setStartTime(arrival);
      event.setEndTime(arrival + duration);
      event.setName(String.format("%s %s", prefix, suffix));
      event.setEventType(this.type.toUpperCase());
      event.setTargetDimensionMap(Collections.emptyMap());

      return event;
    }
  }
}
