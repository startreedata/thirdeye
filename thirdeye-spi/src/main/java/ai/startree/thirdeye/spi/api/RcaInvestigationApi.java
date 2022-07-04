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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.Date;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class RcaInvestigationApi implements ThirdEyeCrudApi<RcaInvestigationApi> {

  private Long id;
  private String name;
  private String text;
  private Map<String, Object> uiMetadata;
  private AnomalyApi anomaly;
  private Date created;
  private UserApi createdBy;
  private Date updated;
  private UserApi updatedBy;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public RcaInvestigationApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public RcaInvestigationApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getText() {
    return text;
  }

  public RcaInvestigationApi setText(final String text) {
    this.text = text;
    return this;
  }

  public Map<String, Object> getUiMetadata() {
    return uiMetadata;
  }

  public RcaInvestigationApi setUiMetadata(final Map<String, Object> uiMetadata) {
    this.uiMetadata = uiMetadata;
    return this;
  }

  public AnomalyApi getAnomaly() {
    return anomaly;
  }

  public RcaInvestigationApi setAnomaly(final AnomalyApi anomaly) {
    this.anomaly = anomaly;
    return this;
  }

  public Date getCreated() {
    return created;
  }

  public RcaInvestigationApi setCreated(final Date created) {

    this.created = created;
    return this;
  }

  public UserApi getCreatedBy() {
    return createdBy;
  }

  public RcaInvestigationApi setCreatedBy(final UserApi createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  public Date getUpdated() {
    return updated;
  }

  public RcaInvestigationApi setUpdated(final Date updated) {
    this.updated = updated;
    return this;
  }

  public UserApi getUpdatedBy() {
    return updatedBy;
  }

  public RcaInvestigationApi setUpdatedBy(final UserApi updatedBy) {
    this.updatedBy = updatedBy;
    return this;
  }
}
