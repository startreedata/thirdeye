package ai.startree.thirdeye.datalayer.calcite.filter;

import ai.startree.thirdeye.datalayer.calcite.object.ObjectToRelationAdapter;

/**
 * Make all elements uniquely identified.
 */
public interface ObjectWithIdToRelationAdapter<T> extends ObjectToRelationAdapter<T> {

  /**
   * Returns a unique identifier of the element.
   */
  Long idOf(T element);

  /**Return the name of the column in the relation that contains the id*/
  String idColumn();
}
