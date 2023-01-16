--
-- Copyright 2023 StarTree Inc
--
-- Licensed under the StarTree Community License (the "License"); you may not use
-- this file except in compliance with the License. You may obtain a copy of the
-- License at http://www.startree.ai/legal/startree-community-license
--
-- Unless required by applicable law or agreed to in writing, software distributed under the
-- License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
-- either express or implied.
-- See the License for the specific language governing permissions and limitations under
-- the License.
--

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS anomaly_feedback_index;
DROP TABLE IF EXISTS anomaly_subscription_group_notification_index;
DROP TABLE IF EXISTS application_index;
DROP TABLE IF EXISTS autotune_config_index;
DROP TABLE IF EXISTS data_source_index;
DROP TABLE IF EXISTS dataset_config_index;
DROP TABLE IF EXISTS detection_alert_config_index;
DROP TABLE IF EXISTS detection_config_index;
DROP TABLE IF EXISTS detection_status_index;
DROP TABLE IF EXISTS entity_to_entity_mapping_index;
DROP TABLE IF EXISTS evaluation_index;
DROP TABLE IF EXISTS event_index;
DROP TABLE IF EXISTS generic_json_entity;
DROP TABLE IF EXISTS job_index;
DROP TABLE IF EXISTS merged_anomaly_result_index;
DROP TABLE IF EXISTS metric_config_index;
DROP TABLE IF EXISTS onboard_dataset_metric_index;
DROP TABLE IF EXISTS online_detection_data_index;
DROP TABLE IF EXISTS override_config_index;
DROP TABLE IF EXISTS rootcause_session_index;
DROP TABLE IF EXISTS rootcause_template_index;
DROP TABLE IF EXISTS task_index;

SET FOREIGN_KEY_CHECKS = 1;
