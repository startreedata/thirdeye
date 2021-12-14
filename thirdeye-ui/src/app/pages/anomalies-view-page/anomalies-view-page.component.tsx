import { Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
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
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
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
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<AnomaliesViewPageParams>();
    const history = useHistory();
    const { t } = useTranslation();

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
        !!evaluation && setAlertEvaluation(evaluation);
    }, [evaluation]);

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [anomaly]);

    if (!isValidNumberId(params.id)) {
        // Invalid id
        enqueueSnackbar(
            t("message.invalid-id", {
                entity: t("label.anomaly"),
                id: params.id,
            }),
            getErrorSnackbarOption()
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
            enqueueSnackbar(
                t("message.delete-success", { entity: t("label.anomaly") }),
                getSuccessSnackbarOption()
            );

            // Redirect to anomalies all path
            history.push(getAnomaliesAllPath());
        });
    };

    return (
        <PageContents centered title={uiAnomaly ? uiAnomaly.name : ""}>
            <Grid container>
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
            </Grid>
        </PageContents>
    );
};
