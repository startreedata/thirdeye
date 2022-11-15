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
import { AlertTemplate } from "../dto/alert-template.interfaces";
import {
    createAlertTemplate,
    createDefaultAlertTemplates,
    deleteAlertTemplate,
    getAlertTemplate,
    getAlertTemplates,
    updateAlertTemplate,
} from "./alert-templates.rest";

jest.mock("axios");

describe("Alert Templates REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getAlertTemplate should invoke axios.get with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAlertTemplateResponse,
        });

        await expect(getAlertTemplate(1)).resolves.toEqual(
            mockAlertTemplateResponse
        );

        expect(axios.get).toHaveBeenCalledWith("/api/alert-templates/1");
    });

    it("getAlertTemplate should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAlertTemplate(1)).rejects.toThrow("testError");
    });

    it("getAlertTemplates should invoke axios.get with appropriate input and return appropriate alerts", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAlertTemplateResponse],
        });

        await expect(getAlertTemplates()).resolves.toEqual([
            mockAlertTemplateResponse,
        ]);

        expect(axios.get).toHaveBeenCalledWith("/api/alert-templates");
    });

    it("getAlertTemplates should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAlertTemplates()).rejects.toThrow("testError");
    });

    it("createAlertTemplate should invoke axios.post with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockAlertTemplateResponse],
        });

        await expect(
            createAlertTemplate(mockAlertTemplateRequest)
        ).resolves.toEqual(mockAlertTemplateResponse);

        expect(axios.post).toHaveBeenCalledWith("/api/alert-templates", [
            mockAlertTemplateRequest,
        ]);
    });

    it("createAlertTemplate should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            createAlertTemplate(mockAlertTemplateRequest)
        ).rejects.toThrow("testError");
    });

    it("createDefaultAlertTemplates should invoke axios.post with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockDefaultAlertTemplatesResponse],
        });

        await expect((await createDefaultAlertTemplates())[0]).toEqual(
            mockDefaultAlertTemplatesResponse
        );

        expect(axios.post).toHaveBeenCalledWith(
            "/api/alert-templates/load-defaults",
            "updateExisting=false"
        );
    });

    it("createDefaultAlertTemplates should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createDefaultAlertTemplates()).rejects.toThrow(
            "testError"
        );
    });

    it("updateAlertTemplate should invoke axios.put with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockAlertTemplateResponse],
        });

        await expect(
            updateAlertTemplate(mockAlertTemplateRequest)
        ).resolves.toEqual(mockAlertTemplateResponse);

        expect(axios.put).toHaveBeenCalledWith("/api/alert-templates", [
            mockAlertTemplateRequest,
        ]);
    });

    it("updateAlertTemplate should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(
            updateAlertTemplate(mockAlertTemplateRequest)
        ).rejects.toThrow("testError");
    });

    it("deleteAlertTemplate should invoke axios.delete with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockAlertTemplateResponse,
        });

        await expect(deleteAlertTemplate(1)).resolves.toEqual(
            mockAlertTemplateResponse
        );

        expect(axios.delete).toHaveBeenCalledWith("/api/alert-templates/1");
    });

    it("deleteAlertTemplate should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteAlertTemplate(1)).rejects.toThrow("testError");
    });
});

const mockAlertTemplateRequest = {
    name: "testNameAlertRequest",
} as AlertTemplate;

const mockAlertTemplateResponse = {
    name: "testNameAlertResponse",
};

