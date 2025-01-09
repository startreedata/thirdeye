/*
 * Copyright 2024 StarTree Inc
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
package ai.startree.thirdeye.plugins.datasource.pinot.resultset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractThirdEyeResultSet implements ThirdEyeResultSet {

  private static final Logger LOG = LoggerFactory.getLogger(AbstractThirdEyeResultSet.class);
  private static final String NULL_STRING = "null";

  @Override
  public Boolean getBoolean(final int rowIndex) {
    return getBoolean(rowIndex, 0);
  }

  @Override
  public Long getLong(final int rowIndex) {
    return getLong(rowIndex, 0);
  }

  @Override
  public Double getDouble(final int rowIndex) {
    return getDouble(rowIndex, 0);
  }

  @Override
  public String getString(final int rowIndex) {
    return getString(rowIndex, 0);
  }

  @Override
  public Integer getInteger(final int rowIndex, final int columnIndex) {
    final String stringValue = getString(rowIndex, columnIndex);
    if (stringValue == null) {
      // This can happen because Pinot and ThirdEye share the same null values for primitives. For instance, Pinot may return Long.MIN_VALUE. 
      LOG.warn("Query returned a value that matches the internal null value of the ThirdEye Series implementation. If this is not expected, this may cause errors downstream.");
      return null;
    }
    return NULL_STRING.equals(stringValue) ? null : Integer.parseInt(stringValue);
  }

  @Override
  public Boolean getBoolean(final int rowIndex, final int columnIndex) {
    final String stringValue = getString(rowIndex, columnIndex);
    if (stringValue == null) {
      // This can happen because Pinot and ThirdEye share the same null values for primitives. For instance, Pinot may return Long.MIN_VALUE. 
      LOG.warn("Query returned a value that matches the internal null value of the ThirdEye Series implementation. If this is not expected, this may cause errors downstream.");
      return null;
    }
    return NULL_STRING.equals(stringValue) ? null : Boolean.parseBoolean(stringValue);
  }

  @Override
  public Long getLong(final int rowIndex, final int columnIndex) {
    final String stringValue = getString(rowIndex, columnIndex);
    if (stringValue == null) {
      // This can happen because Pinot and ThirdEye share the same null values for primitives. For instance, Pinot may return Long.MIN_VALUE. 
      LOG.warn("Query returned a value that matches the internal null value of the ThirdEye Series implementation. If this is not expected, this may cause errors downstream.");
      return null;
    }
    return NULL_STRING.equals(stringValue) ? null : Long.parseLong(stringValue);
  }

  @Override
  public Double getDouble(final int rowIndex, final int columnIndex) {
    final String stringValue = getString(rowIndex, columnIndex);
    if (stringValue == null) {
      // This can happen because Pinot and ThirdEye share the same null values for primitives. For instance, Pinot may return Long.MIN_VALUE. 
      LOG.warn("Query returned a value that matches the internal null value of the ThirdEye Series implementation. If this is not expected, this may cause errors downstream.");
      return null;
    }
    return NULL_STRING.equals(stringValue) ? null : Double.parseDouble(stringValue);
  }
}
