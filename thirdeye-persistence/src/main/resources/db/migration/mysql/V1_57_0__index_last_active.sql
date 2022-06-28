ALTER TABLE task_index ADD COLUMN last_active timestamp DEFAULT CURRENT_TIMESTAMP;
CREATE INDEX task_last_active_idx ON task_index (last_active);
