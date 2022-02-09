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

package ai.startree.thirdeye.rootcause.impl;

import ai.startree.thirdeye.rootcause.Pipeline;
import ai.startree.thirdeye.rootcause.PipelineResult;
import ai.startree.thirdeye.spi.rootcause.PipelineContext;
import java.util.HashSet;

/**
 * EmptyPipeline serves as a dummy implementation or sink that does not emit any output.
 * Can be used to construct an validate a DAG without a full implementation of component pipelines.
 */
public class EmptyPipeline extends Pipeline {

  @Override
  public PipelineResult run(PipelineContext context) {
    return new PipelineResult(context, new HashSet<>());
  }
}
