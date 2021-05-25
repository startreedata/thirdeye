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

package org.apache.pinot.thirdeye.rootcause.impl;

import org.apache.pinot.thirdeye.rootcause.Pipeline;
import org.apache.pinot.thirdeye.rootcause.PipelineInitContext;
import org.apache.pinot.thirdeye.rootcause.PipelineResult;
import org.apache.pinot.thirdeye.spi.rootcause.Entity;
import org.apache.pinot.thirdeye.spi.rootcause.PipelineContext;

/**
 * NullPipeline serves as a dummy implementation or sink that emits as output any received inputs.
 * Can be used to construct an validate a DAG without a full implementation of component pipelines.
 */
public class NullPipeline extends Pipeline {

  @Override
  public void init(final PipelineInitContext context) {
    super.init(context);
  }

  @Override
  public PipelineResult run(PipelineContext context) {
    return new PipelineResult(context, context.filter(Entity.class));
  }
}
