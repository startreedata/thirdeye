import { Grid } from "@material-ui/core";
import { AxiosError } from "axios";
import { assign, isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { AlertWizard } from "../../components/alert-wizard/alert-wizard.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import {
    getAlert,
    getAllAlerts,
    updateAlert,
} from "../../rest/alerts/alerts.rest";
import {
    Alert,
    AlertEvaluation,
    EditableAlert,
} from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createSubscriptionGroup,
    getAllSubscriptionGroups,
    updateSubscriptionGroups,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { createAlertEvaluation } from "../../utils/alerts/alerts.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import {
    generateDateRangeMonthsFromNow,
    getAlertsViewPath,
} from "../../utils/routes/routes.util";
import { AlertsUpdatePageParams } from "./alerts-update-page.interfaces";

export const AlertsUpdatePage: FunctionComponent = () => {
    const {
        getEvaluation,
        errorMessages,
        status: getEvaluationStatus,
    } = useGetEvaluation();
    const [loading, setLoading] = useState(true);
    const [alert, setAlert] = useState<Alert>();
    const params = useParams<AlertsUpdatePageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );

    useEffect(() => {
        // Default the time range to 6 months ago
        const [defaultStart, defaultEnd] = generateDateRangeMonthsFromNow(6);
        searchParams.set(
            TimeRangeQueryStringKey.START_TIME,
            defaultStart.toString()
        );
        searchParams.set(
            TimeRangeQueryStringKey.END_TIME,
            defaultEnd.toString()
        );
        setSearchParams(searchParams, { replace: true });
    }, []);

    useEffect(() => {
        fetchAlert();
    }, []);

    const onAlertWizardFinish = (
        newAlert: EditableAlert,
        subscriptionGroups: SubscriptionGroup[],
        omittedSubscriptionGroups: SubscriptionGroup[] = []
    ): void => {
        if (!alert) {
            return;
        }

        newAlert = assign({ ...newAlert }, { id: alert?.id });

        updateAlert(newAlert as Alert)
            .then((alert: Alert): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", { entity: t("label.alert") })
                );

                if (
                    isEmpty(subscriptionGroups) &&
                    isEmpty(omittedSubscriptionGroups)
                ) {
                    // Redirect to alerts detail path
                    navigate(getAlertsViewPath(alert.id));

                    return;
                }

                // Add alert to subscription groups
                const subscriptionGroupsToBeAdded = subscriptionGroups.map(
                    (subscriptionGroup) => ({
                        ...subscriptionGroup,
                        alerts: subscriptionGroup.alerts
                            ? [...subscriptionGroup.alerts, alert] // Add to existing list
                            : [alert], // Create new list
                    })
                );

                // Remove alert from subscription groups
                const subscriptionGroupsToBeOmitted =
                    omittedSubscriptionGroups.map((subscriptionGroup) => ({
                        ...subscriptionGroup,
                        alerts: subscriptionGroup.alerts.filter(
                            (subGroupAlert) => subGroupAlert.id !== alert.id // Remove alert from list
                        ),
                    }));

                const subscriptionGroupsToBeUpdated = [
                    ...subscriptionGroupsToBeAdded,
                    ...subscriptionGroupsToBeOmitted,
                ];

                updateSubscriptionGroups(subscriptionGroupsToBeUpdated)
                    .then((): void => {
                        notify(
                            NotificationTypeV1.Success,
                            t("message.update-success", {
                                entity: t("label.subscription-groups"),
                            })
                        );
                    })
                    .catch((error: AxiosError): void => {
                        const errMessages = getErrorMessages(error);

                        isEmpty(errMessages)
                            ? notify(
                                  NotificationTypeV1.Error,
                                  t("message.update-error", {
                                      entity: t("label.subscription-groups"),
                                  })
                              )
                            : errMessages.map((err) =>
                                  notify(NotificationTypeV1.Error, err)
                              );
                    })
                    .finally((): void => {
                        // Redirect to alerts detail path
                        navigate(getAlertsViewPath(alert.id));
                    });
            })
            .catch((error: AxiosError): void => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.update-error", {
                              entity: t("label.alert"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            });
    };

    const onSubscriptionGroupWizardFinish = async (
        subscriptionGroup: SubscriptionGroup
    ): Promise<SubscriptionGroup> => {
        let newSubscriptionGroup: SubscriptionGroup =
            null as unknown as SubscriptionGroup;

        if (!subscriptionGroup) {
            return newSubscriptionGroup;
        }

        try {
            newSubscriptionGroup = await createSubscriptionGroup(
                subscriptionGroup
            );

            notify(
                NotificationTypeV1.Success,
                t("message.create-success", {
                    entity: t("label.subscription-group"),
                })
            );
        } catch (error) {
            const errMessages = getErrorMessages(error as AxiosError);

            isEmpty(errMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.create-error", {
                          entity: t("label.subscription-group"),
                      })
                  )
                : errMessages.map((err) =>
                      notify(NotificationTypeV1.Error, err)
                  );
        }

        return newSubscriptionGroup;
    };

    const fetchAllSubscriptionGroups = async (): Promise<
        SubscriptionGroup[]
    > => {
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];
        try {
            fetchedSubscriptionGroups = await getAllSubscriptionGroups();
        } catch (error) {
            const errMessages = getErrorMessages(error as AxiosError);

            isEmpty(errMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.subscription-groups"),
                      })
                  )
                : errMessages.map((err) =>
                      notify(NotificationTypeV1.Error, err)
                  );
        }

        return fetchedSubscriptionGroups;
    };

    const fetchAllAlerts = async (): Promise<Alert[]> => {
        let fetchedAlerts: Alert[] = [];
        try {
            fetchedAlerts = await getAllAlerts();
        } catch (error) {
            const errMessages = getErrorMessages(error as AxiosError);

            isEmpty(errMessages)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.alerts"),
                      })
                  )
                : errMessages.map((err) =>
                      notify(NotificationTypeV1.Error, err)
                  );
        }

        return fetchedAlerts;
    };

    const fetchAlertEvaluation = async (
        alert: Alert
    ): Promise<AlertEvaluation> => {
        const fetchedAlertEvaluation = await getEvaluation(
            createAlertEvaluation(alert, startTime, endTime)
        );

        if (fetchedAlertEvaluation === undefined) {
            return {} as AlertEvaluation;
        }

        return fetchedAlertEvaluation;
    };

    useEffect(() => {
        if (getEvaluationStatus === ActionStatus.Error) {
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
    }, [errorMessages, getEvaluationStatus]);

    const fetchAlert = (): void => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: params.id,
                })
            );
            setLoading(false);

            return;
        }

        getAlert(toNumber(params.id))
            .then((alert) => {
                setAlert(alert);
            })
            .catch((error: AxiosError) => {
                const errMessages = getErrorMessages(error);

                isEmpty(errMessages)
                    ? notify(
                          NotificationTypeV1.Error,
                          t("message.error-while-fetching", {
                              entity: t("label.alert"),
                          })
                      )
                    : errMessages.map((err) =>
                          notify(NotificationTypeV1.Error, err)
                      );
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <PageV1>
            <PageHeader
                title={t("label.update-entity", {
                    entity: t("label.alert"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {alert && (
                        <AlertWizard<Alert>
                            alert={alert}
                            getAlertEvaluation={fetchAlertEvaluation}
                            getAllAlerts={fetchAllAlerts}
                            getAllSubscriptionGroups={
                                fetchAllSubscriptionGroups
                            }
                            onFinish={onAlertWizardFinish}
                            onSubscriptionGroupWizardFinish={
                                onSubscriptionGroupWizardFinish
                            }
                        />
                    )}
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
