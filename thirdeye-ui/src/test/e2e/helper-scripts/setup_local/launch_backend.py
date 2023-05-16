#!/usr/bin/env python
# Copyright 2023 StarTree Inc
#
# Licensed under the StarTree Community License (the "License"); you may not use
# this file except in compliance with the License. You may obtain a copy of the
# License at http://www.startree.ai/legal/startree-community-license
#
# Unless required by applicable law or agreed to in writing, software distributed under the
# License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
# either express or implied.
#
# See the License for the specific language governing permissions and limitations under
# the License.
import os
import re
import subprocess
import sys
import time
import urllib.parse

SUBPROCESS_OUTPUT = subprocess.DEVNULL
if os.environ.get('DEBUG_BACKEND') == 'true':
    SUBPROCESS_OUTPUT = sys.__stdout__

def bootstrap_sample_pinot_data(working_directory):
    print('[Setup Script] Bootstrapping USStoreSalesOrderData data into pinot', flush=True)
    schema_path = os.path.join(working_directory, 'thirdeye/examples/USStoreSalesOrderData/schema.json')
    subprocess.run(['curl', '--location', '--request', 'POST', 'http://localhost:9000/schemas',
                    '--header', 'Content-Type: application/json',
                    '--data-binary', f'@{schema_path}'],
                   check=True,
                   stdout=SUBPROCESS_OUTPUT,
                   stderr=SUBPROCESS_OUTPUT)
    table_config_path = os.path.join(working_directory, 'thirdeye/examples/USStoreSalesOrderData/table_config.json')
    subprocess.run(['curl', '--location', '--request', 'POST', 'http://localhost:9000/tables',
                    '--header', 'Content-Type: application/json',
                    '--data-binary', f'@{table_config_path}'],
                   check=True,
                   stdout=SUBPROCESS_OUTPUT,
                   stderr=SUBPROCESS_OUTPUT)
    csv_path = os.path.join(working_directory, 'thirdeye/examples/USStoreSalesOrderData/rawdata/data.csv')
    subprocess.run(['curl', '--location', '--request', 'POST',
                    'http://localhost:9000/ingestFromFile?tableNameWithType=USStoreSalesOrderData_OFFLINE&batchConfigMapStr=' + urllib.parse.quote(
                        '{\"inputFormat":\"csv\", \"recordReader.prop.delimiter\":\",\"}'),
                    '--header', 'Content-Type: multipart/form-data',
                    '--form', f'file=@"{csv_path}"'],
                   check=True,
                   stdout=SUBPROCESS_OUTPUT,
                   stderr=SUBPROCESS_OUTPUT)


def replace_after_substring(content, substring, to_replace, replace_to):
    idx_of_substring = content.index(substring)
    before = content[:idx_of_substring]
    after = content[idx_of_substring:]
    after = after.replace(to_replace, replace_to, 1)
    return before + after


def setup_thirdeye_backend(working_directory):
    print('[Setup Script] Setting up ThirdEye. Use "DEBUG_BACKEND=true; python3 launch_fresh_be_with_current_fe.py;" to output logs', flush=True)
    # Build ThirdEye
    subprocess.run(['./mvnw', '-T', '1C', 'install', '-DskipTests', '-pl', '!thirdeye-ui'],
                   cwd=os.path.join(working_directory, 'thirdeye'),
                   check=True,
                   stdout=SUBPROCESS_OUTPUT,
                   stderr=SUBPROCESS_OUTPUT)


def config_thirdeye(working_directory):
    print('[Setup Script] Modifying ThirdEye configuration', flush=True)
    # Modify config to turn off scheduler and taskDriver so fixture data doesn't change
    config_file_path = os.path.join(working_directory, 'thirdeye', 'config', 'server.yaml')

    with open(config_file_path, 'r') as server_yaml_file:
        config_contents = server_yaml_file.read()

        # This usually happens within Docker environment
        if os.environ.get("MYSQL_HOST"):
            new_mysql_host = os.environ.get("MYSQL_HOST")
            config_contents = config_contents.replace('127.0.0.1:3306/thirdeye_test',
                                                      f'{new_mysql_host}:3306/thirdeye_test')

        config_contents = replace_after_substring(config_contents, 'scheduler:', 'enabled: true', 'enabled: false')
        config_contents = replace_after_substring(config_contents, 'taskDriver:', 'enabled: true', 'enabled: false')
    with open(config_file_path, 'w') as server_yaml_file:
        server_yaml_file.write(config_contents)


def bootstrap_mysql(working_directory):
    print('[Setup Script] Setting up MySQL Server', flush=True)

    SCRIPT_LOCATION = 'thirdeye-persistence/src/main/resources/db/db-init.sql'
    subprocess.run(" ".join([
        'mysql',
        '-u',
        'root',
        '--password=te-e2e-pass',
        '--host',
        os.environ.get("MYSQL_HOST", "127.0.0.1"),
        '-P',
        '3306',
        '<',
        SCRIPT_LOCATION
    ]),
        cwd=os.path.join(working_directory, 'thirdeye'),
        shell=True,
        check=True,
        stderr=SUBPROCESS_OUTPUT,
        stdout=SUBPROCESS_OUTPUT)


def launch_thirdeye(working_directory):
    print('[Setup Script] Launching ThirdEye', flush=True)
    # Find the snapshot version for current using regex
    pattern = r'thirdeye-distribution-(\d*\.\d*\.\d*)-SNAPSHOT-dist'
    subdirs = os.listdir(os.path.join(working_directory, 'thirdeye/thirdeye-distribution/target/'))

    version = None
    for subdir in subdirs:
        result = re.match(pattern, subdir)
        if result:
            version = result.groups()[0]
            break

    if version is None:
        raise Exception('No snapshot directory matching: ', pattern)

    # Launch thirdeye and the ui
    bin_directory = f'thirdeye-distribution/target/thirdeye-distribution-{version}-SNAPSHOT-dist/thirdeye-distribution-{version}-SNAPSHOT'

    thirdeye_backend_process = subprocess.Popen(['bin/thirdeye.sh', 'server'],
                                                cwd=os.path.join(working_directory, 'thirdeye', bin_directory),
                                                stdout=SUBPROCESS_OUTPUT,
                                                stderr=SUBPROCESS_OUTPUT)

    return thirdeye_backend_process


def setup_and_launch_thirdeye_backend(working_directory):
    setup_thirdeye_backend(working_directory)
    config_thirdeye(working_directory)
    bootstrap_sample_pinot_data(working_directory)
    bootstrap_mysql(working_directory)
    return launch_thirdeye(working_directory)
