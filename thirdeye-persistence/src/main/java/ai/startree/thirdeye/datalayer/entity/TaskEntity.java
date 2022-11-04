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
package ai.startree.thirdeye.datalayer.entity;

import java.sql.Timestamp;

public class TaskEntity extends AbstractEntity implements HasJsonVal<TaskEntity>{

  private String name;
  private String status;
  private String type;
  private long startTime;
  private long endTime;
  private long jobId;
  private long workerId;
  private Timestamp lastActive;
  private String jsonVal;

  @Override
  public String getJsonVal() {
    return this.jsonVal;
  }

  @Override
  public TaskEntity setJsonVal(final String jsonVal) {
    this.jsonVal = jsonVal;
    return this;
  }

  public String getName() {
    return name;
  }

  public TaskEntity setName(final String name) {
    this.name = name;
    return this;
  }

  public String getStatus() {
    return status;
  }

  public TaskEntity setStatus(final String status) {
    this.status = status;
    return this;
  }

  public String getType() {
    return type;
  }

  public TaskEntity setType(final String type) {
    this.type = type;
    return this;
  }

  public long getStartTime() {
    return startTime;
  }

  public TaskEntity setStartTime(final long startTime) {
    this.startTime = startTime;
    return this;
  }

  public long getEndTime() {
    return endTime;
  }

  public TaskEntity setEndTime(final long endTime) {
    this.endTime = endTime;
    return this;
  }

  public long getJobId() {
    return jobId;
  }

  public TaskEntity setJobId(final long jobId) {
    this.jobId = jobId;
    return this;
  }

  public long getWorkerId() {
    return workerId;
  }

  public TaskEntity setWorkerId(final long workerId) {
    this.workerId = workerId;
    return this;
  }

  public Timestamp getLastActive() {
    return lastActive;
  }

  public TaskEntity setLastActive(final Timestamp lastActive) {
    this.lastActive = lastActive;
    return this;
  }
}
