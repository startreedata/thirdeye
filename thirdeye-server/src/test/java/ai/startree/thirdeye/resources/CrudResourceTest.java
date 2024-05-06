/*
 * Copyright 2024 StarTree Inc
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

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.alert.AlertTemplateRenderer;
import ai.startree.thirdeye.auth.AuthConfiguration;
import ai.startree.thirdeye.auth.AuthorizationManager;
import ai.startree.thirdeye.auth.NamespaceResolver;
import ai.startree.thirdeye.auth.ThirdEyeAuthorizerProvider;
import ai.startree.thirdeye.auth.ThirdEyeServerPrincipal;
import ai.startree.thirdeye.datalayer.bao.AbstractManagerImpl;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.resources.testutils.SingleResourceAuthorizer;
import ai.startree.thirdeye.service.CrudService;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.auth.AuthenticationType;
import ai.startree.thirdeye.spi.auth.ThirdEyeAuthorizer;
import ai.startree.thirdeye.spi.datalayer.bao.AbstractManager;
import ai.startree.thirdeye.spi.datalayer.bao.AlertManager;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.google.common.collect.ImmutableMap;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.mockito.stubbing.Answer;
import org.testng.annotations.Test;

public class CrudResourceTest {

  static ThirdEyeServerPrincipal nobody() {
    return new ThirdEyeServerPrincipal("nobody", "", AuthenticationType.OAUTH);
  }

  @Test
  public void createUserInfoTest() {
    final DummyManager manager = mock(DummyManager.class);// new DummyManager(dao);
    when(manager.save(any(DummyDto.class))).thenAnswer((Answer<Long>) invocationOnMock -> {
      ((DummyDto) invocationOnMock.getArgument(0)).setId(1L);
      return 1L;
    });
    when(manager.update(any(DummyDto.class))).thenReturn(1);
    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        ThirdEyeAuthorizerProvider.ALWAYS_ALLOW);

    final List<String> emails = List.of("tester1@testing.com", "tester2@testing.com");
    final ThirdEyeServerPrincipal owner = getPrincipal(emails.get(0));
    final DummyApi api = new DummyApi().setData("testData");

    final Timestamp before = new Timestamp(1671476530000L);
    List<DummyApi> response = (List<DummyApi>) resource.createMultiple(owner, singletonList(api))
        .getEntity();

    assertThat(response).isNotNull();
    assertThat(response.isEmpty()).isFalse();
    final DummyApi responseApi = response.get(0);
    assertThat(responseApi.getCreatedBy()).isEqualTo(emails.get(0));
    assertThat(responseApi.getUpdatedBy()).isEqualTo(emails.get(0));
    assertThat(responseApi.getCreateTime().after(before)).isTrue();
    assertThat(responseApi.getCreateTime()).isEqualTo(responseApi.getUpdateTime());
  }

  @Test
  public void updateUserInfoTest() {
    final DummyManager manager = mock(DummyManager.class);// new DummyManager(dao);
    when(manager.save(any(DummyDto.class))).thenAnswer((Answer<Long>) invocationOnMock -> {
      ((DummyDto) invocationOnMock.getArgument(0)).setId(1L);
      return 1L;
    });
    when(manager.update(any(DummyDto.class))).thenReturn(1);
    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        ThirdEyeAuthorizerProvider.ALWAYS_ALLOW);

    final List<String> emails = List.of("tester1@testing.com", "tester2@testing.com");
    final Timestamp before = new Timestamp(1671476530000L);
    final ThirdEyeServerPrincipal owner = getPrincipal(emails.get(0));
    final ThirdEyeServerPrincipal updater = getPrincipal(emails.get(1));

    final DummyDto dbDto = new DummyDto().setData("testData");
    dbDto.setId(1L)
        .setCreatedBy(owner.getName())
        .setCreateTime(before)
        .setUpdatedBy(owner.getName())
        .setUpdateTime(before);
    when(manager.findById(1L)).thenReturn(dbDto);
    final DummyApi api = new DummyApi()
        .setId(1L)
        .setData("updateTestData");

    List<DummyApi> response = (List<DummyApi>) resource.editMultiple(updater, singletonList(api))
        .getEntity();

    assertThat(response).isNotNull();
    assertThat(response.isEmpty()).isFalse();
    final DummyApi responseApi = response.get(0);
    assertThat(responseApi.getData()).isEqualTo("updateTestData");
    assertThat(responseApi.getCreatedBy()).isEqualTo(owner.getName());
    assertThat(responseApi.getUpdatedBy()).isEqualTo(updater.getName());
    assertThat(responseApi.getCreateTime().before(responseApi.getUpdateTime())).isTrue();
  }

  private ThirdEyeServerPrincipal getPrincipal(String name) {
    return new ThirdEyeServerPrincipal(name, "", AuthenticationType.OAUTH);
  }

  @Test
  public void testGetAll_withNoAccess() {
    final DummyManager manager = mock(DummyManager.class);
    final UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    when(manager.findAll()).thenReturn(Arrays.asList(
        (DummyDto) new DummyDto().setId(1L),
        (DummyDto) new DummyDto().setId(2L),
        (DummyDto) new DummyDto().setId(3L)
    ));

    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        ThirdEyeAuthorizerProvider.ALWAYS_DENY);
    try (Response resp = resource.list(nobody(), uriInfo)) {
      assertThat(resp.getStatus()).isEqualTo(200);

      final List<DummyApi> entities = ((Stream<DummyApi>) resp.getEntity()).collect(
          Collectors.toList());
      assertThat(entities).isEmpty();
    }
  }

  @Test
  public void testGetAll_withPartialAccess() {
    final DummyManager manager = mock(DummyManager.class);
    final UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    when(manager.findAll()).thenReturn(Arrays.asList(
        (DummyDto) new DummyDto().setId(1L),
        (DummyDto) new DummyDto().setId(2L),
        (DummyDto) new DummyDto().setId(3L)
    ));

    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        new SingleResourceAuthorizer("2"));

    try (Response resp = resource.list(nobody(), uriInfo)) {
      assertThat(resp.getStatus()).isEqualTo(200);

      final List<DummyApi> entities = ((Stream<DummyApi>) resp.getEntity()).collect(
          Collectors.toList());
      assertThat(1).isEqualTo(entities.size());
      assertThat(2L).isEqualTo(entities.get(0).getId());
    }
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testGet_withNoAccess() {
    final DummyManager manager = mock(DummyManager.class);
    when(manager.findById(1L)).thenReturn((DummyDto) new DummyDto().setId(1L));
    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        ThirdEyeAuthorizerProvider.ALWAYS_DENY);
    resource.get(nobody(), 1L);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testDelete_withNoAccess() {
    final DummyManager manager = mock(DummyManager.class);
    when(manager.findById(1L)).thenReturn((DummyDto) new DummyDto().setId(1L));
    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        ThirdEyeAuthorizerProvider.ALWAYS_DENY);
    resource.delete(nobody(), 1L);
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testDeleteAll_withNoAccess() {
    final DummyManager manager = mock(DummyManager.class);
    when(manager.findAll()).thenReturn(Arrays.asList(
        (DummyDto) new DummyDto().setId(1L),
        (DummyDto) new DummyDto().setId(2L),
        (DummyDto) new DummyDto().setId(3L)
    ));
    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        ThirdEyeAuthorizerProvider.ALWAYS_DENY);
    resource.deleteAll(nobody());
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testDeleteAll_withPartialAccess() {
    final DummyManager manager = mock(DummyManager.class);
    final List<DummyDto> dtos = Arrays.asList(
        (DummyDto) new DummyDto().setId(1L),
        (DummyDto) new DummyDto().setId(2L),
        (DummyDto) new DummyDto().setId(3L)
    );
    when(manager.findAll()).thenReturn(dtos);

    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        new SingleResourceAuthorizer("2"));
    resource.deleteAll(nobody());
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testCreateMultiple_withNoAccess() {
    final DummyManager manager = mock(DummyManager.class);
    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        ThirdEyeAuthorizerProvider.ALWAYS_DENY);

    resource.createMultiple(nobody(),
        Arrays.asList(new DummyApi(), new DummyApi(), new DummyApi()));
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testCreateMultiple_withNoAccessToRelatedResources() {
    final DummyManager manager = mock(DummyManager.class);
    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        ThirdEyeAuthorizerProvider.ALWAYS_DENY);

    resource.createMultiple(
        nobody(),
        Arrays.asList(new DummyApi(), new DummyApi(), new DummyApi())
    );
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testUpdateMultiple_withNoAccess() {
    final DummyManager manager = mock(DummyManager.class);
    when(manager.findById(1L)).thenReturn((DummyDto) new DummyDto().setId(1L));
    when(manager.findById(2L)).thenReturn((DummyDto) new DummyDto().setId(2L));
    when(manager.findById(3L)).thenReturn((DummyDto) new DummyDto().setId(3L));

    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        ThirdEyeAuthorizerProvider.ALWAYS_DENY);

    resource.editMultiple(nobody(), Arrays.asList(
        new DummyApi().setId(1L),
        new DummyApi().setId(2L),
        new DummyApi().setId(3L)
    ));
  }

  @Test(expectedExceptions = ForbiddenException.class)
  public void testUpdateMultiple_withPartialAccess() {
    final DummyManager manager = mock(DummyManager.class);
    when(manager.findById(1L)).thenReturn((DummyDto) new DummyDto().setId(1L));
    when(manager.findById(2L)).thenReturn((DummyDto) new DummyDto().setId(2L));
    when(manager.findById(3L)).thenReturn((DummyDto) new DummyDto().setId(3L));

    final DummyResource resource = new DummyResource(manager, ImmutableMap.of(),
        new SingleResourceAuthorizer("2"));

    resource.editMultiple(nobody(), Arrays.asList(
        new DummyApi().setId(1L),
        new DummyApi().setId(2L),
        new DummyApi().setId(3L)
    ));
  }
}

class DummyDto extends AbstractDTO {

  private String data;

  public String getData() {
    return data;
  }

  public DummyDto setData(final String data) {
    this.data = data;
    return this;
  }
}

class DummyApi implements ThirdEyeCrudApi<DummyApi> {

  private Long id;
  private Timestamp createTime;
  private String createdBy;
  private Timestamp updateTime;
  private String updatedBy;
  private String data;

  public Long getId() {
    return id;
  }

  public DummyApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public Timestamp getCreateTime() {
    return createTime;
  }

  public DummyApi setCreateTime(final Timestamp createTime) {
    this.createTime = createTime;
    return this;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public DummyApi setCreatedBy(final String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public Timestamp getUpdateTime() {
    return updateTime;
  }

  public DummyApi setUpdateTime(final Timestamp updateTime) {
    this.updateTime = updateTime;
    return this;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public DummyApi setUpdatedBy(final String updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }

  public String getData() {
    return data;
  }

  public DummyApi setData(final String data) {
    this.data = data;
    return this;
  }
}

class DummyService extends CrudService<DummyApi, DummyDto> {

  DummyMapper mapper = new DummyMapper();

  public DummyService(final AuthorizationManager authorizationManager,
      final AbstractManager<DummyDto> dtoManager,
      final ImmutableMap<String, String> apiToIndexMap) {
    super(authorizationManager, dtoManager, apiToIndexMap);
  }

  @Override
  protected DummyApi toApi(final DummyDto dto) {
    return mapper.toApi(dto);
  }

  @Override
  protected DummyDto toDto(final DummyApi api) {
    return mapper.toDto(api);
  }
}

class DummyResource extends CrudResource<DummyApi, DummyDto> {

  public DummyResource(
      final DummyManager dtoManager,
      final ImmutableMap<String, String> apiToBeanMap,
      final ThirdEyeAuthorizer thirdEyeAuthorizer) {
    super(new DummyService(
        new AuthorizationManager(mock(AlertTemplateRenderer.class),
            mock(AlertManager.class),
            thirdEyeAuthorizer, new NamespaceResolver(null, null, null, null),
            new AuthConfiguration()),
        dtoManager,
        apiToBeanMap));
  }
}

class DummyManager extends AbstractManagerImpl<DummyDto> {

  protected DummyManager(final GenericPojoDao genericPojoDao) {
    super(DummyDto.class, genericPojoDao);
  }
}

class DummyMapper {

  DummyDto toDto(DummyApi api) {
    return (DummyDto) new DummyDto()
        .setData(api.getData())
        .setId(api.getId())
        .setCreatedBy(api.getCreatedBy())
        .setCreateTime(api.getCreateTime())
        .setUpdatedBy(api.getUpdatedBy())
        .setUpdateTime(api.getUpdateTime());
  }

  DummyApi toApi(DummyDto dto) {
    return new DummyApi()
        .setData(dto.getData())
        .setId(dto.getId())
        .setCreatedBy(dto.getCreatedBy())
        .setCreateTime(dto.getCreateTime())
        .setUpdatedBy(dto.getUpdatedBy())
        .setUpdateTime(dto.getUpdateTime());
  }
}
