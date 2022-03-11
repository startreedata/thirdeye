import { Box, Card, CardContent, Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { AnomalyBreakdownComparisonHeatmap } from "../../components/anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    createAlertEvaluation,
    getUiAnomaly,
} from "../../utils/anomalies/anomalies.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getAnomaliesAllPath } from "../../utils/routes/routes.util";
import { AnomaliesViewPageParams } from "./anomalies-view-page.interfaces";

export const AnomaliesViewPage: FunctionComponent = () => {
    const {
        evaluation,
        getEvaluation,
        status: getEvaluationRequestStatus,
    } = useGetEvaluation();
    const { anomaly, getAnomaly } = useGetAnomaly();
    const [uiAnomaly, setUiAnomaly] = useState<UiAnomaly | null>(null);
    const [alertEvaluation, setAlertEvaluation] =
        useState<AlertEvaluation | null>(null);
    const [searchParams] = useSearchParams();
    const { showDialog } = useDialog();
    const { id: anomalyId } = useParams<AnomaliesViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch anomaly
        anomalyId &&
            isValidNumberId(anomalyId) &&
            getAnomaly(toNumber(anomalyId));
    }, [anomalyId]);

    useEffect(() => {
        !!anomaly && setUiAnomaly(getUiAnomaly(anomaly));
    }, [anomaly]);

    useEffect(() => {
        if (!evaluation || !anomaly) {
            return;
        }
        // Only filter for the current anomaly
        const anomalyDetectionResults =
            evaluation.detectionEvaluations.output_AnomalyDetectorResult_0;
        anomalyDetectionResults.anomalies = [anomaly];
        setAlertEvaluation(evaluation);
    }, [evaluation, anomaly]);

    useEffect(() => {
        // Fetched alert or time range changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [anomaly, searchParams]);

    useEffect(() => {
        if (getEvaluationRequestStatus === ActionStatus.Error) {
            notify(
                NotificationTypeV1.Error,
                t("message.error-while-fetching", {
                    entity: t("label.chart-data"),
                })
            );
        }
    }, [getEvaluationRequestStatus]);

    if (anomalyId && !isValidNumberId(anomalyId)) {
        // Invalid id
        notify(
            NotificationTypeV1.Error,
            t("message.invalid-id", {
                entity: t("label.anomaly"),
                id: anomalyId,
            })
        );

        setUiAnomaly(null);
    }

    const fetchAlertEvaluation = (): void => {
        const start = searchParams.get(TimeRangeQueryStringKey.START_TIME);
        const end = searchParams.get(TimeRangeQueryStringKey.END_TIME);

        if (!anomaly || !anomaly.alert || !start || !end) {
            setAlertEvaluation(null);

            return;
        }
        getEvaluation(
            createAlertEvaluation(anomaly.alert.id, Number(start), Number(end))
        );
    };

    const handleAnomalyDelete = (uiAnomaly: UiAnomaly): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiAnomaly.name }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleAnomalyDeleteOk(uiAnomaly),
        });
    };

    const handleAnomalyDeleteOk = (uiAnomaly: UiAnomaly): void => {
        deleteAnomaly(uiAnomaly.id).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", { entity: t("label.anomaly") })
            );

            // Redirect to anomalies all path
            navigate(getAnomaliesAllPath());
        });
    };

    return (
        <PageV1>
            <PageHeader
                showCreateButton
                showTimeRange
                title={uiAnomaly ? uiAnomaly.name : ""}
            />
            <PageContentsGridV1>
                {/* Anomaly */}
                <Grid item xs={12}>
                    <AnomalyCard
                        uiAnomaly={uiAnomaly}
                        onDelete={handleAnomalyDelete}
                    />
                </Grid>

                {/* Alert evaluation time series */}
                <Grid item xs={12}>
                    {getEvaluationRequestStatus === ActionStatus.Error && (
                        <Card variant="outlined">
                            <CardContent>
                                <Box pb={20} pt={20}>
                                    <NoDataIndicator />
                                </Box>
                            </CardContent>
                        </Card>
                    )}
                    {getEvaluationRequestStatus === ActionStatus.Done && (
                        <AlertEvaluationTimeSeriesCard
                            alertEvaluation={alertEvaluation}
                            alertEvaluationTimeSeriesHeight={500}
                            maximizedTitle={uiAnomaly ? uiAnomaly.name : ""}
                            onRefresh={fetchAlertEvaluation}
                        />
                    )}
                </Grid>

                <Grid item xs={12}>
                    <AnomalyBreakdownComparisonHeatmap
                        anomaly={anomaly}
                        anomalyId={toNumber(anomalyId)}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
