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
import { sortBy } from "lodash";
import { useCallback, useEffect, useState } from "react";
import { GetDatasets } from "../../rest/datasets/dataset.interfaces";
import { useGetDatasets } from "../../rest/datasets/datasets.actions";
import { useGetDatasources } from "../../rest/datasources/datasources.actions";
import { GetDatasources } from "../../rest/datasources/datasources.interfaces";
import { useGetMetrics } from "../../rest/metrics/metrics.actions";
import { GetMetrics } from "../../rest/metrics/metrics.interface";
import { buildPinotDatasourcesTree, DatasetInfo } from "./datasources.util";

export interface UseGetDatasourcesTreeHook {
    datasetsInfo: DatasetInfo[] | null;
    getDatasourcesHook: GetDatasources;
    getDatasetsHook: GetDatasets;
    getMetricsHook: GetMetrics;
}

function useGetDatasourcesTree(): UseGetDatasourcesTreeHook {
    const getDatasourcesHook = useGetDatasources();
    const getDatasetsHook = useGetDatasets();
    const getMetricsHook = useGetMetrics();

    const [datasetsInfo, setDatasetsInfo] = useState<DatasetInfo[] | null>(
        null
    );

    const refreshDatasets = useCallback(() => {
        setDatasetsInfo(null);
        getDatasetsHook.getDatasets();
        getMetricsHook.getMetrics();
        getDatasourcesHook.getDatasources();
    }, [
        getDatasetsHook.getDatasets,
        getMetricsHook.getMetrics,
        getDatasourcesHook.getDatasources,
    ]);

    useEffect(() => {
        refreshDatasets();
    }, []);

    // Build the table configuration tree
    useEffect(() => {
        if (
            !getMetricsHook.metrics ||
            !getDatasetsHook.datasets ||
            !getDatasourcesHook.datasources
        ) {
            return;
        }

        const datasourceInfo = buildPinotDatasourcesTree(
            getDatasourcesHook.datasources,
            getDatasetsHook.datasets,
            getMetricsHook.metrics
        );
        const datasetInfo = sortBy(
            datasourceInfo.reduce((previous: DatasetInfo[], dSource) => {
                return [...previous, ...dSource.tables];
            }, []),
            [(d) => d.dataset.name.toLowerCase()]
        );

        setDatasetsInfo(datasetInfo);
    }, [
        getMetricsHook.metrics,
        getDatasetsHook.datasets,
        getDatasourcesHook.datasources,
    ]);

    return {
        datasetsInfo,
        getDatasourcesHook,
        getDatasetsHook,
        getMetricsHook,
    };
}

export { useGetDatasourcesTree };
