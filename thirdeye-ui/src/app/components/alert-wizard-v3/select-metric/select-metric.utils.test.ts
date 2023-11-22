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

import { EditableAlert } from "../../../rest/dto/alert.interfaces";
import {
    Datasource,
    DatasourceMetaList,
    DatasourceProperties,
} from "../../../rest/dto/datasource.interfaces";
import { Metric } from "../../../rest/dto/metric.interfaces";
import { DatasetInfo } from "../../../utils/datasources/datasources.util";
import {
    determineDatasetInitialSelectionsFromServerData,
    generateTemplateProperties,
} from "./select-metric.utils";

describe("AlertWizardV3/Threshold Setup Utils", () => {
    it("generateTemplateProperties should return object with expected values extracted", () => {
        expect(
            generateTemplateProperties(
                "mockMetric",
                {
                    id: 1,
                    dataSource: mockDatasource,
                    name: "mockDataset",
                    active: true,
                    additive: true,
                    dimensions: [],
                    timeColumn: {
                        name: "mockTimeColumn",
                        format: "mockFormat",
                        timezone: "pst",
                        interval: 1,
                    },
                },
                "mockAggregationFunction",
                "P1D"
            )
        ).toEqual({
            dataSource: "mockDatasource",
            dataset: "mockDataset",
            aggregationFunction: "mockAggregationFunction",
            aggregationColumn: "mockMetric",
            monitoringGranularity: "P1D",
            timezone: "pst",
        });
    });

    it("resetSelectedMetrics should call the expected set functions", () => {
        const [dataset, metric] =
            determineDatasetInitialSelectionsFromServerData(
                mockDatasets as DatasetInfo[],
                {
                    name: "",
                    cron: "",
                    description: "",
                    templateProperties: {
                        dataset: "mockDataset2",
                        dataSource: "mockDatasource2",
                        aggregationColumn: "mockMetric2",
                        aggregationFunction: "AVG",
                    },
                } as EditableAlert
            );

        expect(dataset).toEqual(mockDatasets[1]);
        expect(metric).toEqual("mockMetric2");
    });

    it("resetSelectedMetrics should call some of the expected set functions", () => {
        const [dataset, metric] =
            determineDatasetInitialSelectionsFromServerData(
                mockDatasets as DatasetInfo[],
                {
                    name: "",
                    cron: "",
                    description: "",
                    templateProperties: {
                        dataset: "mockDataset2",
                        dataSource: "mockDatasource2",
                    },
                } as EditableAlert
            );

        expect(dataset).toEqual(mockDatasets[1]);
        expect(metric).toBeNull();
    });
});

const mockDatasource = {
    id: 1,
    name: "mockDatasource",
    type: "testTypeDatasource1",
    properties: {
        zookeeperUrl: "testURLDatasource1",
        clusterName: "testClusterDatasource1",
        controllerConnectionScheme: "http",
        controllerHost: "localhost",
        controllerPort: 9000,
        cacheLoaderClassName: "testCacheLoaderClassNameDatasource1",
    } as DatasourceProperties,
    metaList: [
        {
            classRef: "testClassRefDatasource1",
            properties: {},
        } as DatasourceMetaList,
    ],
} as Datasource;

const mockMetrics = [
    {
        name: "mockMetric1",
    },
    {
        name: "mockMetric2",
    },
    {
        name: "mockMetric3",
    },
];

const mockDatasets = [
    {
        dataset: {
            name: "mockDataset1",
            dataSource: {
                name: "mockDatasource1",
            },
        },
        metrics: mockMetrics as Metric[],
    },
    {
        dataset: {
            name: "mockDataset2",
            dataSource: {
                name: "mockDatasource2",
            },
        },
        metrics: mockMetrics as Metric[],
    },
];
