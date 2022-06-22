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
package ai.startree.thirdeye.spi.metric;

/**
 * Represents the various data types supported for a metric<br/>
 * Currently we support INT, SHORT, LONG, FLOAT, DOUBLE
 *
 * @author kgopalak
 */
public enum MetricType {

  INT {
    public Number toNumber(String s) {
      return Integer.parseInt(s);
    }

    public int byteSize() {
      return 4;
    }
  },
  SHORT {
    public Number toNumber(String s) {
      return Short.parseShort(s);
    }

    public int byteSize() {
      return 2;
    }
  },
  LONG {
    public Number toNumber(String s) {
      return Long.parseLong(s);
    }

    public int byteSize() {
      return 8;
    }
  },
  FLOAT {
    public Number toNumber(String s) {
      return Float.parseFloat(s);
    }

    public int byteSize() {
      return 4;
    }
  },
  DOUBLE {
    public Number toNumber(String s) {
      return Double.parseDouble(s);
    }

    public int byteSize() {
      return 8;
    }
  };

  public Number toNumber(String s) {
    throw new AbstractMethodError();
  }

  public int byteSize() {
    throw new AbstractMethodError();
  }
}
