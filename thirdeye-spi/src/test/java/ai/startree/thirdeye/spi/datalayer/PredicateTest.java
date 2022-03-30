package ai.startree.thirdeye.spi.datalayer;

import static ai.startree.thirdeye.spi.datalayer.Predicate.parseAndCombinePredicates;
import static ai.startree.thirdeye.spi.datalayer.Predicate.parseFilterPredicate;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import java.util.List;
import org.junit.Test;

public class PredicateTest {

  @Test
  public void testParseFilterPredicateNominalCase() {
    Predicate output = parseFilterPredicate("browser=chrome");
    assertThat(output.getLhs()).isEqualTo("browser");
    assertThat(output.getOper()).isEqualTo(OPER.EQ);
    assertThat((String) output.getRhs()).isEqualTo("chrome");
  }

  @Test
  public void testParseFilterPredicateEmptyRhs() {
    Predicate output = parseFilterPredicate("browser=");
    assertThat((String) output.getRhs()).isEqualTo("");
  }

  @Test
  public void testParseAndCombinePredicatesNoCombine() {
    List<String> stringPredicates = List.of("browser=chrome", "country=US", "revenue>30");
    List<Predicate> output = parseAndCombinePredicates(stringPredicates);
    assertThat(output.size()).isEqualTo(3);
    // order is changed due to EQ oper being combined (or not) at the end
    // order is not important - underlying implementation may break order, this is ok - just fix the test
    assertThat(output.get(0).getLhs()).isEqualTo("revenue");
    assertThat(output.get(0).getOper()).isEqualTo(OPER.GT);
    assertThat(output.get(1).getLhs()).isEqualTo("country");
    assertThat(output.get(1).getOper()).isEqualTo(OPER.EQ);
    assertThat(output.get(2).getLhs()).isEqualTo("browser");
    assertThat(output.get(2).getOper()).isEqualTo(OPER.EQ);
  }

  @Test
  public void testParseAndCombinePredicatesWithInCombine() {
    List<String> stringPredicates = List.of("browser=chrome", "browser=safari", "country=US");
    List<Predicate> output = parseAndCombinePredicates(stringPredicates);
    assertThat(output.size()).isEqualTo(2);
    // order is not important - underlying implementation may break order, this is ok - just fix the test
    assertThat(output.get(0).getLhs()).isEqualTo("country");
    assertThat(output.get(0).getOper()).isEqualTo(OPER.EQ);
    assertThat(output.get(1).getLhs()).isEqualTo("browser");
    assertThat(output.get(1).getOper()).isEqualTo(OPER.IN);
    // order is not important here - underlying implementation may break order, this is ok - just fix the test
    assertThat((String[]) output.get(1).getRhs()).isEqualTo(new String[]{"chrome", "safari"});
  }


}
