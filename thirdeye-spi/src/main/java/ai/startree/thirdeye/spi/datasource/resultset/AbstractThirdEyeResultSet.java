/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
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
