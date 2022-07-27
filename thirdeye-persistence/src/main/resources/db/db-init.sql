--
-- Copyright 2022 StarTree Inc
--
-- Licensed under the StarTree Community License (the "License"); you may not use
-- this file except in compliance with the License. You may obtain a copy of the
-- License at http://www.startree.ai/legal/startree-community-license
--
-- Unless required by applicable law or agreed to in writing, software distributed under the
-- License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
-- either express or implied.
-- See the License for the specific language governing permissions and limitations under
-- the License.
--

-- ------------------------------------------
-- MySQL 8.0 Setup
-- ------------------------------------------

-- Create DB
CREATE DATABASE IF NOT EXISTS thirdeye_test
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

-- Create thirdeye user
CREATE USER IF NOT EXISTS 'uthirdeye'@'%' IDENTIFIED BY 'pass';
GRANT ALL PRIVILEGES ON thirdeye_test.* TO 'uthirdeye'@'%';
