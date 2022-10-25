/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import axios from "axios";
import { Investigation } from "../dto/rca.interfaces";
import {
    createInvestigation,
    deleteInvestigation,
    getCohorts,
    getInvestigation,
    getInvestigations,
    updateInvestigation,
} from "./rca.rest";

jest.mock("axios");

describe("RCA REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getInvestigations should invoke axios.get with appropriate input and return expected investigation", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockInvestigation],
        });

        await expect(getInvestigations()).resolves.toEqual([mockInvestigation]);

        expect(axios.get).toHaveBeenCalledWith("/api/rca/investigations");
    });

    it("getInvestigations should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getInvestigations()).rejects.toThrow("testError");
    });

    it("getInvestigations with anomalyId should invoke axios.get with appropriate input and return appropriate investigations", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockInvestigation, mockInvestigation],
        });

        await expect(getInvestigations(1)).resolves.toEqual([
            mockInvestigation,
            mockInvestigation,
        ]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/rca/investigations?anomaly.id=1"
        );
    });

    it("getInvestigations with alertId should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getInvestigations(1)).rejects.toThrow("testError");
    });

    it("getInvestigation should invoke axios.get with appropriate input and return appropriate investigation", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockInvestigation,
        });

        await expect(getInvestigation(1)).resolves.toEqual(mockInvestigation);

        expect(axios.get).toHaveBeenCalledWith("/api/rca/investigations/1");
    });

    it("getInvestigation should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getInvestigation(1)).rejects.toThrow("testError");
    });

    it("deleteInvestigation should invoke axios.delete with appropriate input and return appropriate investigation", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockInvestigation,
        });

        await expect(deleteInvestigation(1)).resolves.toEqual(
            mockInvestigation
        );

        expect(axios.delete).toHaveBeenCalledWith("/api/rca/investigations/1");
    });

    it("deleteInvestigation should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteInvestigation(1)).rejects.toThrow("testError");
    });

    it("updateInvestigation should invoke axios.put with appropriate input and return appropriate investigation", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockInvestigation],
        });

        await expect(updateInvestigation(mockInvestigation)).resolves.toEqual(
            mockInvestigation
        );

        expect(axios.put).toHaveBeenCalledWith("/api/rca/investigations", [
            mockInvestigation,
        ]);
    });

    it("updateInvestigation should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateInvestigation(mockInvestigation)).rejects.toThrow(
            "testError"
        );
    });

    it("createInvestigation should invoke axios.post with appropriate input and return appropriate investigation", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockInvestigation],
        });

        await expect(createInvestigation(mockInvestigation)).resolves.toEqual(
            mockInvestigation
        );

        expect(axios.post).toHaveBeenCalledWith("/api/rca/investigations", [
            mockInvestigation,
        ]);
    });

    it("createInvestigation should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createInvestigation(mockInvestigation)).rejects.toThrow(
            "testError"
        );
    });

    it("getCohorts should invoke axios.post with appropriate input and return appropriate response", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: mockCohortResponse,
        });

        await expect(
            getCohorts({
                metricId: 123,
                start: 100,
                end: 200,
            })
        ).resolves.toEqual(mockCohortResponse);

        expect(axios.post).toHaveBeenCalledWith("/api/rca/metrics/cohorts", {
            metric: 123,
            start: 100,
            end: 200,
        });
    });

    it("getCohorts should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            getCohorts({
                metricId: 123,
                start: 100,
                end: 200,
            })
        ).rejects.toThrow("testError");
    });
});

const mockError = new Error("testError");

const mockInvestigation = {
    id: 1928705,
    name: "problem investigation",
    text: "Problem is caused by rollout of new feature xx",
    uiMetadata: {
        additionalProp3: [1, 2, 3],
        additionalProp2: 3,
        querySearchString: "yes",
    },
    created: 1651236574971,
    createdBy: {
        principal: "no-auth-user",
    },
    updated: 1651236574971,
    updatedBy: {
        principal: "no-auth-user",
    },
} as Investigation;

const mockCohortResponse = {
    metric: {
        id: 180203,
        name: "views",
        dataset: {
            name: "pageviews",
        },
        active: true,
        updated: 1665465762000,
        datatype: "INT",
        aggregationFunction: "SUM",
    },
    aggregate: 1.1250401e8,
    threshold: 2.81260025e7,
    percentage: 25.0,
    generateEnumerationItems: false,
    dimensions: ["country", "gender", "browser", "version", "device", "os"],
    limit: 100,
    maxDepth: 10,
    resultSize: 25,
    results: [
        {
            dimensionFilters: {
                country: "US",
            },
            value: 4.5076276e7,
            percentage: 40.07,
        },
        {
            dimensionFilters: {
                country: "IN",
            },
            value: 3.9436916e7,
            percentage: 35.05,
        },
    ],
};
