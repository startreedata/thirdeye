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
package ai.startree.thirdeye;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Temporary classes that merges old metrics from dropwizard-metrics and new metrics from micrometers
 */
public class MergingMetricsServlet extends HttpServlet {

  private final PrometheusMeterRegistry promRegistry;
  // based on dropwizard metrics and prometheus client
  private final CollectorRegistry legacyRegistry;

  public MergingMetricsServlet(final PrometheusMeterRegistry newRegistry, final
  CollectorRegistry legacyRegistry) {
    this.promRegistry = newRegistry;
    this.legacyRegistry = legacyRegistry;
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setStatus(200);
    String contentType = TextFormat.chooseContentType(req.getHeader("Accept"));
    resp.setContentType(contentType);
    try (Writer writer = new BufferedWriter(resp.getWriter())) {
      writer.write(promRegistry.scrape());
      writer.flush();
      TextFormat.writeFormat(contentType, writer,
          this.legacyRegistry.filteredMetricFamilySamples(this.parse(req)));
      writer.flush();
    }
  }

  private Set<String> parse(HttpServletRequest req) {
    String[] includedParam = req.getParameterValues("name[]");
    return (Set) (includedParam == null ? Collections.emptySet()
        : new HashSet(Arrays.asList(includedParam)));
  }
}
