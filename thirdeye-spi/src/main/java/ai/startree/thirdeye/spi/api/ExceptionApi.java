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

package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;

@JsonInclude(Include.NON_NULL)
public class ExceptionApi {

  private String message;
  private ExceptionApi cause;
  private List<StackTraceElementApi> stackTrace;

  public String getMessage() {
    return message;
  }

  public ExceptionApi setMessage(final String message) {
    this.message = message;
    return this;
  }

  public ExceptionApi getCause() {
    return cause;
  }

  public ExceptionApi setCause(final ExceptionApi cause) {
    this.cause = cause;
    return this;
  }

  public List<StackTraceElementApi> getStackTrace() {
    return stackTrace;
  }

  public ExceptionApi setStackTrace(
      final List<StackTraceElementApi> stackTrace) {
    this.stackTrace = stackTrace;
    return this;
  }
}
