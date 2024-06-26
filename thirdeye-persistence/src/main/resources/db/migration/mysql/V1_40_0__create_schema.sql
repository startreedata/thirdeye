/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */
CREATE TABLE IF NOT EXISTS generic_json_entity
(
    id          bigint(20) PRIMARY KEY AUTO_INCREMENT,
    json_val    text,
    type        varchar(100),
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10)
    ) ENGINE = InnoDB;
CREATE INDEX generic_json_entity_type_idx ON generic_json_entity (type);

CREATE TABLE IF NOT EXISTS job_index
(
    name                varchar(200) NOT NULL,
    status              varchar(100) NOT NULL,
    type                varchar(100) NOT NULL,
    config_id           bigint(20),
    schedule_start_time bigint(20)   NOT NULL,
    schedule_end_time   bigint(20)   NOT NULL,
    base_id             bigint(20)   NOT NULL,
    create_time         timestamp,
    update_time         timestamp DEFAULT CURRENT_TIMESTAMP,
    version             int(10)
    ) ENGINE = InnoDB;
CREATE INDEX job_status_idx ON job_index (status);
CREATE INDEX job_type_idx ON job_index (type);
CREATE INDEX job_config_id_idx ON job_index (config_id);
CREATE INDEX job_base_id_idx ON job_index (base_id);

CREATE TABLE IF NOT EXISTS task_index
(
    name        varchar(200) NOT NULL,
    status      varchar(100) NOT NULL,
    type        varchar(100) NOT NULL,
    start_time  bigint(20)   NOT NULL,
    end_time    bigint(20)   NOT NULL,
    job_id      bigint(20),
    worker_id   bigint(20),
    base_id     bigint(20)   NOT NULL,
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10)
    ) ENGINE = InnoDB;
CREATE INDEX task_status_idx ON task_index (status);
CREATE INDEX task_type_idx ON task_index (type);
CREATE INDEX task_job_idx ON task_index (job_id);
CREATE INDEX task_base_id_idx ON task_index (base_id);
CREATE INDEX task_name_idx ON task_index (name);
CREATE INDEX task_start_time_idx ON task_index (start_time);
CREATE INDEX task_create_time_idx ON task_index (create_time);
CREATE INDEX task_status_start_time_idx ON task_index (status, start_time);

CREATE TABLE IF NOT EXISTS anomaly_feedback_index
(
    type        varchar(100) NOT NULL,
    base_id     bigint(20)   NOT NULL,
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10)
    ) ENGINE = InnoDB;
CREATE INDEX anomaly_feedback_base_id_idx ON anomaly_feedback_index (base_id);

CREATE TABLE IF NOT EXISTS merged_anomaly_result_index
(
    function_id         bigint(20),
    detection_config_id bigint(20),
    anomaly_feedback_id bigint(20),
    metric_id           bigint(20),
    start_time          bigint(20) NOT NULL,
    end_time            bigint(20) NOT NULL,
    collection          varchar(200),
    metric              varchar(200),
    dimensions          varchar(1023),
    notified            boolean   DEFAULT FALSE,
    base_id             bigint(20) NOT NULL,
    create_time         timestamp,
    update_time         timestamp DEFAULT CURRENT_TIMESTAMP,
    child               boolean,
    version             int(10)
    ) ENGINE = InnoDB;
CREATE INDEX merged_anomaly_result_function_idx ON merged_anomaly_result_index (function_id);
CREATE INDEX merged_anomaly_result_feedback_idx ON merged_anomaly_result_index (anomaly_feedback_id);
CREATE INDEX merged_anomaly_result_metric_idx ON merged_anomaly_result_index (metric_id);
CREATE INDEX merged_anomaly_result_start_time_idx ON merged_anomaly_result_index (start_time);
CREATE INDEX merged_anomaly_result_base_id_idx ON merged_anomaly_result_index (base_id);
CREATE INDEX merged_anomaly_result_detection_config_id_idx ON merged_anomaly_result_index (detection_config_id);

CREATE TABLE IF NOT EXISTS data_source_index
(
    name        varchar(200) NOT NULL,
    type        varchar(200) NOT NULL,
    base_id     bigint(20)   NOT NULL,
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10),
    CONSTRAINT uc_datasource UNIQUE (name)
    ) ENGINE = InnoDB;
CREATE INDEX data_source_index_name_idx ON data_source_index (name);
CREATE INDEX data_source_index_type_idx ON data_source_index (type);

