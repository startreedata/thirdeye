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

package org.apache.pinot.thirdeye.detection.components.triggers;

import java.util.Arrays;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerException;
import org.apache.pinot.thirdeye.spi.detection.EventTriggerV2;

/**
 * Absolute change rule detection
 */
public class ConsoleOutputTrigger implements EventTriggerV2<ConsoleOutputTriggerSpec> {

  private ConsoleOutputTriggerSpec spec;

  @Override
  public void init(final ConsoleOutputTriggerSpec spec) {
    this.spec = spec;
  }

  @Override
  public void trigger(final Object[] event) throws EventTriggerException {
    System.out.println("Got one trigger event = " + Arrays.toString(event));
  }
}
