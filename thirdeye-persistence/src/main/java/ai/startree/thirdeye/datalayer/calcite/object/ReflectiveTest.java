/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.datalayer.calcite.object;

import ai.startree.thirdeye.datalayer.calcite.filter.SqlFilterRunner;
import ai.startree.thirdeye.datalayer.calcite.object.adapter.EventToRelationAdapter;
import ai.startree.thirdeye.spi.datalayer.dto.EventDTO;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.tools.RelConversionException;
import org.apache.calcite.tools.ValidationException;

// todo cyril move this to test
public class ReflectiveTest {

  public static void main(String[] args)
      throws SQLException, SqlParseException, ValidationException, RelConversionException {
    final EventDTO event1 = (EventDTO) new EventDTO().setName("lol")
        .setStartTime(1234L)
        .setEndTime(5678L)
        .setTargetDimensionMap(Map.of("country", List.of("US", "re")))
        .setId(3L)
        ;
    final EventDTO event2 = (EventDTO) new EventDTO().setName("lol")
        .setStartTime(1234L)
        .setEndTime(5678L)
        .setEventType("haha")
        .setTargetDimensionMap(Map.of("lol", List.of("US", "re")))
        .setId(4L)
        ;
    List<EventDTO> events = List.of(event1, event2);

    final SqlFilterRunner<EventDTO> filterRunner = new SqlFilterRunner<>(new EventToRelationAdapter());
    final ObjectSchema<EventDTO> eventSchema = new ObjectSchema<>(events, new EventToRelationAdapter());
    final String queryFilter = "(MULTISET['US', 'FR'] MULTISET INTERSECT dimensionMap['country']) is not empty";
    List<EventDTO> filteredEvents = filterRunner.applyFilter(events, queryFilter);
    List<EventDTO> filteredEvents2 = filterRunner.applyFilter(events, "'US' member of dimensionMap['country']");
    List<EventDTO> filteredEvents3 = filterRunner.applyFilter(events, "'FR' member of dimensionMap['country']");
    //List<EventDTO> filteredEvents = filterRunner.applyFilter(events, "'US' member of dimensionMap['country']", new EventSchemaFactory());
    String lol  ="lol";
  }
}
