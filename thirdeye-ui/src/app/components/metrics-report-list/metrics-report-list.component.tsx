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
import { isEmpty, uniqBy } from "lodash";
import React, {
    FunctionComponent,
    ReactNode,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { linkRendererV1 } from "../../platform/utils";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import { useGetAnomalies } from "../../rest/anomalies/anomaly.actions";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { createAlertEvaluation } from "../../utils/anomalies/anomalies.util";
import {
    getAlertsViewPath,
    getMetricsViewPath,
} from "../../utils/routes/routes.util";
import { AnomalyQuickFilters } from "../anomaly-quick-filters/anomaly-quick-filters.component";
import { MetricReportExpandList } from "../metrics-report-expand-list/metrics-report-expand-list.component";
import { TimeRangeQueryStringKey } from "../time-range/time-range-provider/time-range-provider.interfaces";
import MetricsReportEvaluationTimeSeries from "../visualizations/metrics-report-evaluation-time-series/metrics-report-evalution-time-series.component";
import { MetricsReportListProps } from "./metrics-report-list.interfaces";
import { useMetricsReportListStyles } from "./metrics-report-list.styles";

export const MetricsReportList: FunctionComponent<MetricsReportListProps> = ({
    metricsReport,
    searchParams,
}) => {
    const [metricsReportData, setMetricsReportData] = useState<
        UiAnomaly[] | null
    >(null);

    const [alertEvaluationData, setAlertEvaluationData] = useState<
        Map<number, AlertEvaluation>
    >(new Map());

    const {
        status: evaluationRequestStatus,
        errorMessages,
        getEvaluation,
    } = useGetEvaluation();

    const {
        status: anomaliesRequestStatus,
        errorMessages: anomaliesRequestErrors,
    } = useGetAnomalies();

    const { getAnomalies } = useGetAnomalies();

    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();

    const classes = useMetricsReportListStyles();

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

    useEffect(() => {
        if (evaluationRequestStatus === ActionStatus.Error) {
            !isEmpty(errorMessages)
                ? errorMessages.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.chart-data"),
                      })
                  );
        }
    }, [errorMessages, evaluationRequestStatus]);

    useEffect(() => {
        if (anomaliesRequestStatus === ActionStatus.Error) {
            !isEmpty(anomaliesRequestErrors)
                ? anomaliesRequestErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.anomalies"),
                      })
                  );
        }
    }, [anomaliesRequestStatus, anomaliesRequestErrors]);

    const alertNameRenderer = useCallback(
        (cellValue: Record<string, unknown>, data: UiAnomaly): ReactNode => {
            return linkRendererV1(cellValue, getAlertsViewPath(data.alertId));
        },
        []
    );

    const fetchAlertEvaluation = useCallback(
        (data: UiAnomaly): void => {
            const start = searchParams?.get(TimeRangeQueryStringKey.START_TIME);
            const end = searchParams?.get(TimeRangeQueryStringKey.END_TIME);

            let anomalyData: Anomaly[] | undefined;

            getAnomalies({
                alertId: data.alertId,
                startTime: Number(start),
                endTime: Number(end),
            }).then((data) => {
                anomalyData = data;
            });

            getEvaluation(
                createAlertEvaluation(data.alertId, Number(start), Number(end))
            ).then((evaluationData) => {
                if (evaluationData && anomalyData?.length) {
                    evaluationData.detectionEvaluations.output_AnomalyDetectorResult_0.anomalies =
                        anomalyData;
                    if (!alertEvaluationData.has(data.alertId)) {
                        setAlertEvaluationData((prevData) => {
                            return new Map([
                                ...prevData,
                                [data.alertId, evaluationData],
                            ]);
                        });
                    }
                }
            });
        },

        [alertEvaluationData, searchParams]
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
            const { alertId } = data;
            const alertEvaluation = alertEvaluationData.get(alertId);

            return (
                <MetricsReportEvaluationTimeSeries
                    alertEvaluation={alertEvaluation}
                    evaluationRequestStatus={evaluationRequestStatus}
                    fetchAlertEvaluation={() => fetchAlertEvaluation(data)}
                />
            );
        },

        [alertEvaluationData, fetchAlertEvaluation, evaluationRequestStatus]
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
                flex: 1,
                minWidth: 0,
                cellClasses: classes.fullWidthCell,
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
