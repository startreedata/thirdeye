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
package ai.startree.thirdeye;

import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

public class MicrometerPrometheusServlet extends HttpServlet {

  private final PrometheusMeterRegistry promRegistry;

  public MicrometerPrometheusServlet(final PrometheusMeterRegistry newRegistry) {
    this.promRegistry = newRegistry;
  }

  @Override
  protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
    resp.setStatus(200);
    String contentType = TextFormat.chooseContentType(req.getHeader("Accept"));
    resp.setContentType(contentType);
    try (Writer writer = new BufferedWriter(resp.getWriter())) {
      String scrape = promRegistry.scrape(contentType);
      writer.write(scrape);
      writer.flush();
    }
  }
}
