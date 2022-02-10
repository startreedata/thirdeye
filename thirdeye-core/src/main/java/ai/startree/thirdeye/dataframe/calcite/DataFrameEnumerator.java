package ai.startree.thirdeye.dataframe.calcite;

import static org.apache.calcite.linq4j.Nullness.castNonNull;

import ai.startree.thirdeye.spi.dataframe.DataFrame;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.util.ImmutableNullableList;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Enumerator that reads from a DataFrame.
 */
public class DataFrameEnumerator implements Enumerator<Object[]> {

  private final DataFrame dataFrame;
  private final @Nullable List<@Nullable Object> filterValues;
  private final AtomicBoolean cancelFlag;
  private @Nullable Object[] current;
  private int currentIndex = -1;
  List<String> columnNames;

  public DataFrameEnumerator(DataFrame dataFrame, AtomicBoolean cancelFlag) {
    this(dataFrame, cancelFlag, null);
  }

  public DataFrameEnumerator(DataFrame dataFrame, AtomicBoolean cancelFlag,
      @Nullable Object @Nullable [] filterValues) {
    this.cancelFlag = cancelFlag;
    this.filterValues = filterValues == null ? null
        : ImmutableNullableList.copyOf(filterValues);
    this.dataFrame = dataFrame;
    this.columnNames = new ArrayList<>(dataFrame.getSeriesNames());
  }

  @Override
  public Object[] current() {
    return castNonNull(current);
  }

  @Override
  public boolean moveNext() {
    outer:
    for (; ; ) {
      if (cancelFlag.get()) {
        return false;
      }
      currentIndex++;
      if (currentIndex == dataFrame.size()) {
        return false;
      }
      Object[] values = columnNames.stream()
          .map(name -> dataFrame.getObject(name, currentIndex))
          .toArray(Object[]::new);

      // works with string - long - boolean - double
      if (filterValues != null) {
        for (int i = 0; i < values.length; i++) {
          Object filterValue = filterValues.get(i);
          if (filterValue != null) {
            if (!filterValue.equals(values[i])) {
              continue outer;
            }
          }
        }
      }
      current = values;
      return true;
    }
  }

  @Override
  public void reset() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    //nothing to do
  }
}
