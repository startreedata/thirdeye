CREATE TABLE IF NOT EXISTS namespace_configuration_entity
(
    id                             bigint(20) PRIMARY KEY AUTO_INCREMENT,
    namespace                      varchar(200),
    timezone                       varchar(100) NOT NULL,
    date_time_pattern              varchar(100) NOT NULL,
    minimum_onboarding_start_time  bigint(20)   NOT NULL,
    create_time                    timestamp,
    version                        int(10),
    update_time                    timestamp DEFAULT CURRENT_TIMESTAMP,
    json_val                       text
    ) ENGINE = InnoDB;
ALTER TABLE namespace_configuration_entity ADD UNIQUE unique_namespace (namespace);
CREATE INDEX namespace_configuration_id_idx ON namespace_configuration_entity (id);
CREATE INDEX namespace_configuration_namespace_idx ON namespace_configuration_entity (namespace);