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
package ai.startree.thirdeye.datalayer.util;

import java.lang.reflect.Field;

class ColumnInfo {

  private String columnNameInDB;
  private int sqlType;
  private String columnNameInEntity;
  private Field field;

  public String getColumnNameInDB() {
    return columnNameInDB;
  }

  public ColumnInfo setColumnNameInDB(final String columnNameInDB) {
    this.columnNameInDB = columnNameInDB;
    return this;
  }

  public int getSqlType() {
    return sqlType;
  }

  public ColumnInfo setSqlType(final int sqlType) {
    this.sqlType = sqlType;
    return this;
  }

  public String getColumnNameInEntity() {
    return columnNameInEntity;
  }

  public ColumnInfo setColumnNameInEntity(final String columnNameInEntity) {
    this.columnNameInEntity = columnNameInEntity;
    return this;
  }

  public Field getField() {
    return field;
  }

  public ColumnInfo setField(final Field field) {
    this.field = field;
    return this;
  }
}
