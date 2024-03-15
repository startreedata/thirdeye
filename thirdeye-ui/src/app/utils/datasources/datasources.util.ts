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
import i18n from "i18next";
import { cloneDeep, isEmpty, omit } from "lodash";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import {
    Datasource,
    DatasourceProperties,
} from "../../rest/dto/datasource.interfaces";
import { Metric, MetricAggFunction } from "../../rest/dto/metric.interfaces";
import { UiDatasource } from "../../rest/dto/ui-datasource.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const STAR_COLUMN = "*";

export const createDefaultDatasource = (): Datasource => {
    if (window.location.host.includes("localhost")) {
        return {
            name: "mypinot",
            type: "pinot",
            defaultQueryOptions: { timeoutMs: "30000" },
            properties: {
                zookeeperUrl: "localhost:2123",
                clusterName: "QuickStartCluster",
                controllerConnectionScheme: "http",
                controllerHost: "localhost",
                controllerPort: 9000,
                brokerUrl: "localhost:8000",
            } as DatasourceProperties,
        } as unknown as Datasource;
    }

    return {
        name: "mypinot",
        type: "pinot",
        defaultQueryOptions: { timeoutMs: "30000" },
        properties: {
            zookeeperUrl:
                "pinot-zookeeper-headless.managed.svc.cluster.local:2181",
            clusterName: "pinot",
            controllerConnectionScheme: "https",
            controllerHost:
                "pinot-pinot-controller-headless.managed.svc.cluster.local",
            controllerPort: 9000,
        } as DatasourceProperties,
    } as unknown as Datasource;
};

export const createEmptyUiDatasource = (): UiDatasource => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
        type: noDataMarker,
        datasource: null,
    };
};

export const getUiDatasource = (datasource: Datasource): UiDatasource => {
    if (!datasource) {
        return createEmptyUiDatasource();
    }

    return getUiDatasourceInternal(datasource);
};

export const getUiDatasources = (datasources: Datasource[]): UiDatasource[] => {
    if (isEmpty(datasources)) {
        return [];
    }

    const uiDatasources = [];
    for (const datasource of datasources) {
        uiDatasources.push(getUiDatasourceInternal(datasource));
    }

    return uiDatasources;
};

export const filterDatasources = (
    uiDatasources: UiDatasource[],
    searchWords: string[]
): UiDatasource[] => {
    if (isEmpty(uiDatasources)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return uiDatasources;
    }

    const filteredUiDatasources = [];
    for (const uiDatasource of uiDatasources) {
        // Only the UI datasource to be searched and not contained datasource
        const uiDatasourceCopy = cloneDeep(uiDatasource);
        uiDatasourceCopy.datasource = null;

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    uiDatasourceCopy,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredUiDatasources.push(uiDatasource);

                break;
            }
        }
    }

    return filteredUiDatasources;
};

export const omitNonUpdatableData = (datasource: Datasource): Datasource => {
    const newDatasource = omit(datasource, "id");

    return newDatasource as Datasource;
};

const getUiDatasourceInternal = (datasource: Datasource): UiDatasource => {
    const uiDatasource = createEmptyUiDatasource();
    const noDataMarker = i18n.t("label.no-data-marker");

    // Maintain a copy of datasource
    uiDatasource.datasource = datasource;

    // Basic properties
    uiDatasource.id = datasource.id;
    uiDatasource.name = datasource.name || noDataMarker;
    uiDatasource.type = datasource.type || noDataMarker;

    return uiDatasource;
};

export interface DatasetInfo {
    dataset: Dataset;
    metrics: Metric[];
    dimensions: string[];
    datasource: string;
}

export interface DatasourceInfo {
    name: string;
    tables: DatasetInfo[];
}

export const buildPinotDatasourcesTree = (
    datasources: Datasource[],
    datasets: Dataset[],
    metrics: Metric[]
): DatasourceInfo[] => {
    const datasetToInfo: {
        [key: string]: DatasetInfo;
    } = {};

    datasets
        .filter((dataset) => dataset.active)
        .forEach((d) => {
            metrics.push({
                id: -1,
                name: STAR_COLUMN,
                aggregationColumn: STAR_COLUMN,
                dataset: d,
                datatype: "DOUBLE",
                aggregationFunction: MetricAggFunction.COUNT,
            });
            datasetToInfo[d.name] = {
                datasource: d.dataSource.name,
                dataset: d,
                metrics: [],
                dimensions: d.dimensions,
            };
        });

    metrics.forEach((metric) => {
        const datasetInfo = datasetToInfo[metric.dataset.name];

        if (datasetInfo) {
            datasetInfo.metrics.push(metric);
        }
    });

    const datasourceToInfo: {
        [key: string]: DatasourceInfo;
    } = {};

    datasources.forEach((ds) => {
        datasourceToInfo[ds.name] = {
            name: ds.name,
            tables: [],
        };
    });

    Object.values(datasetToInfo).forEach((tableInfo) => {
        const bucket = datasourceToInfo[tableInfo.dataset.dataSource.name];

        if (bucket && tableInfo.metrics.length > 0) {
            bucket.tables.push(tableInfo);
        }
    });

    return Object.values(datasourceToInfo);
};
