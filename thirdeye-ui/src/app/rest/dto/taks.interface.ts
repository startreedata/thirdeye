// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
export enum TaskStatus {
    COMPLETE = "COMPLETE",
    FAILED = "FAILED",
    WAITING = "WAITING",
    TIMEOUT = "TIMEOUT",
}

export enum TaskType {
    DETECTION = "DETECTION",
    MONITOR = "MONITOR",
    ONBOARDING = "ONBOARDING",
    NOTIFICATION = "NOTIFICATION",
}

export interface Task {
    id: number;
    created: number;
    updated: number;
    taskType: TaskType;
    workerId: number;
    job: {
        jobName: string;
        scheduleStartTime: number;
        scheduleEndTime: number;
        windowStartTime: number;
        windowEndTime: number;
        configId: number;
    };
    status: TaskStatus;
    startTime: number;
    endTime: number;
    taskInfo: string;
    message: string;
    lastActive: number;
}
