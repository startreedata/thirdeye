import { Box, Card, CardContent, CardHeader, Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
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
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
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
import { PROMISES } from "../../utils/constants/constants.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    getAlertsAllPath,
    getAnomaliesAnomalyPath,
} from "../../utils/routes/routes.util";
import { AlertsViewPageParams } from "./alerts-view-page.interfaces";

export const AlertsViewPage: FunctionComponent = () => {
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
    const [uiAlert, setUiAlert] = useState<UiAlert | null>(null);
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const [alertEvaluation, setAlertEvaluation] =
        useState<AlertEvaluation | null>(null);
    const [searchParams] = useSearchParams();
    const { showDialog } = useDialogProviderV1();
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
                // Determine if any of the calls failed
                if (
                    subscriptionGroupsResponse.status === PROMISES.REJECTED ||
                    alertResponse.status === PROMISES.REJECTED
                ) {
                    const axiosError =
                        alertResponse.status === PROMISES.REJECTED
                            ? alertResponse.reason
                            : subscriptionGroupsResponse.status ===
                              PROMISES.REJECTED
                            ? subscriptionGroupsResponse.reason
                            : ({} as AxiosError);
                    const errMessages = getErrorMessages(axiosError);
                    isEmpty(errMessages)
                        ? notify(
                              NotificationTypeV1.Error,
                              t("message.error-while-fetching", {
                                  entity: t(
                                      alertResponse.status === PROMISES.REJECTED
                                          ? "label.alert"
                                          : "label.subscription-groups"
                                  ),
                              })
                          )
                        : errMessages.map((err) =>
                              notify(NotificationTypeV1.Error, err)
                          );
                }

                // Attempt to gather data
                if (subscriptionGroupsResponse.status === PROMISES.FULFILLED) {
                    fetchedSubscriptionGroups =
                        subscriptionGroupsResponse.value;
                }
                if (alertResponse.status === PROMISES.FULFILLED) {
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
            contents: t("message.delete-confirmation", {
                name: uiAlert.name,
            }),
            okButtonText: t("label.delete"),
            cancelButtonText: t("label.cancel"),
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

    return !uiAlert || evaluationRequestStatus === ActionStatus.Working ? (
        <AppLoadingIndicatorV1 />
    ) : (
        <PageV1>
            <PageHeader showCreateButton title={uiAlert.name} />

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
