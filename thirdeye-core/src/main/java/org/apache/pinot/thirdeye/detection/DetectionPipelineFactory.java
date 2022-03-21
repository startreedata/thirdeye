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

package org.apache.pinot.thirdeye.detection;

import static java.util.Objects.requireNonNull;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.lang.reflect.Constructor;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AlertDTO;
import org.apache.pinot.thirdeye.spi.detection.DataProvider;

@Singleton
public class DetectionPipelineFactory {

  private static final String PROP_CLASS_NAME = "className";

  private final DataProvider dataProvider;

  @Inject
  public DetectionPipelineFactory(final DataProvider dataProvider) {
    this.dataProvider = dataProvider;
  }

  public DetectionPipeline get(DetectionPipelineContext context) {
    final AlertDTO config = requireNonNull(context.getAlert());
    final String className = config.getProperties().get(PROP_CLASS_NAME).toString();
    try {
      final Constructor<?> constructor = Class
          .forName(className)
          .getConstructor(DataProvider.class,
              AlertDTO.class,
              long.class,
              long.class);

      return (DetectionPipeline) constructor.newInstance(dataProvider,
          config,
          context.getStart(),
          context.getEnd());
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to initialize the detection pipeline.", e);
    }
  }
}
