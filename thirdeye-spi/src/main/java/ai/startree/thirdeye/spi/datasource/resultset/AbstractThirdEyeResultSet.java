/*
 * Copyright 2022 StarTree Inc
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
package ai.startree.thirdeye.spi.datasource.resultset;

public abstract class AbstractThirdEyeResultSet implements ThirdEyeResultSet {

  @Override
  public boolean getBoolean(final int rowIndex) {
    return getBoolean(rowIndex, 0);
  }

  @Override
  public long getLong(final int rowIndex) {
    return getLong(rowIndex, 0);
  }

  @Override
  public double getDouble(final int rowIndex) {
    return getDouble(rowIndex, 0);
  }

  @Override
  public String getString(final int rowIndex) {
    return getString(rowIndex, 0);
  }

  @Override
  public int getInteger(final int rowIndex, final int columnIndex) {
    return Integer.parseInt(getString(rowIndex, columnIndex));
  }

  @Override
  public boolean getBoolean(final int rowIndex, final int columnIndex) {
    return Boolean.parseBoolean(getString(rowIndex, columnIndex));
  }

  @Override
  public long getLong(final int rowIndex, final int columnIndex) {
    return Long.parseLong(getString(rowIndex, columnIndex));
  }

  @Override
  public double getDouble(final int rowIndex, final int columnIndex) {
    return Double.parseDouble(getString(rowIndex, columnIndex));
  }
}
