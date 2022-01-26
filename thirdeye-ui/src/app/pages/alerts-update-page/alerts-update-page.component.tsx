import { Grid } from "@material-ui/core";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { assign, isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { AlertWizard } from "../../components/alert-wizard/alert-wizard.component";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import {
    getAlert,
    getAllAlerts,
    updateAlert,
} from "../../rest/alerts/alerts.rest";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createSubscriptionGroup,
    getAllSubscriptionGroups,
    updateSubscriptionGroups,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { createAlertEvaluation } from "../../utils/alerts/alerts.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getAlertsViewPath } from "../../utils/routes/routes.util";
import { AlertsUpdatePageParams } from "./alerts-update-page.interfaces";

export const AlertsUpdatePage: FunctionComponent = () => {
    const { getEvaluation } = useGetEvaluation();
    const [loading, setLoading] = useState(true);
    const [alert, setAlert] = useState<Alert>();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const params = useParams<AlertsUpdatePageParams>();
    const history = useHistory();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: alert ? alert.name : "",
                onClick: (): void => {
                    if (alert) {
                        history.push(getAlertsViewPath(alert.id));
                    }
                },
            },
        ]);
    }, [alert]);

    useEffect(() => {
        fetchAlert();
    }, []);

    const onAlertWizardFinish = (
        newAlert: Alert,
        subscriptionGroups: SubscriptionGroup[],
        omittedSubscriptionGroups: SubscriptionGroup[] = []
    ): void => {
        if (!alert) {
            return;
        }

        newAlert = assign({ ...newAlert }, { id: alert?.id });

        updateAlert(newAlert)
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
                    history.push(getAlertsViewPath(alert.id));

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
                const subscriptionGroupsToBeOmitted = omittedSubscriptionGroups.map(
                    (subscriptionGroup) => ({
                        ...subscriptionGroup,
                        alerts: subscriptionGroup.alerts.filter(
                            (subGroupAlert) => subGroupAlert.id !== alert.id // Remove alert from list
                        ),
                    })
                );

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
                    .catch((): void => {
                        notify(
                            NotificationTypeV1.Error,
                            t("message.update-error", {
                                entity: t("label.subscription-groups"),
                            })
                        );
                    })
                    .finally((): void => {
                        // Redirect to alerts detail path
                        history.push(getAlertsViewPath(alert.id));
                    });
            })
            .catch((): void => {
                notify(
                    NotificationTypeV1.Error,
                    t("message.update-error", { entity: t("label.alert") })
                );
            });
    };

    const onSubscriptionGroupWizardFinish = async (
        subscriptionGroup: SubscriptionGroup
    ): Promise<SubscriptionGroup> => {
        let newSubscriptionGroup: SubscriptionGroup = (null as unknown) as SubscriptionGroup;

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
            notify(
                NotificationTypeV1.Error,
                t("message.create-error", {
                    entity: t("label.subscription-group"),
                })
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
            notify(NotificationTypeV1.Error, t("message.fetch-error"));
        }

        return fetchedSubscriptionGroups;
    };

    const fetchAllAlerts = async (): Promise<Alert[]> => {
        let fetchedAlerts: Alert[] = [];
        try {
            fetchedAlerts = await getAllAlerts();
        } catch (error) {
            notify(NotificationTypeV1.Error, t("message.fetch-error"));
        }

        return fetchedAlerts;
    };

    const fetchAlertEvaluation = async (
        alert: Alert
    ): Promise<AlertEvaluation> => {
        const fetchedAlertEvaluation = await getEvaluation(
            createAlertEvaluation(
                alert,
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            )
        );

        if (fetchedAlertEvaluation === undefined) {
            return {} as AlertEvaluation;
        }

        return fetchedAlertEvaluation;
    };

    const fetchAlert = (): void => {
        // Validate id from URL
        if (!isValidNumberId(params.id)) {
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
            .catch(() => {
                notify(NotificationTypeV1.Error, t("message.fetch-error"));
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
                    <AlertWizard
                        alert={alert}
                        getAlertEvaluation={fetchAlertEvaluation}
                        getAllAlerts={fetchAllAlerts}
                        getAllSubscriptionGroups={fetchAllSubscriptionGroups}
                        onFinish={onAlertWizardFinish}
                        onSubscriptionGroupWizardFinish={
                            onSubscriptionGroupWizardFinish
                        }
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
