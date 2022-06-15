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
package ai.startree.thirdeye.logging;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.logging.filter.FilterFactory;
import java.util.Collections;
import java.util.Set;
import javax.validation.constraints.NotNull;

// TODO: shounak. Remove this class and META-INF/services/io.dropwizard.logging.filter.FilterFactory file once dropwizard is upgraded to v2.1.x

@JsonTypeName("uri")
public class UriFilterFactory implements FilterFactory<IAccessEvent> {
  @NotNull
  private Set<String> uris = Collections.emptySet();

  @JsonProperty
  public Set<String> getUris() {
    return uris;
  }

  @JsonProperty
  public void setUris(final Set<String> uris) {
    this.uris = uris;
  }

  @Override
  public Filter<IAccessEvent> build() {
    return new Filter<IAccessEvent>() {
      @Override
      public FilterReply decide(final IAccessEvent event) {
        if (uris.contains(event.getRequestURI())) {
          return FilterReply.DENY;
        }
        return FilterReply.NEUTRAL;
      }
    };
  }
}