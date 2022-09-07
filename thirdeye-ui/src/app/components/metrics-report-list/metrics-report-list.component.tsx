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
import { uniqBy } from "lodash";
import React, {
    FunctionComponent,
    ReactNode,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { DataGridV1 } from "../../platform/components";
import { linkRendererV1 } from "../../platform/utils";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    getAlertsViewPath,
    getMetricsViewPath,
} from "../../utils/routes/routes.util";
import { AnomalyQuickFilters } from "../anomaly-quick-filters/anomaly-quick-filters.component";
import { MetricReportExpandList } from "../metrics-report-expand-list/metrics-report-expand-list.component";
import MetricsReportEvaluationTimeSeries from "../visualizations/metrics-report-evaluation-time-series/metrics-report-evalution-time-series.component";
import { MetricsReportListProps } from "./metrics-report-list.interfaces";

export const MetricsReportList: FunctionComponent<MetricsReportListProps> = ({
    metricsReport,
    searchParams,
}) => {
    const [metricsReportData, setMetricsReportData] = useState<
        UiAnomaly[] | null
    >(null);
    const { t } = useTranslation();

    const generateDataWithChildren = (data: UiAnomaly[]): UiAnomaly[] => {
        return data.map((metric, index: number) => ({
            ...metric,
            children: [
                {
                    id: index,
                    expandPanelContents: (
                        <MetricReportExpandList
                            id={metric.alertId}
                            searchParams={searchParams}
                        />
                    ),
                },
            ],
        }));
    };

    useEffect(() => {
        if (!metricsReport) {
            return setMetricsReportData(null);
        }

        const metricsReports = generateDataWithChildren(
            uniqBy(metricsReport, "alertId")
        );
        setMetricsReportData(metricsReports);
    }, [metricsReport]);

    const alertNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            return linkRendererV1(cellValue, getAlertsViewPath(data.alertId));
        },
        []
    );

    const metricNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            return data.metricId
                ? linkRendererV1(cellValue, getMetricsViewPath(data.metricId))
                : cellValue;
        },
        []
    );

    const chartRenderer = useCallback(
        (_: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            return (
                <MetricsReportEvaluationTimeSeries
                    data={data}
                    searchParams={searchParams}
                />
            );
        },
        [searchParams]
    );

    const metricsReportColumns = useMemo(
        () => [
            {
                key: "alertName",
                dataKey: "alertName",
                header: t("label.alert"),
                sortable: true,
                minWidth: 300,
                customCellRenderer: alertNameRenderer,
            },
            {
                key: "metricName",
                dataKey: "metricName",
                header: t("label.metric"),
                sortable: true,
                minWidth: 180,
                customCellRenderer: metricNameRenderer,
            },
            {
                key: "datasetName",
                dataKey: "datasetName",
                header: t("label.dataset"),
                sortable: true,
                minWidth: 180,
            },
            {
                key: "chart",
                dataKey: "chart",
                header: t("label.chart"),
                minWidth: 500,
                cellTooltip: false,
                customCellRenderer: chartRenderer,
            },
        ],
        [alertNameRenderer, metricNameRenderer, chartRenderer]
    );

    return (
        <DataGridV1<UiAnomaly>
            disableSearch
            disableSelection
            hideBorder
            columns={metricsReportColumns}
            data={metricsReportData}
            expandColumnKey="alertName"
            rowKey="id"
            toolbarComponent={<AnomalyQuickFilters />}
        />
    );
};
