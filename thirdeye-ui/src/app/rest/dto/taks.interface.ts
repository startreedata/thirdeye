/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

export enum TaskStatus {
    COMPLETED = "COMPLETED",
    FAILED = "FAILED",
    WAITING = "WAITING",
    TIMEOUT = "TIMEOUT",
    RUNNING = "RUNNING",
}

export enum TaskType {
    DETECTION = "DETECTION",
    NOTIFICATION = "NOTIFICATION",
}

export enum TaskSubtype {
    DETECTION_HISTORICAL_DATA_AFTER_CREATE = "DETECTION_HISTORICAL_DATA_AFTER_CREATE",
    DETECTION_HISTORICAL_DATA_AFTER_UPDATE = "DETECTION_HISTORICAL_DATA_AFTER_UPDATE",
    DETECTION_HISTORICAL_DATA_MANUAL = "DETECTION_HISTORICAL_DATA_MANUAL",
    DETECTION_TRIGGERED_BY_CRON = "DETECTION_TRIGGERED_BY_CRON",
    NOTIFICATION_TRIGGERED_BY_CRON = "NOTIFICATION_TRIGGERED_BY_CRON",
}

export interface Task {
    id: number;
    created: number;
    updated: number;
    taskType: TaskType;
    taskSubType?: TaskSubtype;
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
