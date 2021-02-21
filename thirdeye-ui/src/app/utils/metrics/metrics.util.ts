import i18n from "i18next";
import { isEmpty } from "lodash";
import { MetricCardData } from "../../components/entity-cards/metric-card/metric-card.interfaces";
import {
    LogicalView,
    Metric,
    MetricAggFunction,
} from "../../rest/dto/metric.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const createEmptyMetricCardData = (): MetricCardData => {
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
    };
};

export const createEmptyMetricLogicalView = (): LogicalView => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        name: noDataMarker,
        query: noDataMarker,
    };
};

export const getMetricCardData = (metric: Metric): MetricCardData => {
    const metricCardData = createEmptyMetricCardData();

    if (!metric) {
        return metricCardData;
    }

    const noDataMarker = i18n.t("label.no-data-marker");

    // Basic properties
    metricCardData.id = metric.id;
    metricCardData.name = metric.name || noDataMarker;
    metricCardData.active = Boolean(metric.active);
    metricCardData.activeText = metric.active
        ? i18n.t("label.active")
        : i18n.t("label.inactive");
    metricCardData.aggregationColumn = metric.aggregationColumn || noDataMarker;
    metricCardData.aggregationFunction =
        metric.aggregationFunction || noDataMarker;

    // Dataset properties
    if (metric.dataset) {
        metricCardData.datasetId = metric.dataset.id;
        metricCardData.datasetName = metric.dataset.name || noDataMarker;
    }

    // Logical view properties
    if (!metric.views) {
        return metricCardData;
    }

    for (const view of metric.views) {
        const metricLocicalView = createEmptyMetricLogicalView();
        metricLocicalView.name = view.name || noDataMarker;
        metricLocicalView.query = view.query || noDataMarker;

        metricCardData.views.push(metricLocicalView);
    }

    return metricCardData;
};

export const getMetricCardDatas = (metrics: Metric[]): MetricCardData[] => {
    if (isEmpty(metrics)) {
        return [];
    }

    const metricCardDatas = [];
    for (const metric of metrics) {
        metricCardDatas.push(getMetricCardData(metric));
    }

    return metricCardDatas;
};

export const filterMetrics = (
    metricCardDatas: MetricCardData[],
    searchWords: string[]
): MetricCardData[] => {
    if (isEmpty(metricCardDatas)) {
        return [];
    }

    if (isEmpty(searchWords)) {
        return metricCardDatas;
    }

    const filteredMetricCardDatas = [];
    for (const metric of metricCardDatas) {
        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    metric,
                    // Check if string property value contains current search word
                    (value) =>
                        Boolean(value) &&
                        value.toLowerCase().indexOf(searchWord.toLowerCase()) >
                            -1
                )
            ) {
                filteredMetricCardDatas.push(metric);

                break;
            }
        }
    }

    return filteredMetricCardDatas;
};
