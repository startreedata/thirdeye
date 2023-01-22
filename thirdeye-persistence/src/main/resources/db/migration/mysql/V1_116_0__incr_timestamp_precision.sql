-- Update create_time column
ALTER TABLE alert_template_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE anomaly_feedback_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE anomaly_subscription_group_notification_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE autotune_config_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE data_source_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE dataset_config_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE detection_alert_config_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE detection_config_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE detection_status_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE entity_to_entity_mapping_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE evaluation_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE event_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE generic_json_entity
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE job_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE merged_anomaly_result_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE metric_config_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE onboard_dataset_metric_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE online_detection_data_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE override_config_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE rca_investigation_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE rootcause_template_index
    MODIFY COLUMN create_time TIMESTAMP(3);
ALTER TABLE task_index
    MODIFY COLUMN create_time TIMESTAMP(3);

-- Update update_time column
ALTER TABLE generic_json_entity
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE merged_anomaly_result_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE job_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE task_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE anomaly_feedback_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE data_source_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE dataset_config_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE metric_config_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE override_config_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE event_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE detection_status_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE autotune_config_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE entity_to_entity_mapping_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE onboard_dataset_metric_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE rca_investigation_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE alert_template_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE detection_config_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE detection_alert_config_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE evaluation_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE rootcause_template_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE online_detection_data_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);
ALTER TABLE anomaly_subscription_group_notification_index
    MODIFY COLUMN update_time TIMESTAMP(3) DEFAULT CURRENT_TIMESTAMP(3);

