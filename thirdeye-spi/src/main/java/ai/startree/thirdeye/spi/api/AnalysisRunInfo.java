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

/**
 * Should be a member of all apis that involve a data analysis that can fail because the data does
 * not match assumptions (eg missing data, too few data, invalid distributions, etc...)
 */
public class AnalysisRunInfo {

  private boolean success = true;
  private String message = "";

  public boolean isSuccess() {
    return success;
  }

  public AnalysisRunInfo setSuccess(final boolean success) {
    this.success = success;
    return this;
  }

  public String getMessage() {
    return message;
  }

  public AnalysisRunInfo setMessage(final String message) {
    this.message = message;
    return this;
  }
}
