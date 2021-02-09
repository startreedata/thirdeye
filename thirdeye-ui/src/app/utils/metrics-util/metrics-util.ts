import i18n from "i18next";
import { cloneDeep, isEmpty } from "lodash";
import { MetricsListData } from "../../components/metrics-list/metrics-list.interfaces";
import { Metric } from "../../rest/dto/metric.interfaces";
import { deepSearchStringProperty } from "../search/search.util";

export const createEmptyMetricData = (): MetricsListData => {
    const noDataMarker = i18n.t("label.no-data-marker");

    return {
        id: -1,
        idText: "-1",
        name: noDataMarker,
        datasetName: noDataMarker,
        active: false,
        activeText: "false",
        aggregationFunction: noDataMarker,
        rollupThreshold: -1,
        rollupThresholdText: "-1",
    };
};

export const getMetricTableDataInternal = (metric: Metric): MetricsListData => {
    const individualMetric: MetricsListData = createEmptyMetricData();
    individualMetric.id = metric.id;
    individualMetric.idText = metric.id.toString();
    individualMetric.name = metric.name;
    individualMetric.datasetName = metric.dataset.name;
    individualMetric.active = metric.active;
    individualMetric.activeText = metric.active.toString();
    individualMetric.aggregationFunction = metric.aggregationFunction;
    individualMetric.rollupThresholdText = metric.rollupThreshold.toString();
    individualMetric.rollupThreshold = metric.rollupThreshold;

    return individualMetric;
};

export const getMetricTableDatas = (metrics: Metric[]): MetricsListData[] => {
    const metricTableDatas: MetricsListData[] = [];

    if (isEmpty(metrics)) {
        return metricTableDatas;
    }

    for (const m of metrics) {
        metricTableDatas.push(getMetricTableDataInternal(m));
    }

    return metricTableDatas;
};

export const filterMetrics = (
    alertCardDatas: MetricsListData[],
    searchWords: string[]
): MetricsListData[] => {
    const filteredMetricsTableDatas: MetricsListData[] = [];

    if (isEmpty(alertCardDatas)) {
        // No alerts available, return empty result
        return filteredMetricsTableDatas;
    }

    if (isEmpty(searchWords)) {
        // No search words available, return original alerts
        return alertCardDatas;
    }

    for (const metric of alertCardDatas) {
        // Create a copy without original alert
        const metricsTableDataCopy = cloneDeep(metric);

        for (const searchWord of searchWords) {
            if (
                deepSearchStringProperty(
                    metricsTableDataCopy,
                    (value: string): boolean => {
                        // Check if string property value contains current search word
                        return (
                            Boolean(value) &&
                            value
                                .toLowerCase()
                                .indexOf(searchWord.toLowerCase()) > -1
                        );
                    }
                )
            ) {
                filteredMetricsTableDatas.push(metric);

                break;
            }
        }
    }

    return filteredMetricsTableDatas;
};
