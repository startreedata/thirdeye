-- Before enforcing unique constraint
UPDATE namespace_configuration_entity
SET namespace = '__NULL__'
WHERE namespace IS NULL;

-- Alter the column to disallow NULLs
ALTER TABLE namespace_configuration_entity
MODIFY namespace VARCHAR(200) NOT NULL;

-- Now enforce real uniqueness
ALTER TABLE namespace_configuration_entity
ADD UNIQUE (namespace);