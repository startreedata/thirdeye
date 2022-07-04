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
package ai.startree.thirdeye.spi;

public class ThirdEyeException extends RuntimeException {

  private final ThirdEyeStatus status;

  public ThirdEyeException(final ThirdEyeStatus status, Object... args) {
    super(getMsg(status, args));
    this.status = status;
  }

  public ThirdEyeException(final Throwable cause, final ThirdEyeStatus status, Object... args) {
    super(getMsg(status, args), cause);
    this.status = status;
  }

  private static String getMsg(final ThirdEyeStatus status, final Object[] args) {
    return String.format(status.getMessage(), args);
  }

  public ThirdEyeStatus getStatus() {
    return status;
  }
}
