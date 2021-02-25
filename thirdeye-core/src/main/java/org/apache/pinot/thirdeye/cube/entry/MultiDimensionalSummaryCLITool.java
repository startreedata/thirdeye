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

package org.apache.pinot.thirdeye.cube.entry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.pinot.thirdeye.cube.cost.CostFunction;
import org.apache.pinot.thirdeye.cube.data.dbrow.Dimensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiDimensionalSummaryCLITool {

  public static final String TOP_K_POSTFIX = "_topk";
  private static final Logger LOG = LoggerFactory.getLogger(MultiDimensionalSummaryCLITool.class);
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /**
   * Removes noisy dimensions.
   *
   * @param dimensions the original dimensions.
   * @return the original dimensions minus noisy dimensions, which are predefined.
   *
   *     TODO: Replace with an user configurable method
   */
  public static Dimensions sanitizeDimensions(Dimensions dimensions) {
    List<String> allDimensionNames = dimensions.names();
    Set<String> dimensionsToRemove = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    dimensionsToRemove.add("environment");
    dimensionsToRemove.add("colo");
    dimensionsToRemove.add("fabric");
    for (String dimensionName : allDimensionNames) {
      if (dimensionName.contains(TOP_K_POSTFIX)) {
        String rawDimensionName = dimensionName.replaceAll(TOP_K_POSTFIX, "");
        dimensionsToRemove.add(rawDimensionName.toLowerCase());
      }
    }
    return removeDimensions(dimensions, dimensionsToRemove);
  }

  public static Dimensions removeDimensions(Dimensions dimensions,
      Collection<String> dimensionsToRemove) {
    List<String> dimensionsToRetain = new ArrayList<>();
    for (String dimensionName : dimensions.names()) {
      if (!dimensionsToRemove.contains(dimensionName.trim())) {
        dimensionsToRetain.add(dimensionName);
      }
    }
    return new Dimensions(dimensionsToRetain);
  }

  public static CostFunction initiateCostFunction(String paramString)
      throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
      InvocationTargetException, InstantiationException {
    HashMap<String, String> params = objectMapper.readValue(paramString, HashMap.class);

    String className = params.get("className");
    Preconditions.checkArgument(!Strings.isNullOrEmpty(className),
        "Class name of cost function cannot be empty.");

    Class<CostFunction> clazz = (Class<CostFunction>) Class.forName(className);
    Constructor<CostFunction> constructor = clazz.getConstructor(Map.class);
    return constructor.newInstance(new Object[]{params});
  }
}
