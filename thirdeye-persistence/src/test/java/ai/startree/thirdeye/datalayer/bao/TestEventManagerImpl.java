package ai.startree.thirdeye.datalayer.bao;

import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.annotations.Test;

public class TestEventManagerImpl {

  private static final String COUNTRY_DIMENSION_KEY = "country";
  private static final String US_COUNTRY_VALUE = "US";
  private static final String FR_COUNTRY_VALUE = "FR";

  private static final String ENV_DIMENSION_KEY = "environment";
  private static final String PROD_ENV_VALUE = "prod";
  private static final String DEV_ENV_VALUE = "dev";
  private static final Map<String, List<String>> DIMENSIONS = Map.of(
      COUNTRY_DIMENSION_KEY,
      List.of(US_COUNTRY_VALUE, FR_COUNTRY_VALUE),
      ENV_DIMENSION_KEY,
      List.of(PROD_ENV_VALUE));

  private static final EventDTO CHRISTMAS_EVENT = new EventDTO().setName("CHRISTMAS")
      .setTargetDimensionMap(DIMENSIONS);
  private static final EventDTO EASTER_EVENT = new EventDTO().setName("EASTER")
      .setTargetDimensionMap(DIMENSIONS);
  private static final EventDTO FR_ONLY_EVENT = new EventDTO().setName("FR_ONLY_EVENT")
      .setTargetDimensionMap(Map.of(COUNTRY_DIMENSION_KEY,
          List.of(FR_COUNTRY_VALUE),
          ENV_DIMENSION_KEY,
          List.of(PROD_ENV_VALUE)));
  private static final EventDTO DEV_ENV_ONLY_EVENT = new EventDTO().setName("DEV_ENV_ONLY_EVENT")
      .setTargetDimensionMap(Map.of(COUNTRY_DIMENSION_KEY,
          List.of(US_COUNTRY_VALUE, FR_COUNTRY_VALUE),
          ENV_DIMENSION_KEY,
          List.of(DEV_ENV_VALUE)));

  private static final List<EventDTO> EVENT_LIST = List.of(CHRISTMAS_EVENT,
      EASTER_EVENT,
      FR_ONLY_EVENT,
      DEV_ENV_ONLY_EVENT);

  @Test
  public void testApplyDimensionFiltersWithNullFilters() {
    final List<EventDTO> output = EventManagerImpl.applyDimensionFilters(EVENT_LIST, null);

    assertThat(output).isEqualTo(EVENT_LIST);
  }

  @Test
  public void testApplyDimensionFiltersWithEmptyMap() {
    final List<EventDTO> output = EventManagerImpl.applyDimensionFilters(EVENT_LIST, Map.of());

    assertThat(output).isEqualTo(EVENT_LIST);
  }

  @Test
  public void testApplyDimensionFiltersWithNoAllowedValues() {
    final Map<String, Set<String>> filters = Map.of(COUNTRY_DIMENSION_KEY, Set.of());
    final List<EventDTO> output = EventManagerImpl.applyDimensionFilters(EVENT_LIST, filters);

    assertThat(output.isEmpty()).isTrue();
  }

  @Test
  public void testApplyDimensionFiltersWithCountryUS() {
    final Map<String, Set<String>> filters = Map.of(COUNTRY_DIMENSION_KEY,
        Set.of(US_COUNTRY_VALUE));
    final List<EventDTO> output = EventManagerImpl.applyDimensionFilters(EVENT_LIST, filters);

    final List<EventDTO> expected = List.of(CHRISTMAS_EVENT, EASTER_EVENT, DEV_ENV_ONLY_EVENT);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testApplyDimensionFiltersWithCountryUSorFR() {
    final Map<String, Set<String>> filters = Map.of(COUNTRY_DIMENSION_KEY,
        Set.of(US_COUNTRY_VALUE, FR_COUNTRY_VALUE));
    final List<EventDTO> output = EventManagerImpl.applyDimensionFilters(EVENT_LIST, filters);

    assertThat(output).isEqualTo(EVENT_LIST);
  }

  @Test
  public void testApplyDimensionFiltersWithCountryUSandEnvProd() {
    final Map<String, Set<String>> filters = Map.of(
        COUNTRY_DIMENSION_KEY,
        Set.of(US_COUNTRY_VALUE),
        ENV_DIMENSION_KEY,
        Set.of(PROD_ENV_VALUE));
    final List<EventDTO> output = EventManagerImpl.applyDimensionFilters(EVENT_LIST, filters);

    final List<EventDTO> expected = List.of(CHRISTMAS_EVENT, EASTER_EVENT);

    assertThat(output).isEqualTo(expected);
  }

  @Test
  public void testApplyDimensionFiltersWithCountryUSorFRandEnvProd() {
    final Map<String, Set<String>> filters = Map.of(
        COUNTRY_DIMENSION_KEY,
        Set.of(US_COUNTRY_VALUE, FR_COUNTRY_VALUE),
        ENV_DIMENSION_KEY,
        Set.of(PROD_ENV_VALUE));
    final List<EventDTO> output = EventManagerImpl.applyDimensionFilters(EVENT_LIST, filters);

    final List<EventDTO> expected = List.of(CHRISTMAS_EVENT, EASTER_EVENT, FR_ONLY_EVENT);

    assertThat(output).isEqualTo(expected);
  }
}
