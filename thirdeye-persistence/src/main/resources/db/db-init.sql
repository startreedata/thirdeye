-- ------------------------------------------
-- MySQL 5.7 Setup
-- ------------------------------------------

-- Create DB
CREATE DATABASE thirdeye_test
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- Create admin user
CREATE USER 'uthirdeyeadmin'@'%' IDENTIFIED BY 'pass';
GRANT ALL PRIVILEGES ON thirdeye_test.* TO 'uthirdeyeadmin'@'%' WITH GRANT OPTION;

-- Create app user
CREATE USER 'uthirdeye'@'%' IDENTIFIED BY 'pass';
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON thirdeye_test.* TO 'uthirdeye'@'%';
