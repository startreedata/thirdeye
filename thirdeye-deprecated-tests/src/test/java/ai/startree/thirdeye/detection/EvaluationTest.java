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

package ai.startree.thirdeye.detection;

import ai.startree.thirdeye.spi.dataframe.DoubleSeries;
import ai.startree.thirdeye.spi.detection.Evaluation;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EvaluationTest {

  @Test
  public void testCalculateMape() {
    double mape = Evaluation
        .calculateMape(DoubleSeries.buildFrom(10, 20), DoubleSeries.buildFrom(20, 30));
    Assert.assertEquals(mape, 0.75);
  }

  @Test
  public void testCalculateZero() {
    double mape = Evaluation
        .calculateMape(DoubleSeries.buildFrom(0, 20), DoubleSeries.buildFrom(1, 30));
    Assert.assertEquals(mape, Double.POSITIVE_INFINITY);
  }
}
