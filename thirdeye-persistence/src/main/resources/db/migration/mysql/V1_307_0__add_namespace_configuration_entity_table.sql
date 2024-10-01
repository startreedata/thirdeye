CREATE TABLE IF NOT EXISTS namespace_configuration_entity
(
    id                             bigint(20) PRIMARY KEY AUTO_INCREMENT,
    namespace                      varchar(200),
    timezone                       varchar(100) NOT NULL,
    date_time_pattern              varchar(100) NOT NULL,
    minimum_onboarding_start_time  bigint(20)   NOT NULL,
    create_time                    timestamp,
    update_time                    timestamp DEFAULT CURRENT_TIMESTAMP,
    json_val                       text
    ) ENGINE = InnoDB;
CREATE INDEX namespace_configuration_id_idx ON namespace_configuration_entity (id);
CREATE INDEX namespace_configuration_namespace_idx ON namespace_configuration_entity (namespace);
CREATE INDEX namespace_configuration_create_time_idx ON namespace_configuration_entity (create_time);
CREATE INDEX namespace_configuration_update_time_idx ON namespace_configuration_entity (update_time);