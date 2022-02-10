package ai.startree.thirdeye;

import static ai.startree.thirdeye.DaoFilterBuilder.toPair;
import static ai.startree.thirdeye.DaoFilterBuilder.toPredicate;
import static ai.startree.thirdeye.spi.util.Pair.createPair;
import static org.assertj.core.api.Assertions.assertThat;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.Predicate.OPER;
import org.testng.annotations.Test;

public class DaoFilterBuilderTest {

  @Test
  public void testToPair() {
    assertThat(toPair("abcd")).isEqualTo(createPair(OPER.EQ, "abcd"));
    assertThat(toPair("[gt]1234")).isEqualTo(createPair(OPER.GT, "1234"));
    assertThat(toPair("[gte]1234")).isEqualTo(createPair(OPER.GE, "1234"));
    assertThat(toPair("[lte]-1")).isEqualTo(createPair(OPER.LE, "-1"));
  }

  @Test
  public void testToOrPredicate() {
    assertThat(toPredicate("col", new Object[]{
        "1", "2", "3", "4"
    })).isEqualTo(Predicate.AND(
        Predicate.EQ("col", "1"),
        Predicate.EQ("col", "2"),
        Predicate.EQ("col", "3"),
        Predicate.EQ("col", "4")
    ));

    assertThat(toPredicate("col", new Object[]{
        "[gt]1", "[lte]-1"
    })).isEqualTo(Predicate.AND(
        Predicate.GT("col", "1"),
        Predicate.LE("col", "-1")
    ));
  }
}
