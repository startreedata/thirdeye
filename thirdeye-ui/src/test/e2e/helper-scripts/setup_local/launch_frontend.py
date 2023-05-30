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
import subprocess
import sys

SUBPROCESS_OUTPUT = subprocess.DEVNULL
if os.environ.get('DEBUG_FRONTEND') == 'true':
    SUBPROCESS_OUTPUT = sys.__stdout__

def launch_thirdeye_frontend(working_directory):
    print('[Setup Script] Launching ThirdEye Frontend Use "DEBUG_FRONTEND=true; python3 launch_fresh_be_with_current_fe.py;" to output logs', flush=True)

    modified_env = os.environ.copy()
    modified_env['TE_DEV_PROXY_SERVER'] = 'http://localhost:8080'
    thirdeye_frontend_process = subprocess.Popen(['npm', 'run', 'start'],
                                                 cwd=working_directory,
                                                 env=modified_env,
                                                 stdout=SUBPROCESS_OUTPUT,
                                                 stderr=SUBPROCESS_OUTPUT)
    return thirdeye_frontend_process