const mockDefaultAlertTemplatesResponse = [
    {
        id: 13371,
        name: "startree-percentage-rule",
        description:
            "Percentage rule template. Aggregation function with 1 operand: SUM, MAX,etc...",
        nodes: [
            {
                name: "root",
                type: "PostProcessor",
                params: {
                    type: "THRESHOLD",
                    "component.min": "${thresholdFilterMin}",
                    "component.ignore": "${thresholdIgnore}",
                    "component.max": "${thresholdFilterMax}",
                },
                inputs: [{ sourcePlanNode: "timeOfWeekProcessor" }],
                outputs: [],
            },
            {
                name: "timeOfWeekProcessor",
                type: "PostProcessor",
                params: {
                    "component.hoursOfDay": "${hoursOfDay}",
                    type: "TIME_OF_WEEK",
                    "component.dayHoursOfWeek": "${dayHoursOfWeek}",
                    "component.daysOfWeek": "${daysOfWeek}",
                    "component.ignore": "${timeOfWeekIgnore}",
                },
                inputs: [{ sourcePlanNode: "coldStartProcessor" }],
                outputs: [],
            },
            {
                name: "coldStartProcessor",
                type: "PostProcessor",
                params: {
                    "component.tableName": "${dataset}",
                    type: "COLD_START",
                    "component.ignore": "${coldStartIgnore}",
                    "component.coldStartPeriod": "${baselineOffset}",
                },
                inputs: [{ sourcePlanNode: "anomalyDetector" }],
                outputs: [],
            },
            {
                name: "anomalyDetector",
                type: "AnomalyDetector",
                params: {
                    "component.percentageChange": "${percentageChange}",
                    "component.monitoringGranularity":
                        "${monitoringGranularity}",
                    "component.metric": "met",
                    "anomaly.dataset": "${dataset}",
                    "component.timestamp": "ts",
                    "anomaly.metric": "${aggregationColumn}",
                    "anomaly.source": "percentage-change-template/root",
                    type: "PERCENTAGE_CHANGE",
                    "component.pattern": "${pattern}",
                },
                inputs: [
                    {
                        targetProperty: "baseline",
                        sourcePlanNode: "baselineMissingDataManager",
                        sourceProperty: "baselineOutput",
                    },
                    {
                        targetProperty: "current",
                        sourcePlanNode: "currentMissingDataManager",
                        sourceProperty: "currentOutput",
                    },
                ],
                outputs: [],
            },
            {
                name: "baselineMissingDataManager",
                type: "TimeIndexFiller",
                params: { "component.timestamp": "ts" },
                inputs: [
                    {
                        sourcePlanNode: "baselineDataFetcher",
                        sourceProperty: "baselineOutput",
                    },
                ],
                outputs: [{ outputName: "baselineOutput" }],
            },
            {
                name: "baselineDataFetcher",
                type: "DataFetcher",
                params: {
                    "component.tableName": "${dataset}",
                    "component.dataSource": "${dataSource}",
                    "component.query":
                        "SELECT __timeGroup(\"${timeColumn}\", '${timeColumnFormat}', '${monitoringGranularity}') as ts, ${aggregationFunction}" +
                        "(${aggregationColumn}) as met FROM ${dataset} WHERE __timeFilter(\"${timeColumn}\", '${timeColumnFormat}', '${baselineOffset}'" +
                        ", '${baselineOffset}') ${queryFilters} GROUP BY ts ORDER BY ts LIMIT ${queryLimit}",
                },
                inputs: [],
                outputs: [{ outputKey: "pinot", outputName: "baselineOutput" }],
            },
            {
                name: "currentMissingDataManager",
                type: "TimeIndexFiller",
                params: { "component.timestamp": "ts" },
                inputs: [
                    {
                        sourcePlanNode: "currentDataFetcher",
                        sourceProperty: "currentOutput",
                    },
                ],
                outputs: [{ outputName: "currentOutput" }],
            },
            {
                name: "currentDataFetcher",
                type: "DataFetcher",
                params: {
                    "component.tableName": "${dataset}",
                    "component.dataSource": "${dataSource}",
                    "component.query":
                        "SELECT __timeGroup(\"${timeColumn}\", '${timeColumnFormat}', '${monitoringGranularity}') as ts, ${aggregationFunction}" +
                        "(${aggregationColumn}) as met FROM ${dataset} WHERE __timeFilter(\"${timeColumn}\", '${timeColumnFormat}') ${queryFilters} GROUP BY ts ORDER BY ts LIMIT ${queryLimit}",
                },
                inputs: [],
                outputs: [{ outputKey: "pinot", outputName: "currentOutput" }],
            },
        ],
        metadata: {
            datasource: { name: "${dataSource}" },
            dataset: {
                name: "${dataset}",
                dimensions: "${rcaIncludedDimensions}",
                completenessDelay: "${completenessDelay}",
                rcaExcludedDimensions: "${rcaExcludedDimensions}",
            },
            metric: {
                name: "${aggregationColumn}",
                aggregationFunction: "${rcaAggregationFunction}",
                where: "${queryFilters}",
            },
            granularity: "${monitoringGranularity}",
            timezone: "${timezone}",
            mergeMaxGap: "${mergeMaxGap}",
            mergeMaxDuration: "${mergeMaxDuration}",
        },
        defaultProperties: {
            timezone: "UTC",
            timeColumn: "AUTO",
            timeColumnFormat: "",
            pattern: "UP_OR_DOWN",
            completenessDelay: "P0D",
            mergeMaxGap: "",
            mergeMaxDuration: "",
            rcaAggregationFunction: "",
            queryFilters: "",
            rcaIncludedDimensions: [],
            rcaExcludedDimensions: [],
            coldStartIgnore: "true",
            timeOfWeekIgnore: "true",
            daysOfWeek: [],
            hoursOfDay: [],
            dayHoursOfWeek: {},
            thresholdIgnore: "true",
            thresholdFilterMin: "-1",
            thresholdFilterMax: "-1",
            queryLimit: "100000000",
        },
    },
];

const mockError = new Error("testError");
