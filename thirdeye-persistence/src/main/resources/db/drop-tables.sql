/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

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
