import { Grid } from "@material-ui/core";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { AnomalyBreakdownComparisonHeatmap } from "../../components/anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.component";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    createAlertEvaluation,
    filterAnomaliesByTime,
    getUiAnomaly,
} from "../../utils/anomalies/anomalies.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getAnomaliesAllPath } from "../../utils/routes/routes.util";
import { AnomaliesViewPageParams } from "./anomalies-view-page.interfaces";

export const AnomaliesViewPage: FunctionComponent = () => {
    const { evaluation, getEvaluation } = useGetEvaluation();
    const { anomaly, getAnomaly } = useGetAnomaly();
    const [uiAnomaly, setUiAnomaly] = useState<UiAnomaly | null>(null);
    const [
        alertEvaluation,
        setAlertEvaluation,
    ] = useState<AlertEvaluation | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const params = useParams<AnomaliesViewPageParams>();
    const history = useHistory();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch anomaly
        isValidNumberId(params.id) && getAnomaly(toNumber(params.id));
    }, [timeRangeDuration]);

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
        anomalyDetectionResults.anomalies = filterAnomaliesByTime(
            anomalyDetectionResults.anomalies,
            anomaly.startTime,
            anomaly.endTime
        );
        setAlertEvaluation(evaluation);
    }, [evaluation, anomaly]);

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [anomaly]);

    if (!isValidNumberId(params.id)) {
        // Invalid id
        notify(
            NotificationTypeV1.Error,
            t("message.invalid-id", {
                entity: t("label.anomaly"),
                id: params.id,
            })
        );

        setUiAnomaly(null);
    }

    const fetchAlertEvaluation = (): void => {
        if (!anomaly || !anomaly.alert) {
            setAlertEvaluation(null);

            return;
        }
        getEvaluation(
            createAlertEvaluation(
                anomaly.alert.id,
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            )
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
            history.push(getAnomaliesAllPath());
        });
    };

    return (
        <PageV1>
            <PageHeader showTimeRange title={uiAnomaly ? uiAnomaly.name : ""} />
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
                    <AlertEvaluationTimeSeriesCard
                        alertEvaluation={alertEvaluation}
                        alertEvaluationTimeSeriesHeight={500}
                        maximizedTitle={uiAnomaly ? uiAnomaly.name : ""}
                        onRefresh={fetchAlertEvaluation}
                    />
                </Grid>

                <Grid item xs={12}>
                    <AnomalyBreakdownComparisonHeatmap
                        anomaly={anomaly}
                        anomalyId={toNumber(params.id)}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
