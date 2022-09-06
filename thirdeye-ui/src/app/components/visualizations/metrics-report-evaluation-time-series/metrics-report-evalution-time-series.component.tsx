import { Box } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AppLoadingIndicatorV1 } from "../../../platform/components";
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
    const [loading, setLoading] = useState(true);
    const { t } = useTranslation();

    const { evaluation, getEvaluation } = useGetEvaluation();

    const { anomalies, getAnomalies } = useGetAnomalies();

    useEffect(() => {
        fetchAlertEvaluation();
    }, [searchParams]);

    useEffect(() => {
        if (evaluation) {
            if (anomalies) {
                evaluation.detectionEvaluations.output_AnomalyDetectorResult_0.anomalies =
                    anomalies;
            }
            setLoading(false);
            setAlertEvaluation(evaluation);
        }
    }, [evaluation]);

    const fetchAlertEvaluation = (): void => {
        setLoading(true);
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
        <Box height={120} width={500}>
            {loading ? (
                <AppLoadingIndicatorV1 />
            ) : !alertEvaluation ? (
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
