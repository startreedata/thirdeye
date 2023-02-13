/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.resources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.auth.AccessControlProvider;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.accessControl.AccessType;
import ai.startree.thirdeye.spi.accessControl.ResourceIdentifier;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.testng.annotations.Test;

public class EntityResourceTest {

  final ThirdEyePrincipal Nobody = new ThirdEyePrincipal("nobody", "");

  @Test(expectedExceptions = ForbiddenException.class)
  public void testGetRawEntity() throws SQLException {
    final GenericPojoDao genericPojoDao = mock(GenericPojoDao.class);
    when(genericPojoDao.getRaw(1L)).thenReturn(new DataSourceDTO().setId(1L));
    new EntityResource(
        genericPojoDao,
        new AuthorizationManager(
            mock(AlertTemplateRenderer.class),
            AccessControlProvider.alwaysDeny
        )
    ).getRawEntity(Nobody, 1L);
  }

  @Test
  public void testGetEntity_withNoAccess() {
    final UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>() {{
      put("limit", Collections.singletonList("10"));
      put("offset", Collections.singletonList("0"));
    }});
    final GenericPojoDao genericPojoDao = mock(GenericPojoDao.class);
    when(genericPojoDao.list(DataSourceDTO.class, 10, 0)).thenReturn(Arrays.asList(
        (DataSourceDTO) new DataSourceDTO().setId(1L),
        (DataSourceDTO) new DataSourceDTO().setId(2L),
        (DataSourceDTO) new DataSourceDTO().setId(3L)
    ));

    Response resp = new EntityResource(
        genericPojoDao,
        new AuthorizationManager(
            mock(AlertTemplateRenderer.class),
            AccessControlProvider.alwaysDeny
        )
    ).getEntity(Nobody, "ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO", uriInfo);
    assertThat(resp.getStatus()).isEqualTo(200);
    final List<DataSourceDTO> entities = ((Stream<DataSourceDTO>) resp.getEntity())
        .collect(Collectors.toList());
    assertThat(entities).isEmpty();
  }

  @Test
  public void testGetEntity_withPartialAccess() {
    final UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>() {{
      put("limit", Collections.singletonList("10"));
      put("offset", Collections.singletonList("0"));
    }});
    final GenericPojoDao genericPojoDao = mock(GenericPojoDao.class);
    when(genericPojoDao.list(DataSourceDTO.class, 10, 0)).thenReturn(Arrays.asList(
        (DataSourceDTO) new DataSourceDTO().setId(1L),
        (DataSourceDTO) new DataSourceDTO().setId(2L),
        (DataSourceDTO) new DataSourceDTO().setId(3L)
    ));

    Response resp = new EntityResource(
        genericPojoDao,
        new AuthorizationManager(
            mock(AlertTemplateRenderer.class),
            (String token, ResourceIdentifier identifiers, AccessType accessType)
                -> identifiers.name.equals("2")
        )
    ).getEntity(Nobody, "ai.startree.thirdeye.spi.datalayer.dto.DataSourceDTO", uriInfo);

    assertThat(resp.getStatus()).isEqualTo(200);
    final List<DataSourceDTO> entities = ((Stream<DataSourceDTO>) resp.getEntity())
        .collect(Collectors.toList());
    assertThat(1).isEqualTo(entities.size());
    assertThat(2L).isEqualTo(entities.get(0).getId());
  }
}
