package ai.startree.thirdeye.datalayer.calcite.object;

import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;

/**
 * Defines the conversion of an object to a SQL table.
 *
 * getRowType and the elements returned by getRow should correspond, in order.
 * For instance, if getRowType returns [VARCHAR, INTEGER, FLOAT]
 * getRow should return for instance Object[]{"aString", 3, 4F}
 */
public interface ObjectToRelationAdapter<T> {

  RelDataType getRowType(RelDataTypeFactory typeFactory);

  Object[] getRow(T element);
}
