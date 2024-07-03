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

ALTER TABLE alert_template_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;
ALTER TABLE anomaly_feedback_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;
ALTER TABLE data_source_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;
ALTER TABLE dataset_config_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;
ALTER TABLE detection_alert_config_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;
ALTER TABLE detection_config_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;
ALTER TABLE enumeration_item_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;
ALTER TABLE event_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;
ALTER TABLE merged_anomaly_result_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;
ALTER TABLE metric_config_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;
ALTER TABLE rca_investigation_index ADD COLUMN namespace varchar(1024) DEFAULT NULL;

-- todo task_entity
