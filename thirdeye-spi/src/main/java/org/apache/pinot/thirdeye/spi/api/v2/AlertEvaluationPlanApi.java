/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.pinot.thirdeye.spi.api.v2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.pinot.spi.utils.GroovyTemplateUtils;
import org.apache.pinot.thirdeye.spi.api.AlertApi;

/**
 * AlertEvaluationPlanApi defines the evaluation plan with AlertApi and a list of DetectionPlanApi
 * also start/end time.
 */
@JsonInclude(Include.NON_NULL)
public class AlertEvaluationPlanApi {

  private AlertApi alert;
  private List<DetectionPlanApi> nodes;
  private Date start;
  private Date end;

  public List<DetectionPlanApi> getNodes() {
    return nodes;
  }

  public AlertEvaluationPlanApi setNodes(final List<DetectionPlanApi> nodes) {
    this.nodes = nodes;
    return this;
  }

  public Date getStart() {
    return start;
  }

  public AlertEvaluationPlanApi setStart(final Date start) {
    this.start = start;
    return this;
  }

  public Date getEnd() {
    return end;
  }

  public AlertEvaluationPlanApi setEnd(final Date end) {
    this.end = end;
    return this;
  }

  public AlertApi getAlert() {
    return alert;
  }

  public AlertEvaluationPlanApi setAlert(final AlertApi alert) {
    this.alert = alert;
    return this;
  }

  public static AlertEvaluationPlanApi applyContextToTemplate(String template,
      Map<String, Object> context) throws IOException, ClassNotFoundException {
    return new ObjectMapper().readValue(
        GroovyTemplateUtils.renderTemplate(template, context), AlertEvaluationPlanApi.class);
  }
}
