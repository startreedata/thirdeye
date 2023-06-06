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

@JsonInclude(Include.NON_NULL)
public class StackTraceElementApi {

  private String className;
  private String methodName;
  private String fileName;
  private int lineNumber;

  public String getClassName() {
    return className;
  }

  public StackTraceElementApi setClassName(final String className) {
    this.className = className;
    return this;
  }

  public String getMethodName() {
    return methodName;
  }

  public StackTraceElementApi setMethodName(final String methodName) {
    this.methodName = methodName;
    return this;
  }

  public String getFileName() {
    return fileName;
  }

  public StackTraceElementApi setFileName(final String fileName) {
    this.fileName = fileName;
    return this;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public StackTraceElementApi setLineNumber(final int lineNumber) {
    this.lineNumber = lineNumber;
    return this;
  }
}