CREATE TABLE IF NOT EXISTS dataset_config_index
(
    dataset                     varchar(200) NOT NULL,
    display_name                varchar(200),
    active                      boolean,
    requires_completeness_check boolean,
    last_refresh_time           bigint(20) DEFAULT 0,
    base_id                     bigint(20)   NOT NULL,
    create_time                 timestamp,
    update_time                 timestamp  DEFAULT CURRENT_TIMESTAMP,
    version                     int(10),
    CONSTRAINT uc_dataset UNIQUE (dataset)
    ) ENGINE = InnoDB;
CREATE INDEX dataset_config_dataset_idx ON dataset_config_index (dataset);
CREATE INDEX dataset_config_active_idx ON dataset_config_index (active);
CREATE INDEX dataset_config_requires_completeness_check_idx ON dataset_config_index (requires_completeness_check);
CREATE INDEX dataset_config_base_id_idx ON dataset_config_index (base_id);
CREATE INDEX dataset_config_display_name_idx ON dataset_config_index (display_name);
CREATE INDEX dataset_config_last_refresh_time_idx ON dataset_config_index (last_refresh_time);

CREATE TABLE IF NOT EXISTS metric_config_index
(
    name        varchar(200) NOT NULL,
    dataset     varchar(200) NOT NULL,
    alias       varchar(400) NOT NULL,
    active      boolean,
    base_id     bigint(20)   NOT NULL,
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10)
    ) ENGINE = InnoDB;
ALTER TABLE `metric_config_index`
    ADD UNIQUE `unique_metric_index` (`name`, `dataset`);
CREATE INDEX metric_config_name_idx ON metric_config_index (name);
CREATE INDEX metric_config_dataset_idx ON metric_config_index (dataset);
CREATE INDEX metric_config_alias_idx ON metric_config_index (alias);
CREATE INDEX metric_config_active_idx ON metric_config_index (active);
CREATE INDEX metric_config_base_id_idx ON metric_config_index (base_id);

CREATE TABLE IF NOT EXISTS override_config_index
(
    start_time    bigint(20)   NOT NULL,
    end_time      bigint(20)   NOT NULL,
    target_entity varchar(100) NOT NULL,
    active        boolean   DEFAULT FALSE,
    base_id       bigint(20)   NOT NULL,
    create_time   timestamp,
    update_time   timestamp DEFAULT CURRENT_TIMESTAMP,
    version       int(10)
    ) ENGINE = InnoDB;
CREATE INDEX override_config_target_entity_idx ON override_config_index (target_entity);
CREATE INDEX override_config_target_start_time_idx ON override_config_index (start_time);
CREATE INDEX override_config_base_id_idx ON override_config_index (base_id);

CREATE TABLE IF NOT EXISTS event_index
(
    name         VARCHAR(100),
    event_type   VARCHAR(100),
    start_time   bigint(20) NOT NULL,
    end_time     bigint(20) NOT NULL,
    metric_name  VARCHAR(200),
    service_name VARCHAR(200),
    base_id      bigint(20) NOT NULL,
    create_time  timestamp,
    update_time  timestamp DEFAULT CURRENT_TIMESTAMP,
    version      int(10)
    ) ENGINE = InnoDB;
CREATE INDEX event_event_type_idx ON event_index (event_type);
CREATE INDEX event_start_time_idx ON event_index (start_time);
CREATE INDEX event_end_time_idx ON event_index (end_time);
CREATE INDEX event_base_id_idx ON event_index (base_id);

CREATE TABLE IF NOT EXISTS detection_status_index
(
    function_id          bigint(20),
    dataset              varchar(200) NOT NULL,
    date_to_check_in_ms  bigint(20)   NOT NULL,
    date_to_check_in_sdf varchar(20),
    detection_run        boolean,
    base_id              bigint(20)   NOT NULL,
    create_time          timestamp,
    update_time          timestamp DEFAULT CURRENT_TIMESTAMP,
    version              int(10)
    ) ENGINE = InnoDB;
ALTER TABLE `detection_status_index`
    ADD UNIQUE `detection_status_unique_index` (`function_id`, `date_to_check_in_sdf`);
CREATE INDEX detection_status_dataset_idx ON detection_status_index (dataset);
CREATE INDEX detection_status_date_idx ON detection_status_index (date_to_check_in_ms);
CREATE INDEX detection_status_sdf_idx ON detection_status_index (date_to_check_in_sdf);
CREATE INDEX detection_status_run_idx ON detection_status_index (detection_run);
CREATE INDEX detection_status_function_idx ON detection_status_index (function_id);
CREATE INDEX detection_status_base_id_idx ON detection_status_index (base_id);

