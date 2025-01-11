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
import json
import os
from pathlib import Path
import platform
import shutil
import subprocess
import tempfile
import time
import argparse
import requests
from retry import retry
from retry.api import retry_call

from setup_local.launch_backend import setup_and_launch_thirdeye_backend
from setup_local.launch_frontend import launch_thirdeye_frontend

PINOT = 'apachepinot/pinot:1.2.0'
PINOT_ARM64 = 'apachepinot/pinot:1.2.0'
TE_UI_DIR = 'thirdeye-ui'

args_parser = argparse.ArgumentParser()
args_parser.add_argument('-r', '--run-only', default=False, help='Use cypress run instead of open', action='store_true')

@retry(delay=15, backoff=2)
def ping_backend():
    print("Pinging backend")
    r = requests.delete('http://localhost:7004/api/alerts/all')
    r.raise_for_status()

@retry(delay=15, backoff=2)
def ping_pinot():
    r = requests.get('http://localhost:9000')
    r.raise_for_status()


def get_path_to_thirdeye_ui():
    path = Path.cwd()
    num_tries = 5

    for num_up_folder in range(num_tries):
        path = path.parent
        if path / TE_UI_DIR in path.iterdir():
            break

    return path / TE_UI_DIR

def replace_docker_compose_config_for_platform(revert=False):
    is_x86 = platform.machine() in ('i386', 'AMD64', 'x86_64')

    from_str = PINOT
    to_str = PINOT_ARM64

    if revert:
        from_str = PINOT_ARM64
        to_str = PINOT

    if not is_x86:
        with open('docker-compose.yaml', 'r') as docker_compose_config_file:
            contents = docker_compose_config_file.read()

        contents = contents.replace(from_str, to_str)

        if PINOT_ARM64 not in contents:
            with open('docker-compose.yaml', 'w') as docker_compose_config_file:
                docker_compose_config_file.write(contents)

def cleanup_lingering_processes():
    grep_keywords = [
        'thirdeye/thirdeye-distribution',
        'thirdeye/thirdeye-ui',
        'webpack',
        'pinot/pinot-distribution'
    ]

    for keyword in grep_keywords:
        result = subprocess.run(f'ps aux | grep {keyword}', shell=True, capture_output=True)
        parsed_output = result.stdout.decode()

        pid = None
        for line in parsed_output.split('\n'):
            if line == '' or f'grep {keyword}' in line:
                continue
            else:
                pid = line.split()[1]

        if pid:
            subprocess.run(['kill', '-9', pid], check=True)


if __name__ == '__main__':
    cleanup_lingering_processes()

    print(get_path_to_thirdeye_ui())

    try:
        replace_docker_compose_config_for_platform()
        with tempfile.TemporaryDirectory() as temp_dir_path:
            subprocess.run(['docker', 'compose', 'up', '-d'],
                           check=True)
            ping_pinot()

            # Check out ThirdEye
            subprocess.run(['git', 'clone', 'https://github.com/startreedata/thirdeye.git'],
                           cwd=temp_dir_path,
                           check=True,
                           stdout=subprocess.DEVNULL,
                           stderr=subprocess.DEVNULL)

            print('[Setup Script] Setting up ThirdEye Backend and Frontend', flush=True)
            backend_process = retry_call(lambda: setup_and_launch_thirdeye_backend(temp_dir_path))
            frontend_process = retry_call(lambda: launch_thirdeye_frontend(get_path_to_thirdeye_ui()))

            time.sleep(60)
            ping_backend()

            args = args_parser.parse_args()
            npx_command = ['npx', 'cypress']
            if args.run_only:
                npx_command.append('run')
                npx_command.append('--browser')
                npx_command.append('chrome')
            else:
                npx_command.append('open')

            subprocess.run(npx_command,
                           cwd=get_path_to_thirdeye_ui(),
                           check=True)
    finally:
        subprocess.run(['docker', 'compose', 'down'])
        cleanup_lingering_processes()
        replace_docker_compose_config_for_platform(True)
