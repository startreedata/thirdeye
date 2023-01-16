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
package ai.startree.thirdeye.spi.dataframe;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

public abstract class AbstractTestResultSetMetaData implements ResultSetMetaData {

  @Override
  public boolean isAutoIncrement(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCaseSensitive(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSearchable(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isCurrency(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int isNullable(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isSigned(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getColumnDisplaySize(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getColumnName(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getSchemaName(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getPrecision(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int getScale(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getTableName(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getCatalogName(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getColumnTypeName(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isReadOnly(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isWritable(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDefinitelyWritable(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getColumnClassName(final int column) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> T unwrap(final Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isWrapperFor(final Class<?> iface) throws SQLException {
    throw new UnsupportedOperationException();
  }
}
