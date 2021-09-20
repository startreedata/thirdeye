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
 */

package org.apache.pinot.thirdeye.util;

import java.util.List;
import org.apache.pinot.thirdeye.datasource.MetricExpression;
import org.apache.pinot.thirdeye.spi.datasource.ThirdEyeRequest;

/**
 * Wrapper for ThirdEye request with derived metric expressions
 */
public class RequestContainer {

  final ThirdEyeRequest request;
  final List<MetricExpression> expressions;

  RequestContainer(ThirdEyeRequest request, List<MetricExpression> expressions) {
    this.request = request;
    this.expressions = expressions;
  }

  public ThirdEyeRequest getRequest() {
    return request;
  }

  public List<MetricExpression> getExpressions() {
    return expressions;
  }
}
