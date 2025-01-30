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
package ai.startree.thirdeye.spi.api;

public class DemoDatasetApi implements ThirdEyeApi {

  /**
   * Should be unique per ThirdEyeDataSource implementation.
   * Different ThirdEyeDataSource implementation may have the same id, but if possible it should be
   * avoided.
   */
  private String id;
  /**
   * Name to display in webapp.
   */
  private String name;
  /**
   * Description to display in webapp. May contain markdown.
   */
  private String description;

  public String getId() {
    return id;
  }

  public DemoDatasetApi setId(final String id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public DemoDatasetApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public DemoDatasetApi setDescription(final String description) {
    this.description = description;
    return this;
  }
}