CREATE TABLE IF NOT EXISTS autotune_config_index
(
    function_id                   bigint(20),
    start_time                    bigint(20) NOT NULL,
    end_time                      bigint(20) NOT NULL,
    performance_evaluation_method varchar(200),
    autotune_method               varchar(200),
    base_id                       bigint(20) NOT NULL,
    create_time                   timestamp,
    update_time                   timestamp DEFAULT CURRENT_TIMESTAMP,
    version                       int(10)
    ) ENGINE = InnoDB;
CREATE INDEX autotune_config_function_idx ON autotune_config_index (function_id);
CREATE INDEX autotune_config_autoTuneMethod_idx ON autotune_config_index (autotune_method);
CREATE INDEX autotune_config_performanceEval_idx ON autotune_config_index (performance_evaluation_method);
CREATE INDEX autotune_config_start_time_idx ON autotune_config_index (start_time);
CREATE INDEX autotune_config_base_id_idx ON autotune_config_index (base_id);

CREATE TABLE IF NOT EXISTS entity_to_entity_mapping_index
(
    from_urn     varchar(255) NOT NULL,
    to_urn       varchar(255) NOT NULL,
    mapping_type varchar(400) NOT NULL,
    base_id      bigint(20)   NOT NULL,
    create_time  timestamp,
    update_time  timestamp DEFAULT CURRENT_TIMESTAMP,
    version      int(10)
    ) ENGINE = InnoDB;
ALTER TABLE `entity_to_entity_mapping_index`
    ADD UNIQUE `entity_mapping_unique_index` (`from_urn`, `to_urn`);
CREATE INDEX entity_mapping_from_urn_idx ON entity_to_entity_mapping_index (from_urn);
CREATE INDEX entity_mapping_to_urn_idx ON entity_to_entity_mapping_index (to_urn);
CREATE INDEX entity_mapping_type_idx ON entity_to_entity_mapping_index (mapping_type);
CREATE INDEX entity_to_entity_mapping_base_id_idx ON entity_to_entity_mapping_index (base_id);

CREATE TABLE IF NOT EXISTS onboard_dataset_metric_index
(
    dataset_name varchar(200) NOT NULL,
    metric_name  varchar(500),
    data_source  varchar(500) NOT NULL,
    onboarded    boolean,
    base_id      bigint(20)   NOT NULL,
    create_time  timestamp,
    update_time  timestamp DEFAULT CURRENT_TIMESTAMP,
    version      int(10)
    ) ENGINE = InnoDB;
CREATE INDEX onboard_dataset_idx ON onboard_dataset_metric_index (dataset_name);
CREATE INDEX onboard_metric_idx ON onboard_dataset_metric_index (metric_name);
CREATE INDEX onboard_datasource_idx ON onboard_dataset_metric_index (data_source);
CREATE INDEX onboard_onboarded_idx ON onboard_dataset_metric_index (onboarded);
CREATE INDEX onboard_dataset_metric_base_id_idx ON onboard_dataset_metric_index (base_id);

CREATE TABLE IF NOT EXISTS rootcause_session_index
(
    base_id             bigint(20) NOT NULL,
    create_time         timestamp,
    update_time         timestamp DEFAULT CURRENT_TIMESTAMP,
    name                varchar(256),
    owner               varchar(32),
    previousId          bigint(20),
    anomalyId           bigint(20),
    anomaly_range_start bigint(8),
    anomaly_range_end   bigint(8),
    created             bigint(8),
    updated             bigint(8),
    version             int(10)
    ) ENGINE = InnoDB;
CREATE INDEX rootcause_session_name_idx ON rootcause_session_index (name);
CREATE INDEX rootcause_session_owner_idx ON rootcause_session_index (owner);
CREATE INDEX rootcause_session_previousId_idx ON rootcause_session_index (previousId);
CREATE INDEX rootcause_session_anomalyId_idx ON rootcause_session_index (anomalyId);
CREATE INDEX rootcause_session_anomaly_range_start_idx ON rootcause_session_index (anomaly_range_start);
CREATE INDEX rootcause_session_anomaly_range_end_idx ON rootcause_session_index (anomaly_range_end);
CREATE INDEX rootcause_session_created_idx ON rootcause_session_index (created);
CREATE INDEX rootcause_session_updated_idx ON rootcause_session_index (updated);
CREATE INDEX rootcause_session_base_id_idx ON rootcause_session_index (base_id);

