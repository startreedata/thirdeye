package ai.startree.thirdeye.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import ai.startree.thirdeye.datalayer.bao.AbstractManagerImpl;
import ai.startree.thirdeye.datalayer.dao.GenericPojoDao;
import ai.startree.thirdeye.spi.ThirdEyePrincipal;
import ai.startree.thirdeye.spi.api.ThirdEyeCrudApi;
import ai.startree.thirdeye.spi.datalayer.dto.AbstractDTO;
import com.google.common.collect.ImmutableMap;
import com.nimbusds.jwt.JWTClaimsSet;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CrudResourceTest {
  final List<String> emails = List.of("tester1@testing.com", "tester2@testing.com");
  private GenericPojoDao dao;
  private DummyManager manager;
  private DummyResource resource;

  @BeforeClass
  public void setup() {
    dao = mock(GenericPojoDao.class);
    when(dao.put(any())).thenReturn(1L);
    manager = new DummyManager(dao);
    resource = new DummyResource(manager, ImmutableMap.of());
  }

  @Test
  public void createUserInfoTest() {
    final ThirdEyePrincipal principal = new ThirdEyePrincipal(new JWTClaimsSet.Builder()
      .claim("email", emails.get(0)).build());
    final DummyApi api = new DummyApi().setData("testData");

    final Timestamp before = new Timestamp(new Date().getTime());
    List<DummyApi> response = (List<DummyApi>) resource.createMultiple(principal, Collections.singletonList(api)).getEntity();

    assertNotNull(response);
    assertFalse(response.isEmpty());
    final DummyApi responseApi = response.get(0);
    assertEquals(responseApi.getCreatedBy(), emails.get(0));
    assertEquals(responseApi.getUpdatedBy(), emails.get(0));
    assertTrue(responseApi.getCreateTime().after(before));
    assertEquals(responseApi.getCreateTime(), responseApi.getUpdateTime());
  }

  @Test
  public void updateUserInfoTest() {
    final Timestamp before = new Timestamp(new Date().getTime());
    final DummyDto dbDto = new DummyDto().setData("testData");
    dbDto.setId(1L)
      .setCreatedBy(emails.get(0))
      .setCreateTime(before)
      .setUpdatedBy(emails.get(0))
      .setUpdateTime(before);
    when(dao.get(1L, DummyDto.class)).thenReturn(dbDto);

    final DummyApi api = new DummyApi()
      .setId(1L)
      .setData("updateTestData");
    final ThirdEyePrincipal principal = new ThirdEyePrincipal(new JWTClaimsSet.Builder()
      .claim("email", emails.get(1)).build());
    List<DummyApi> response = (List<DummyApi>) resource.editMultiple(principal, Collections.singletonList(api)).getEntity();

    assertNotNull(response);
    assertFalse(response.isEmpty());
    final DummyApi responseApi = response.get(0);
    assertEquals(responseApi.getData(), "updateTestData");
    assertEquals(responseApi.getCreatedBy(), emails.get(0));
    assertEquals(responseApi.getUpdatedBy(), emails.get(1));
    assertTrue(responseApi.getCreateTime().before(responseApi.getUpdateTime()));
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

  DummyMapper mapper = Mappers.getMapper(DummyMapper.class);

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

@Mapper
interface DummyMapper {

  DummyDto toDto(DummyApi api);

  DummyApi toApi(DummyDto dto);
}