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
package ai.startree.thirdeye.datalayer.entity;

import org.checkerframework.checker.nullness.qual.Nullable;

public class NamespaceConfigurationEntity extends AbstractEntity
    implements HasJsonVal<NamespaceConfigurationEntity> {

  private String timezone;
  private String dateTimePattern;
  private long minimumOnboardingStartTime;

  private String jsonVal;

  private @Nullable String namespace;

  @Override
  public String getJsonVal() {
    return this.jsonVal;
  }

  @Override
  public NamespaceConfigurationEntity setJsonVal(final String jsonVal) {
    this.jsonVal = jsonVal;
    return this;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public String getDateTimePattern() { return dateTimePattern; }

  public void setDateTimePattern(String dateTimePattern) {
    this.dateTimePattern = dateTimePattern;
  }

  public long getMinimumOnboardingStartTime() { return minimumOnboardingStartTime; }

  public void setMinimumOnboardingStartTime(long minimumOnboardingStartTime) {
    this.minimumOnboardingStartTime = minimumOnboardingStartTime;
  }

  public @Nullable String getNamespace() {
    return namespace;
  }

  public NamespaceConfigurationEntity setNamespace(final @Nullable String namespace) {
    this.namespace = namespace;
    return this;
  }
}
