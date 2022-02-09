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

package ai.startree.thirdeye.datalayer.bao;

import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.bao.EventManager;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;

@Singleton
public class EventManagerImpl extends AbstractManagerImpl<EventDTO> implements EventManager {

  @Inject
  public EventManagerImpl(GenericPojoDao genericPojoDao) {
    super(EventDTO.class, genericPojoDao);
  }

  public List<EventDTO> findByEventType(String eventType) {
    Predicate predicate = Predicate.EQ("eventType", eventType);
    return findByPredicate(predicate);
  }

  public List<EventDTO> findEventsBetweenTimeRange(String eventType, long start, long end) {
    Predicate predicate = Predicate
        .AND(Predicate.EQ("eventType", eventType), Predicate.GT("endTime", start),
            Predicate.LT("startTime", end));
    return findByPredicate(predicate);
  }

  public List<EventDTO> findEventsBetweenTimeRangeByName(String eventType, String name, long start,
      long end) {
    Predicate predicate = Predicate
        .AND(Predicate.EQ("eventType", eventType), Predicate.EQ("name", name),
            Predicate.GT("endTime", start), Predicate.LT("startTime", end));
    return findByPredicate(predicate);
  }
}
