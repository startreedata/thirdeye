import { Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { AnomalyCardData } from "../../components/entity-cards/anomaly-card/anomaly-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { getAlertEvaluation } from "../../rest/alerts-rest/alerts-rest";
import {
    deleteAnomaly,
    getAnomaly,
} from "../../rest/anomalies-rest/anomalies-rest";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    createAlertEvaluation,
    getAnomalyCardData,
} from "../../utils/anomalies-util/anomalies-util";
import { isValidNumberId } from "../../utils/params-util/params-util";
import { getAnomaliesAllPath } from "../../utils/routes-util/routes-util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar-util/snackbar-util";
import { AnomaliesDetailPageParams } from "./anomalies-detail-page.interfaces";

export const AnomaliesDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [anomalyCardData, setAnomalyCardData] = useState<AnomalyCardData>();
    const [
        alertEvaluation,
        setAlertEvaluation,
    ] = useState<AlertEvaluation | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<AnomaliesDetailPageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchAnomaly();
    }, []);

    useEffect(() => {
        // Fetched anomaly or time range changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [anomalyCardData, timeRangeDuration]);

    const onDeleteAnomaly = (anomalyCardData: AnomalyCardData): void => {
        if (!anomalyCardData) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: anomalyCardData.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteAnomalyConfirmation(anomalyCardData);
            },
        });
    };

    const onDeleteAnomalyConfirmation = (
        anomalyCardData: AnomalyCardData
    ): void => {
        if (!anomalyCardData) {
            return;
        }

        deleteAnomaly(anomalyCardData.id)
            .then((): void => {
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.anomaly"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Redirect to anomalies all path
                history.push(getAnomaliesAllPath());
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", {
                        entity: t("label.anomaly"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchAnomaly = (): void => {
        // Validate id from URL
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.anomaly"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );
            setLoading(false);

            return;
        }

        getAnomaly(toNumber(params.id))
            .then((anomaly: Anomaly): void => {
                setAnomalyCardData(getAnomalyCardData(anomaly));
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    const fetchAlertEvaluation = (): void => {
        setAlertEvaluation(null);
        let fetchedAlertEvaluation = {} as AlertEvaluation;

        if (!anomalyCardData) {
            setAlertEvaluation(fetchedAlertEvaluation);

            return;
        }

        getAlertEvaluation(
            createAlertEvaluation(
                anomalyCardData.alertId,
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            )
        )
            .then((alertEvaluation: AlertEvaluation): void => {
                fetchedAlertEvaluation = alertEvaluation;
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            })
            .finally((): void => {
                setAlertEvaluation(fetchedAlertEvaluation);
            });
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents
            centered
            title={anomalyCardData ? anomalyCardData.name : ""}
        >
            {anomalyCardData && (
                <Grid container>
                    {/* Anomaly */}
                    <Grid item sm={12}>
                        <AnomalyCard
                            hideViewDetailsLinks
                            anomalyCardData={anomalyCardData}
                            onDelete={onDeleteAnomaly}
                        />
                    </Grid>

                    {/* Alert evaluation time series */}
                    <Grid item sm={12}>
                        <AlertEvaluationTimeSeriesCard
                            showMaximizeButton
                            alertEvaluation={alertEvaluation}
                            maximizedTitle={anomalyCardData.name}
                            visualizationHeight={500}
                        />
                    </Grid>
                </Grid>
            )}

            {/* No data available message */}
            {!anomalyCardData && <NoDataIndicator />}
        </PageContents>
    );
};
