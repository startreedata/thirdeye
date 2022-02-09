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

package ai.startree.thirdeye.notification.commons;

import com.google.common.base.MoreObjects;
import java.util.Objects;

public class JiraConfiguration {

  private String url;
  private String user;
  private String password;
  private String defaultProject = "THIRDEYE";
  private Long jiraIssueTypeId = 19L;

  public String getJiraHost() {
    return url;
  }

  public void setJiraHost(String jiraHost) {
    this.url = jiraHost;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public void setDefaultProject(String defaultProject) {
    this.defaultProject = defaultProject;
  }

  public String getDefaultProject() {
    return defaultProject;
  }

  public void setJiraIssueTypeId(Long jiraIssueTypeId) {
    this.jiraIssueTypeId = jiraIssueTypeId;
  }

  public Long getJiraIssueTypeId() {
    return jiraIssueTypeId;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof JiraConfiguration)) {
      return false;
    }
    JiraConfiguration at = (JiraConfiguration) o;
    return Objects.equals(url, at.getJiraHost())
        && Objects.equals(user, at.getUser())
        && Objects.equals(password, at.getPassword())
        && Objects.equals(defaultProject, at.getDefaultProject())
        && Objects.equals(jiraIssueTypeId, at.getJiraIssueTypeId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, user, password, defaultProject);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("url", url)
        .add("user", user)
        .add("password", password)
        .add("defaultProject", defaultProject)
        .add("issueTypeId", jiraIssueTypeId)
        .toString();
  }
}
