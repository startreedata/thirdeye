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
package ai.startree.thirdeye.resources;

import static ai.startree.thirdeye.auth.ThirdEyePrincipal.NAME_CLAIM;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ai.startree.thirdeye.auth.ThirdEyePrincipal;
import ai.startree.thirdeye.datalayer.bao.AbstractManagerImpl;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.authorization.AccessControlIdentifiers;
import ai.startree.thirdeye.spi.authorization.AccessType;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.google.common.collect.ImmutableMap;
import com.nimbusds.jwt.JWTClaimsSet;
import java.net.http.HttpHeaders;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CrudResourceTest {

  final List<String> emails = List.of("tester1@testing.com", "tester2@testing.com");
  private DummyManager manager;
  private DummyResource resource;

  @BeforeClass
  public void setup() {
    manager = mock(DummyManager.class);// new DummyManager(dao);
    when(manager.save(any(DummyDto.class))).thenAnswer((Answer<Long>) invocationOnMock -> {
      ((DummyDto) invocationOnMock.getArgument(0)).setId(1L);
      return 1L;
    });
    when(manager.update(any(DummyDto.class))).thenReturn(1);
    resource = new DummyResource(manager, ImmutableMap.of());
  }

  @Test
  public void createUserInfoTest() {
    final ThirdEyePrincipal owner = getPrincipal(emails.get(0));
    final DummyApi api = new DummyApi().setData("testData");

    final Timestamp before = getCurrentTime();
    List<DummyApi> response = (List<DummyApi>) resource.createMultiple(owner, singletonList(api)).getEntity();

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
    final Timestamp before = getCurrentTime();
    final ThirdEyePrincipal owner = getPrincipal(emails.get(0));
    final ThirdEyePrincipal updater = getPrincipal(emails.get(1));

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

    List<DummyApi> response = (List<DummyApi>) resource.editMultiple(updater, singletonList(api)).getEntity();

    assertThat(response).isNotNull();
    assertThat(response.isEmpty()).isFalse();
    final DummyApi responseApi = response.get(0);
    assertThat(responseApi.getData()).isEqualTo("updateTestData");
    assertThat(responseApi.getCreatedBy()).isEqualTo(owner.getName());
    assertThat(responseApi.getUpdatedBy()).isEqualTo(updater.getName());
    assertThat(responseApi.getCreateTime().before(responseApi.getUpdateTime())).isTrue();
  }

  private ThirdEyePrincipal getPrincipal(String name) {
    return new ThirdEyePrincipal(new JWTClaimsSet.Builder().claim(NAME_CLAIM, name).build());
  }

  private Timestamp getCurrentTime() {
    return new Timestamp(new Date().getTime());
  }

  @Test
  public void testGetAll_withNoAccess() {
    reset(manager);
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    when(manager.findAll()).thenReturn(Arrays.asList(
        (DummyDto) new DummyDto().setId(1L),
        (DummyDto) new DummyDto().setId(2L),
        (DummyDto) new DummyDto().setId(3L)
    ));

    resource.accessControl = (AccessControlIdentifiers identifiers,
        AccessType accessType, HttpHeaders httpHeaders) -> false;

    try (Response resp = resource.getAll(new ThirdEyePrincipal("nobody"), uriInfo, null)) {
      assertThat(resp.getStatus()).isEqualTo(200);

      List<DummyApi> entities = ((Stream<DummyApi>) resp.getEntity()).collect(Collectors.toList());
      assertThat(entities).isEmpty();
    }
  }

  @Test
  public void testGetAll_withPartialAccess() {
    reset(manager);
    UriInfo uriInfo = mock(UriInfo.class);
    when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());
    when(manager.findAll()).thenReturn(Arrays.asList(
        (DummyDto) new DummyDto().setId(1L),
        (DummyDto) new DummyDto().setId(2L),
        (DummyDto) new DummyDto().setId(3L)
    ));

    resource.accessControl = (AccessControlIdentifiers identifiers,
        AccessType accessType, HttpHeaders httpHeaders) -> identifiers.name.equals("2");

    try (Response resp = resource.getAll(new ThirdEyePrincipal("nobody"), uriInfo, null)) {
      assertThat(resp.getStatus()).isEqualTo(200);

      List<DummyApi> entities = ((Stream<DummyApi>) resp.getEntity()).collect(Collectors.toList());
      assertThat(entities.size()).isEqualTo(1);
      assertThat(entities.get(0).getId()).isEqualTo(2L);
    }
  }

  @Test
  public void testGet_withNoAccess() {
    reset(manager);
    when(manager.findById(1L)).thenReturn((DummyDto) new DummyDto().setId(1L));

    resource.accessControl = (AccessControlIdentifiers identifiers,
        AccessType accessType, HttpHeaders httpHeaders) -> false;

    try (Response resp = resource.get(new ThirdEyePrincipal("nobody"), 1L, null)) {
      assertThat(resp.getStatus()).isEqualTo(403);
    }
  }

  @Test
  public void testDelete_withNoAccess() {
    reset(manager);
    when(manager.findById(1L)).thenReturn((DummyDto) new DummyDto().setId(1L));

    resource.accessControl = (AccessControlIdentifiers identifiers,
        AccessType accessType, HttpHeaders httpHeaders) -> false;

    try (Response resp = resource.delete(new ThirdEyePrincipal("nobody"), 1L, null)) {
      assertThat(resp.getStatus()).isEqualTo(403);
    }
  }

  @Test
  public void testDeleteAll_withNoAccess() {
    reset(manager);
    when(manager.findAll()).thenReturn(Arrays.asList(
        (DummyDto) new DummyDto().setId(1L),
        (DummyDto) new DummyDto().setId(2L),
        (DummyDto) new DummyDto().setId(3L)
    ));

    resource.accessControl = (AccessControlIdentifiers identifiers,
        AccessType accessType, HttpHeaders httpHeaders) -> false;

    try (Response resp = resource.deleteAll(new ThirdEyePrincipal("nobody"), null)) {
      assertThat(resp.getStatus()).isEqualTo(200);
      verify(manager, never()).delete(any());
    }
  }

  @Test
  public void testDeleteAll_withPartialAccess() {
    reset(manager);
    var dtos = Arrays.asList(
        (DummyDto) new DummyDto().setId(1L),
        (DummyDto) new DummyDto().setId(2L),
        (DummyDto) new DummyDto().setId(3L)
    );
    when(manager.findAll()).thenReturn(dtos);

    resource.accessControl = (AccessControlIdentifiers identifiers,
        AccessType accessType, HttpHeaders httpHeaders) -> identifiers.name.equals("2");

    try (Response resp = resource.deleteAll(new ThirdEyePrincipal("nobody"), null)) {
      assertThat(resp.getStatus()).isEqualTo(200);
      verify(manager).delete(dtos.get(1));
      verify(manager, never()).delete(dtos.get(0));
      verify(manager, never()).delete(dtos.get(2));
    }
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

class DummyResource extends CrudResource<DummyApi, DummyDto> {

  DummyMapper mapper = new DummyMapper();

  public DummyResource(
    final DummyManager dtoManager,
    final ImmutableMap<String, String> apiToBeanMap) {
    super(dtoManager, apiToBeanMap);
  }

  @Override
  protected DummyDto createDto(final ThirdEyePrincipal principal, final DummyApi api) {
    return mapper.toDto(api);
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
