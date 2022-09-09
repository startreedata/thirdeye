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

import { Box } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { useGetAnomalies } from "../../../rest/anomalies/anomaly.actions";
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { createAlertEvaluation } from "../../../utils/anomalies/anomalies.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { generateChartOptionsForMetricsReport } from "../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../time-series-chart/time-series-chart.component";
import { MetricsReportEvaluationTimeSeriesProps } from "./metrics-report-evaluation-time-series.interface";

const MetricsReportEvaluationTimeSeries: FunctionComponent<
    MetricsReportEvaluationTimeSeriesProps
> = ({ data, searchParams }) => {
    const [alertEvaluation, setAlertEvaluation] =
        useState<AlertEvaluation | null>(null);
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const {
        evaluation,
        getEvaluation,
        errorMessages,
        status: evaluationRequestStatus,
    } = useGetEvaluation();

    const {
        anomalies,
        getAnomalies,
        status: anomaliesRequestStatus,
        errorMessages: anomaliesRequestErrors,
    } = useGetAnomalies();

    useEffect(() => {
        fetchAlertEvaluation();
    }, [searchParams]);

    useEffect(() => {
        if (evaluation) {
            if (anomalies) {
                evaluation.detectionEvaluations.output_AnomalyDetectorResult_0.anomalies =
                    anomalies;
            }
            setAlertEvaluation(evaluation);
        }
    }, [evaluation]);

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

    const fetchAlertEvaluation = (): void => {
        const start = searchParams?.get(TimeRangeQueryStringKey.START_TIME);
        const end = searchParams?.get(TimeRangeQueryStringKey.END_TIME);
        getAnomalies({
            alertId: data.alertId,
            startTime: Number(start),
            endTime: Number(end),
        });
        getEvaluation(
            createAlertEvaluation(data.alertId, Number(start), Number(end))
        );
    };

    return (
        <Box height={120}>
            {evaluationRequestStatus === ActionStatus.Working ? (
                <AppLoadingIndicatorV1 />
            ) : evaluationRequestStatus === ActionStatus.Error ||
              !alertEvaluation ? (
                <NoDataIndicator />
            ) : (
                <TimeSeriesChart
                    height={120}
                    {...generateChartOptionsForMetricsReport(
                        alertEvaluation,
                        anomalies || [],
                        t
                    )}
                />
            )}
        </Box>
    );
};

export default MetricsReportEvaluationTimeSeries;
