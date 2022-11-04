// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import {
    useGetAnomalyDimensionAnalysis,
    useGetAnomalyMetricBreakdown,
    useGetInvestigation,
    useGetInvestigations,
} from "./rca.actions";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("RCA Actions", () => {
    describe("useGetAnomalyMetricBreakdown", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAnomalyMetricBreakdown());

            expect(result.current.anomalyMetricBreakdown).toBeNull();
            expect(result.current.getMetricBreakdown).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({ data: mockDataResponse });
            const { result, waitFor } = renderHook(() =>
                useGetAnomalyMetricBreakdown()
            );

            await act(async () => {
                const promise = result.current.getMetricBreakdown(123, {
                    baselineOffset: mockBaselineOffset,
                });

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.anomalyMetricBreakdown).toBeNull();
                expect(result.current.getMetricBreakdown).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.anomalyMetricBreakdown).toEqual(
                        mockDataResponse
                    );
                    expect(result.current.getMetricBreakdown).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });

    describe("useGetAnomalyDimensionAnalysis", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() =>
                useGetAnomalyDimensionAnalysis()
            );

            expect(result.current.anomalyDimensionAnalysisData).toBeNull();
            expect(result.current.getDimensionAnalysisData).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: mockDimensionAnalysisResult,
            });
            const { result, waitFor } = renderHook(() =>
                useGetAnomalyDimensionAnalysis()
            );

            await act(async () => {
                const promise = result.current.getDimensionAnalysisData(123, {
                    baselineOffset: mockBaselineOffset,
                });

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.anomalyDimensionAnalysisData).toBeNull();
                expect(result.current.getDimensionAnalysisData).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.anomalyDimensionAnalysisData).toEqual(
                        mockDimensionAnalysisResult
                    );
                    expect(
                        result.current.getDimensionAnalysisData
                    ).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });

    describe("useGetInvestigations", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetInvestigations());

            expect(result.current.investigations).toBeNull();
            expect(result.current.getInvestigations).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: [mockInvestigation],
            });
            const { result, waitFor } = renderHook(() =>
                useGetInvestigations()
            );

            await act(async () => {
                const promise = result.current.getInvestigations();

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.investigations).toBeNull();
                expect(result.current.getInvestigations).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.investigations).toEqual([
                        mockInvestigation,
                    ]);
                    expect(result.current.getInvestigations).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });

    describe("useGetInvestigation", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetInvestigation());

            expect(result.current.investigation).toBeNull();
            expect(result.current.getInvestigation).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: mockInvestigation,
            });
            const { result, waitFor } = renderHook(() => useGetInvestigation());

            await act(async () => {
                const promise = result.current.getInvestigation(1);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.investigation).toBeNull();
                expect(result.current.getInvestigation).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.investigation).toEqual(
                        mockInvestigation
                    );
                    expect(result.current.getInvestigation).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });
});

const mockDataResponse = {
    id: 1,
};

const mockDimensionAnalysisResult = {
    metric: {
        name: "views",
        dataset: {
            name: "pageviews",
        },
    },
    baselineTotal: 1064166,
    currentTotal: 901666,
    baselineTotalSize: 1064166,
    currentTotalSize: 901666,
    globalRatio: 0.8473,
    dimensions: ["browser", "version", "device"],
    responseRows: [
        {
            baselineValue: 531524,
            currentValue: 541000,
            sizeFactor: 0.54558273545247,
            percentageChange: "1.7828%",
            contributionChange: "10.0526%",
            contributionToOverallChange: "5.8314%",
            names: ["(ALL)-", "", ""],
            otherDimensionValues: ["others", "firefox", "ie", "safari"],
            moreOtherDimensionNumber: 0,
            cost: 0,
        },
    ],
    gainer: [],
    loser: [
        {
            baselineValue: 532642,
            currentValue: 360666,
            sizeFactor: 0.45441726454753,
            percentageChange: "-32.2874%",
            contributionChange: "-10.0526%",
            contributionToOverallChange: "-105.8314%",
            dimensionName: "browser",
            dimensionValue: "chrome",
            cost: "8674.5062",
        },
    ],
    dimensionCosts: [
        {
            name: "browser",
            cost: 8674.506240721543,
        },
        {
            name: "version",
            cost: 0.024159221341796367,
        },
    ],
};

const mockInvestigation = {
    id: 1928705,
    name: "problem investigation",
    text: "Problem is caused by rollout of new feature xx",
    uiMetadata: {
        additionalProp3: [1, 2, 3],
        additionalProp2: 3,
        additionalProp1: "yes",
    },
    created: 1651236574971,
    createdBy: {
        principal: "no-auth-user",
    },
    updated: 1651236574971,
    updatedBy: {
        principal: "no-auth-user",
    },
};

const mockBaselineOffset = "P1W";
