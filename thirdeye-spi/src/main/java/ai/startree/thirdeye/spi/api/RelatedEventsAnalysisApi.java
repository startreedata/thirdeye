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

import java.util.List;

public class RelatedEventsAnalysisApi {

  private List<EventApi> events;

  private TextualAnalysis textualAnalysis;

  public List<EventApi> getEvents() {
    return events;
  }

  public RelatedEventsAnalysisApi setEvents(
      final List<EventApi> events) {
    this.events = events;
    return this;
  }

  public TextualAnalysis getTextualAnalysis() {
    return textualAnalysis;
  }

  public RelatedEventsAnalysisApi setTextualAnalysis(
      final TextualAnalysis textualAnalysis) {
    this.textualAnalysis = textualAnalysis;
    return this;
  }
}
