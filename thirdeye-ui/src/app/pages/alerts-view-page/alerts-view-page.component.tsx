import { Box, Card, CardContent, CardHeader, Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AlertCard } from "../../components/entity-cards/alert-card/alert-card.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import {
    AppLoadingIndicatorV1,
    JSONEditorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import {
    deleteAlert,
    getAlert,
    updateAlert,
} from "../../rest/alerts/alerts.rest";
import { useGetAnomalies } from "../../rest/anomalies/anomaly.actions";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiAlert } from "../../rest/dto/ui-alert.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-groups/subscription-groups.rest";
import {
    createAlertEvaluation,
    getUiAlert,
} from "../../utils/alerts/alerts.util";
import { isValidNumberId } from "../../utils/params/params.util";
import {
    getAlertsAllPath,
    getAnomaliesAnomalyPath,
} from "../../utils/routes/routes.util";
import { AlertsViewPageParams } from "./alerts-view-page.interfaces";

export const AlertsViewPage: FunctionComponent = () => {
    const {
        evaluation,
        getEvaluation,
        status: evaluationRequestStatus,
    } = useGetEvaluation();
    const { anomalies, getAnomalies } = useGetAnomalies();
    const [uiAlert, setUiAlert] = useState<UiAlert | null>(null);
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const [alertEvaluation, setAlertEvaluation] =
        useState<AlertEvaluation | null>(null);
    const [searchParams] = useSearchParams();
    const { showDialog } = useDialog();
    const { id: alertId } = useParams<AlertsViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        fetchAlert();
    }, [alertId]);

    useEffect(() => {
        if (evaluation) {
            if (anomalies) {
                evaluation.detectionEvaluations.output_AnomalyDetectorResult_0.anomalies =
                    anomalies;
            }
            setAlertEvaluation(evaluation);
        }
    }, [evaluation, anomalies]);

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [uiAlert, searchParams]);

    useEffect(() => {
        if (evaluationRequestStatus === ActionStatus.Error) {
            notify(
                NotificationTypeV1.Error,
                t("message.error-while-fetching", {
                    entity: t("label.anomalies"),
                })
            );
        }
    }, [evaluationRequestStatus]);

    const fetchAlertEvaluation = (): void => {
        const start = searchParams.get(TimeRangeQueryStringKey.START_TIME);
        const end = searchParams.get(TimeRangeQueryStringKey.END_TIME);

        if (!uiAlert || !uiAlert.alert || !start || !end) {
            setAlertEvaluation(null);

            return;
        }
        getAnomalies({
            alertId: uiAlert.alert.id,
            startTime: Number(start),
            endTime: Number(end),
        });
        getEvaluation(
            createAlertEvaluation(uiAlert.alert, Number(start), Number(end))
        );
    };

    const fetchAlert = (): void => {
        setUiAlert(null);
        let fetchedUiAlert = {} as UiAlert;
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];

        if (alertId && !isValidNumberId(alertId)) {
            // Invalid id
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: alertId,
                })
            );

            setUiAlert(fetchedUiAlert);
            setSubscriptionGroups(fetchedSubscriptionGroups);

            return;
        }

        Promise.allSettled([
            getAlert(toNumber(alertId)),
            getAllSubscriptionGroups(),
        ])
            .then(([alertResponse, subscriptionGroupsResponse]) => {
                // Attempt to gather data
                if (subscriptionGroupsResponse.status === "fulfilled") {
                    fetchedSubscriptionGroups =
                        subscriptionGroupsResponse.value;
                }
                if (alertResponse.status === "fulfilled") {
                    fetchedUiAlert = getUiAlert(
                        alertResponse.value,
                        fetchedSubscriptionGroups
                    );
                }
            })
            .finally(() => {
                setUiAlert(fetchedUiAlert);
                setSubscriptionGroups(fetchedSubscriptionGroups);
            });
    };

    const handleAlertChange = (uiAlert: UiAlert): void => {
        if (!uiAlert.alert) {
            return;
        }

        updateAlert(uiAlert.alert).then((alert) => {
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", { entity: t("label.alert") })
            );

            // Replace updated alert as fetched alert
            setUiAlert(getUiAlert(alert, subscriptionGroups));
        });
    };

    const handleAlertDelete = (): void => {
        if (!uiAlert) {
            return;
        }
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiAlert.name }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleAlertDeleteOk(uiAlert),
        });
    };

    const handleAlertDeleteOk = (uiAlert: UiAlert): void => {
        deleteAlert(uiAlert.id).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", { entity: t("label.alert") })
            );

            // Redirect to alerts all path
            navigate(getAlertsAllPath());
        });
    };

    const onAnomalyBarClick = (anomaly: Anomaly): void => {
        navigate(getAnomaliesAnomalyPath(anomaly.id));
    };

    return !uiAlert || evaluationRequestStatus === ActionStatus.Working ? (
        <AppLoadingIndicatorV1 />
    ) : (
        <PageV1>
            <PageHeader showCreateButton showTimeRange title={uiAlert.name} />

            <PageContentsGridV1>
                {/* Alert evaluation time series */}
                <Grid item xs={12}>
                    {evaluationRequestStatus === ActionStatus.Error && (
                        <Card variant="outlined">
                            <CardContent>
                                <Box pb={20} pt={20}>
                                    <NoDataIndicator />
                                </Box>
                            </CardContent>
                        </Card>
                    )}

                    {evaluationRequestStatus === ActionStatus.Done && (
                        <AlertEvaluationTimeSeriesCard
                            alertEvaluation={alertEvaluation}
                            alertEvaluationTimeSeriesHeight={500}
                            title={uiAlert.name}
                            onAnomalyBarClick={onAnomalyBarClick}
                            onRefresh={fetchAlertEvaluation}
                        />
                    )}
                </Grid>

                {/* Alert Details Card*/}
                <Grid item xs={12}>
                    <AlertCard
                        anomalies={anomalies}
                        uiAlert={uiAlert}
                        onChange={handleAlertChange}
                        onDelete={handleAlertDelete}
                    />
                </Grid>

                {/* Readonly detection configuration */}
                <Grid item sm={12}>
                    <Card variant="outlined">
                        <CardHeader
                            title={t("label.detection-configuration")}
                            titleTypographyProps={{ variant: "h6" }}
                        />
                        <CardContent>
                            <JSONEditorV1
                                disableValidation
                                readOnly
                                value={
                                    uiAlert.alert as unknown as Record<
                                        string,
                                        unknown
                                    >
                                }
                            />
                        </CardContent>
                    </Card>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
