/*
 * Copyright 2023 StarTree Inc
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

  /**
   * Return the name of the column in the relation that contains the id
   */
  String idColumn();
}
