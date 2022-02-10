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

package ai.startree.thirdeye.spi.datalayer.bao;

import ai.startree.thirdeye.spi.datalayer.dto.EntityToEntityMappingDTO;
import java.util.List;
import java.util.Set;

public interface EntityToEntityMappingManager extends AbstractManager<EntityToEntityMappingDTO> {

  List<EntityToEntityMappingDTO> findByFromURN(String fromURN);

  List<EntityToEntityMappingDTO> findByFromURNs(Set<String> fromURN);

  List<EntityToEntityMappingDTO> findByToURN(String toURN);

  List<EntityToEntityMappingDTO> findByToURNs(Set<String> toURN);

  EntityToEntityMappingDTO findByFromAndToURN(String fromURN, String toURN);

  List<EntityToEntityMappingDTO> findByMappingType(String mappingType);

  List<EntityToEntityMappingDTO> findByFromURNAndMappingType(String fromURN, String mappingType);

  List<EntityToEntityMappingDTO> findByToURNAndMappingType(String toURN, String mappingType);
}
