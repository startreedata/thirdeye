/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

-- ------------------------------------------
-- MySQL 8.0 Setup
-- ------------------------------------------

-- Create DB
CREATE DATABASE thirdeye
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- Create thirdeye user
CREATE USER 'uthirdeye'@'%' IDENTIFIED BY 'pass';
GRANT ALL PRIVILEGES ON thirdeye.* TO 'uthirdeye'@'%';
