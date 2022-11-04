// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import axios from "axios";
import { getAppAnalytics } from "./app-analytics.rest";

jest.mock("axios");

describe("App Analytics REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getAppAnalytics should invoke axios.get with appropriate input and return appropriate response", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAppAnalyticsResponse,
        });

        await expect(getAppAnalytics()).resolves.toEqual(
            mockAppAnalyticsResponse
        );

        expect(axios.get).toHaveBeenCalledWith("/api/app-analytics");
    });

    it("getAppAnalytics should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAppAnalytics()).rejects.toThrow("testError");
    });
});

const mockAppAnalyticsResponse = {
    version: "1.87.0-b152c0e410dc4199239f57f15ad6d512516940b4",
    nMonitoredMetrics: 11,
    anomalyStats: {
        totalCount: 2355,
        countWithFeedback: 47,
        feedbackStats: {
            ANOMALY: 12,
            NOT_ANOMALY: 3,
            NO_FEEDBACK: 1,
            ANOMALY_EXPECTED: 29,
            ANOMALY_NEW_TREND: 2,
        },
    },
};

const mockError = new Error("testError");
