import { Grid } from "@material-ui/core";
import { PageContentsGridV1, PageV1 } from "@startree-ui/platform-ui";
import { isEmpty } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { AlertWizard } from "../../components/alert-wizard/alert-wizard.component";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import { createAlert, getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createSubscriptionGroup,
    getAllSubscriptionGroups,
    updateSubscriptionGroups,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { createAlertEvaluation } from "../../utils/alerts/alerts.util";
import { getAlertsViewPath } from "../../utils/routes/routes.util";
import { getSuccessSnackbarOption } from "../../utils/snackbar/snackbar.util";

export const AlertsCreatePage: FunctionComponent = () => {
    const { getEvaluation } = useGetEvaluation();
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { enqueueSnackbar } = useSnackbar();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    const onAlertWizardFinish = (
        alert: Alert,
        subscriptionGroups: SubscriptionGroup[]
    ): void => {
        if (!alert) {
            return;
        }

        createAlert(alert).then((alert: Alert): void => {
            enqueueSnackbar(
                t("message.create-success", { entity: t("label.alert") }),
                getSuccessSnackbarOption()
            );

            if (isEmpty(subscriptionGroups)) {
                // Redirect to alerts detail path
                history.push(getAlertsViewPath(alert.id));

                return;
            }

            // Update subscription groups with new alert
            for (const subscriptionGroup of subscriptionGroups) {
                if (subscriptionGroup.alerts) {
                    // Add to existing list
                    subscriptionGroup.alerts.push(alert);
                } else {
                    // Create and add to list
                    subscriptionGroup.alerts = [alert];
                }
            }

            updateSubscriptionGroups(subscriptionGroups)
                .then((): void => {
                    enqueueSnackbar(
                        t("message.update-success", {
                            entity: t("label.subscription-groups"),
                        }),
                        getSuccessSnackbarOption()
                    );
                })
                .finally((): void => {
                    // Redirect to alerts detail path
                    history.push(getAlertsViewPath(alert.id));
                });
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
            // Empty
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
            // Empty
        }

        return fetchedSubscriptionGroups;
    };

    const fetchAllAlerts = async (): Promise<Alert[]> => {
        let fetchedAlerts: Alert[] = [];
        try {
            fetchedAlerts = await getAllAlerts();
        } catch (error) {
            // Empty
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

    return (
        <PageV1>
            <PageHeader title={t("label.create")} />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <AlertWizard
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
