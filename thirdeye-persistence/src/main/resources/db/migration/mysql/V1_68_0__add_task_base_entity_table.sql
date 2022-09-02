
CREATE TABLE IF NOT EXISTS task_base_entity
(
    id          bigint(20) PRIMARY KEY AUTO_INCREMENT,
    json_val    text,
    type        varchar(100),
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10)
    ) ENGINE = InnoDB;
CREATE INDEX task_base_entity_type_idx ON task_base_entity (type);

INSERT INTO task_base_entity (id, json_val, type, create_time, update_time, version)
SELECT * from generic_json_entity where type="TASK";
DELETE from generic_json_entity where type="TASK";
