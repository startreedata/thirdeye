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
