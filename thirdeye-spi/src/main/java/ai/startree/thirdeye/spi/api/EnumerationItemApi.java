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
package ai.startree.thirdeye.spi.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.List;
import java.util.Map;

@JsonInclude(Include.NON_NULL)
public class EnumerationItemApi implements ThirdEyeCrudApi<EnumerationItemApi> {

  private Long id;
  private String name;
  private String namespace;
  private String description;
  private Map<String, Object> params;
  private List<AlertApi> alerts;
  private AuthorizationConfigurationApi auth;

  @Override
  public Long getId() {
    return id;
  }

  @Override
  public EnumerationItemApi setId(final Long id) {
    this.id = id;
    return this;
  }

  public String getName() {
    return name;
  }

  public EnumerationItemApi setName(final String name) {
    this.name = name;
    return this;
  }

  public String getNamespace() {
    return namespace;
  }

  public EnumerationItemApi setNamespace(final String namespace) {
    this.namespace = namespace;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public EnumerationItemApi setDescription(final String description) {
    this.description = description;
    return this;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public EnumerationItemApi setParams(final Map<String, Object> params) {
    this.params = params;
    return this;
  }

  public List<AlertApi> getAlerts() {
    return alerts;
  }

  public EnumerationItemApi setAlerts(final List<AlertApi> alerts) {
    this.alerts = alerts;
    return this;
  }

  public AuthorizationConfigurationApi getAuth() {
    return auth;
  }

  public EnumerationItemApi setAuth(final AuthorizationConfigurationApi auth) {
    this.auth = auth;
    return this;
  }
}
