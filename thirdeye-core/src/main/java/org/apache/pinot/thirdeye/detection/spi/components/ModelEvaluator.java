/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package org.apache.pinot.thirdeye.detection.spi.components;

import org.apache.pinot.thirdeye.detection.spi.model.ModelEvaluationResult;
import org.apache.pinot.thirdeye.spi.detection.AbstractSpec;
import org.apache.pinot.thirdeye.spi.detection.BaseComponent;
import org.joda.time.Instant;

/**
 * The interface for model evaluator.
 *
 * @param <T> the spec class for this model evaluator
 */
public interface ModelEvaluator<T extends AbstractSpec> extends BaseComponent<T> {

  /**
   * Evaluate the current detection model.
   *
   * @param evaluationTimeStamp the time stamp when the evaluation is run.
   * @return a model evaluation result
   */
  ModelEvaluationResult evaluateModel(Instant evaluationTimeStamp);
}
