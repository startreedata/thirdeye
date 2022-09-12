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
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AppLoadingIndicatorV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { generateChartOptionsForMetricsReport } from "../../rca/anomaly-time-series-card/anomaly-time-series-card.utils";
import { TimeSeriesChart } from "../time-series-chart/time-series-chart.component";
import { MetricsReportEvaluationTimeSeriesProps } from "./metrics-report-evaluation-time-series.interface";

const MetricsReportEvaluationTimeSeries: FunctionComponent<
    MetricsReportEvaluationTimeSeriesProps
> = ({ alertEvaluation, fetchAlertEvaluation, evaluationRequestStatus }) => {
    const { t } = useTranslation();
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (!alertEvaluation) {
            fetchAlertEvaluation();
            setIsLoading(true);
        }
    }, [alertEvaluation]);

    useEffect(() => {
        if (
            (alertEvaluation && isLoading) ||
            (isLoading && evaluationRequestStatus !== ActionStatus.Working)
        ) {
            setIsLoading(false);
        }
    }, [alertEvaluation, isLoading, evaluationRequestStatus]);

    const { anomalies = [] } =
        alertEvaluation?.detectionEvaluations.output_AnomalyDetectorResult_0 ||
        {};

    return (
        <Box height={120}>
            {isLoading && <AppLoadingIndicatorV1 />}

            {(evaluationRequestStatus === ActionStatus.Error ||
                !alertEvaluation) && <NoDataIndicator />}

            {alertEvaluation && (
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
