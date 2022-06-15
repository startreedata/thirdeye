///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

import i18n from "i18next";
import { isEmpty } from "lodash";
import { formatNumberV1 } from "../../platform/utils";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import {
    LogicalMetric,
    LogicalView,
    Metric,
    MetricAggFunction,
} from "../../rest/dto/metric.interfaces";
import { UiMetric } from "../../rest/dto/ui-metric.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const createEmptyUiMetric = (): UiMetric => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        name: noDataMarker,
        datasetId: -1,
        datasetName: noDataMarker,
        active: false,
        activeText: noDataMarker,
        aggregationColumn: noDataMarker,
        aggregationFunction: noDataMarker as MetricAggFunction,
        views: [],
        viewCount: formatNumberV1(0),
    };
};

export const createEmptyMetric = (): LogicalMetric => {
    return {
        name: "",
        active: true,
        aggregationFunction: "SUM" as MetricAggFunction,
        dataset: { name: "" } as Dataset,
        rollupThreshold: 0,
    };
};

export const createEmptyMetricLogicalView = (): LogicalView => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        name: noDataMarker,
        query: noDataMarker,
    };
};

export const getUiMetric = (metric: Metric): UiMetric => {
    const uiMetric = createEmptyUiMetric();

    if (!metric) {
        return uiMetric;
    }

    const noDataMarker = i18n.t("label.no-data-marker");

    // Basic properties
    uiMetric.id = metric.id;
    uiMetric.name = metric.name || noDataMarker;
    uiMetric.active = Boolean(metric.active);
    uiMetric.activeText = metric.active
        ? i18n.t("label.active")
        : i18n.t("label.inactive");
    uiMetric.aggregationColumn = metric.aggregationColumn || noDataMarker;
    uiMetric.aggregationFunction = metric.aggregationFunction || noDataMarker;

    // Dataset properties
    if (metric.dataset) {
        uiMetric.datasetId = metric.dataset.id;
        uiMetric.datasetName = metric.dataset.name || noDataMarker;
    }

    // Logical view properties
    if (!metric.views) {
        return uiMetric;
    }

    for (const view of metric.views) {
        const metricLocicalView = createEmptyMetricLogicalView();
        metricLocicalView.name = view.name || noDataMarker;
        metricLocicalView.query = view.query || noDataMarker;

        uiMetric.views.push(metricLocicalView);
    }
    uiMetric.viewCount = formatNumberV1(uiMetric.views.length);

    return uiMetric;
};

export const getUiMetrics = (metrics: Metric[]): UiMetric[] => {
    if (isEmpty(metrics)) {
        return [];
    }

    const uiMetrics = [];
    for (const metric of metrics) {
        uiMetrics.push(getUiMetric(metric));
    }

    return uiMetrics;
};

export const filterMetrics = (
    uiMetrics: UiMetric[],
    searchWords: string[]
): UiMetric[] => {
    if (isEmpty(uiMetrics)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return uiMetrics;
    }

    const filteredUiMetrics = [];
    for (const uiMetric of uiMetrics) {
        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    uiMetric,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredUiMetrics.push(uiMetric);

                break;
            }
        }
    }

    return filteredUiMetrics;
};
