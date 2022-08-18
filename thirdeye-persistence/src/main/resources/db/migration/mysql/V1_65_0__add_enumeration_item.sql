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

CREATE TABLE IF NOT EXISTS enumeration_item_index
(
    name        varchar(200) NOT NULL,
    base_id     bigint(20)   NOT NULL,
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10),
    json_val    text
) ENGINE = InnoDB;
CREATE INDEX enumeration_item_name_idx ON enumeration_item_index (name);
CREATE INDEX enumeration_item_params_idx ON enumeration_item_index (json_val(500));

