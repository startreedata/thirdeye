/*
 * Copyright 2022 StarTree Inc
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
import axios from "axios";
import { TaskStatus, TaskType } from "../dto/taks.interface";
import { getTasks } from "./tasks.rest";

jest.mock("axios");

describe("Tasks REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getTasks should invoke axios.get with appropriate input and return appropriate anomalies", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockTask],
        });

        await expect(getTasks()).resolves.toEqual([mockTask]);

        expect(axios.get).toHaveBeenCalledWith("/api/tasks?");
    });

    it("getTasks should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getTasks()).rejects.toThrow("testError");
    });

    it("getTasks with list of statuses should invoke axios.get with appropriate input and return appropriate tasks", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockTask],
        });

        await expect(
            getTasks({ status: [TaskStatus.TIMEOUT, TaskStatus.FAILED] })
        ).resolves.toEqual([mockTask]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/tasks?status=%5Bin%5DTIMEOUT%2CFAILED"
        );
    });

    it("getTasks with list of type should invoke axios.get with appropriate input and return appropriate tasks", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockTask],
        });

        await expect(
            getTasks({ type: [TaskType.NOTIFICATION, TaskType.DETECTION] })
        ).resolves.toEqual([mockTask]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/tasks?type=%5Bin%5DNOTIFICATION%2CDETECTION"
        );
    });

    it("getTasks with start and end should invoke axios.get with appropriate input and return appropriate anomalies", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockTask],
        });

        await expect(getTasks({ startTime: 1, endTime: 2 })).resolves.toEqual([
            mockTask,
        ]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/tasks?startTime=%5Bgte%5D1&startTime=%5Blte%5D2"
        );
    });
});

const mockError = new Error("testError");

const mockTask = {
    id: 188756,
    created: 1666285800000,
    updated: 1666285804000,
    taskType: "NOTIFICATION",
    workerId: 3111522896955825700,
    job: {
        jobName: "NOTIFICATION_21361",
        scheduleStartTime: 0,
        scheduleEndTime: 0,
        windowStartTime: 0,
        windowEndTime: 0,
        configId: 0,
    },
    status: "COMPLETED",
    startTime: 1666285803726,
    endTime: 1666285803733,
    taskInfo: '{"detectionAlertConfigId":21361}',
    message: "",
};
