import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import { assign, isEmpty, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { AlertWizard } from "../../components/alert-wizard/alert-wizard.component";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
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
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { AlertsUpdatePageParams } from "./alerts-update-page.interfaces";

export const AlertsUpdatePage: FunctionComponent = () => {
    const { getEvaluation } = useGetEvaluation();
    const [loading, setLoading] = useState(true);
    const [alert, setAlert] = useState<Alert>();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<AlertsUpdatePageParams>();
    const history = useHistory();
    const { t } = useTranslation();

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
                enqueueSnackbar(
                    t("message.update-success", { entity: t("label.alert") }),
                    getSuccessSnackbarOption()
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
                        enqueueSnackbar(
                            t("message.update-success", {
                                entity: t("label.subscription-groups"),
                            }),
                            getSuccessSnackbarOption()
                        );
                    })
                    .catch((): void => {
                        enqueueSnackbar(
                            t("message.update-error", {
                                entity: t("label.subscription-groups"),
                            }),
                            getErrorSnackbarOption()
                        );
                    })
                    .finally((): void => {
                        // Redirect to alerts detail path
                        history.push(getAlertsViewPath(alert.id));
                    });
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", { entity: t("label.alert") }),
                    getErrorSnackbarOption()
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
            enqueueSnackbar(
                t("message.create-success", {
                    entity: t("label.subscription-group"),
                }),
                getSuccessSnackbarOption()
            );
        } catch (error) {
            enqueueSnackbar(
                t("message.create-error", {
                    entity: t("label.subscription-group"),
                }),
                getErrorSnackbarOption()
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
            enqueueSnackbar(t("message.fetch-error"), getErrorSnackbarOption());
        }

        return fetchedSubscriptionGroups;
    };

    const fetchAllAlerts = async (): Promise<Alert[]> => {
        let fetchedAlerts: Alert[] = [];
        try {
            fetchedAlerts = await getAllAlerts();
        } catch (error) {
            enqueueSnackbar(t("message.fetch-error"), getErrorSnackbarOption());
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
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.alert"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );
            setLoading(false);

            return;
        }

        getAlert(toNumber(params.id))
            .then((alert) => {
                setAlert(alert);
            })
            .catch(() => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
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
        <PageContents centered title={t("label.update")}>
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
        </PageContents>
    );
};
