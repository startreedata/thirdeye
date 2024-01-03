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

import jdk.jfr.Experimental;

/**
 * Placeholder for text analysis.
 * Experimental: the API will change.
 * */
@Experimental
public class TextualAnalysis {
  private String text;

  public String getText() {
    return text;
  }

  public TextualAnalysis setText(final String text) {
    this.text = text;
    return this;
  }
}