CREATE TABLE IF NOT EXISTS alert_template_index
(
    name        varchar(200) NOT NULL,
    base_id     bigint(20)   NOT NULL,
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10),
    CONSTRAINT uc_alert_template UNIQUE (name)
    ) ENGINE = InnoDB;
CREATE INDEX alert_template_index_name_idx ON alert_template_index (name);

CREATE TABLE IF NOT EXISTS detection_config_index
(
    base_id     bigint(20)   NOT NULL,
    `name`      VARCHAR(256) NOT NULL,
    active      BOOLEAN,
    created_by  VARCHAR(256),
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10)
    ) ENGINE = InnoDB;
ALTER TABLE `detection_config_index`
    ADD UNIQUE `detection_config_unique_index` (`name`);
CREATE INDEX detection_config_base_id_idx ON detection_config_index (base_id);
CREATE INDEX detection_config_name_idx ON detection_config_index (`name`);
CREATE INDEX detection_config_active_idx ON detection_config_index (active);
CREATE INDEX detection_config_created_by_index ON detection_config_index (created_by);

CREATE TABLE IF NOT EXISTS detection_alert_config_index
(
    base_id     bigint(20)   NOT NULL,
    `name`      VARCHAR(256) NOT NULL,
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10)
    ) ENGINE = InnoDB;
ALTER TABLE `detection_alert_config_index`
    ADD UNIQUE `detection_alert_config_unique_index` (`name`);
CREATE INDEX detection_alert_config_base_id_idx ON detection_alert_config_index (base_id);
CREATE INDEX detection_alert_config_name_idx ON detection_alert_config_index (`name`);

CREATE TABLE IF NOT EXISTS evaluation_index
(
    base_id             bigint(20) NOT NULL,
    detection_config_id bigint(20) NOT NULL,
    start_time          bigint(20) NOT NULL,
    end_time            bigint(20) NOT NULL,
    detectorName        VARCHAR(128),
    mape                double,
    create_time         timestamp,
    update_time         timestamp DEFAULT CURRENT_TIMESTAMP,
    version             int(10)
    ) ENGINE = InnoDB;
ALTER TABLE `evaluation_index`
    ADD UNIQUE `evaluation_index` (`detection_config_id`, `start_time`, `end_time`);
CREATE INDEX evaluation_base_id_idx ON evaluation_index (base_id);
CREATE INDEX evaluation_detection_config_id_idx ON evaluation_index (detection_config_id);
CREATE INDEX evaluation_detection_start_time_idx ON evaluation_index (start_time);

CREATE TABLE IF NOT EXISTS rootcause_template_index
(
    base_id     bigint(20)   NOT NULL,
    `name`      VARCHAR(256) NOT NULL,
    owner       varchar(32)  NOT NULL,
    metric_id   bigint(20)   NOT NULL,
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10)
    ) ENGINE = InnoDB;
ALTER TABLE `rootcause_template_index`
    ADD UNIQUE `rootcause_template_index` (`name`);
CREATE INDEX rootcause_template_id_idx ON rootcause_template_index (base_id);
CREATE INDEX rootcause_template_owner_idx ON rootcause_template_index (owner);
CREATE INDEX rootcause_template_metric_idx ON rootcause_template_index (metric_id);

CREATE TABLE IF NOT EXISTS online_detection_data_index
(
    base_id     bigint(20) NOT NULL,
    dataset     varchar(200),
    metric      varchar(200),
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10)
    ) ENGINE = InnoDB;
CREATE INDEX online_detection_data_id_idx ON online_detection_data_index (base_id);
CREATE INDEX online_detection_data_dataset_idx ON online_detection_data_index (dataset);
CREATE INDEX online_detection_data_metric_idx ON online_detection_data_index (metric);

CREATE TABLE IF NOT EXISTS anomaly_subscription_group_notification_index
(
    base_id             bigint(20) NOT NULL,
    anomaly_id          bigint(20) NOT NULL,
    detection_config_id bigint(20) NOT NULL,
    create_time         timestamp,
    update_time         timestamp DEFAULT CURRENT_TIMESTAMP,
    version             int(10)
    ) ENGINE = InnoDB;
ALTER TABLE `anomaly_subscription_group_notification_index`
    ADD UNIQUE `anomaly_subscription_group_notification_index` (anomaly_id);
CREATE INDEX anomaly_subscription_group_anomaly_idx ON anomaly_subscription_group_notification_index (anomaly_id);
CREATE INDEX anomaly_subscription_group_detection_config_idx ON anomaly_subscription_group_notification_index (anomaly_id)
