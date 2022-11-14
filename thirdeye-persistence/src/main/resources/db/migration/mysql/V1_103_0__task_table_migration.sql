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

INSERT INTO task_entity
(
    id,
    name,
    status,
    type,
    start_time,
    end_time,
    job_id,
    worker_id,
    create_time,
    update_time,
    version,
    last_active,
    json_val
) select
    id,
    name,
    status,
    task_index.type,
    start_time,
    end_time,
    job_id,
    worker_id,
    task_index.create_time,
    task_index.update_time,
    task_index.version,
    last_active,
    json_val
  from task_index
  left join generic_json_entity
  on task_index.base_id = generic_json_entity.id
  where task_index.status = "FAILED" AND task_index.create_time > (NOW() - INTERVAL 30 day) ;
