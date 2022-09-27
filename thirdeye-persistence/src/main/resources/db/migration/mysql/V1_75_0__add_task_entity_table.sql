CREATE TABLE IF NOT EXISTS task_entity
(
    id          bigint(20) PRIMARY KEY AUTO_INCREMENT,
    name        varchar(200) NOT NULL,
    status      varchar(100) NOT NULL,
    type        varchar(100) NOT NULL,
    start_time  bigint(20)   NOT NULL,
    end_time    bigint(20)   NOT NULL,
    job_id      bigint(20),
    worker_id   bigint(20),
    create_time timestamp,
    update_time timestamp DEFAULT CURRENT_TIMESTAMP,
    version     int(10),
    last_active timestamp DEFAULT CURRENT_TIMESTAMP,
    json_val    text
    ) ENGINE = InnoDB;
CREATE INDEX task_status_idx ON task_entity (status);
CREATE INDEX task_type_idx ON task_entity (type);
CREATE INDEX task_job_idx ON task_entity (job_id);
CREATE INDEX task_id_idx ON task_entity (id);
CREATE INDEX task_name_idx ON task_entity (name);
CREATE INDEX task_start_time_idx ON task_entity (start_time);
CREATE INDEX task_create_time_idx ON task_entity (create_time);
CREATE INDEX task_status_start_time_idx ON task_entity (status, start_time);
CREATE INDEX task_last_active_idx ON task_entity (last_active);