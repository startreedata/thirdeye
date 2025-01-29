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
package ai.startree.thirdeye.exception;

import ai.startree.thirdeye.spi.api.ExceptionApi;
import ai.startree.thirdeye.spi.api.StackTraceElementApi;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ExceptionUtils {

  public static ExceptionApi toExceptionApi(final Throwable t) {
    if (t == null) {
      return null;
    }

    final List<StackTraceElementApi> stackTrace = Arrays.stream(t.getStackTrace())
        .map(ExceptionUtils::stackTraceElementApi)
        .collect(Collectors.toList());

    return new ExceptionApi()
        .setMessage(t.getMessage())
        .setCause(toExceptionApi(t.getCause()))
        .setStackTrace(stackTrace);
  }

  private static StackTraceElementApi stackTraceElementApi(final StackTraceElement ste) {
    return new StackTraceElementApi()
        .setClassName(ste.getClassName())
        .setMethodName(ste.getMethodName())
        .setFileName(ste.getFileName())
        .setLineNumber(ste.getLineNumber());
  }
}
