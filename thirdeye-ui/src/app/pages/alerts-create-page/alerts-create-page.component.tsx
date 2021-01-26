import { isEmpty } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { AlertWizard } from "../../components/alert-wizard/alert-wizard.component";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { createAlert, getAllAlerts } from "../../rest/alerts-rest/alerts-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    createSubscriptionGroup,
    getAllSubscriptionGroups,
    updateSubscriptionGroups,
} from "../../rest/subscription-groups-rest/subscription-groups-rest";
import {
    getAlertsCreatePath,
    getAlertsDetailPath,
} from "../../utils/routes-util/routes-util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar-util/snackbar-util";

export const AlertsCreatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { enqueueSnackbar } = useSnackbar();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.create"),
                onClick: (): void => {
                    history.push(getAlertsCreatePath());
                },
            },
        ]);

        setLoading(false);
    }, []);

    const fetchAllSubscriptionGroups = async (): Promise<
        SubscriptionGroup[]
    > => {
        let fetchedSubscriptionGroups: SubscriptionGroup[] = [];
        await getAllSubscriptionGroups()
            .then((subscriptionGroups: SubscriptionGroup[]): void => {
                fetchedSubscriptionGroups = subscriptionGroups;
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            });

        return fetchedSubscriptionGroups;
    };

    const fetchAllAlerts = async (): Promise<Alert[]> => {
        let alerts: Alert[] = [];
        await getAllAlerts()
            .then((nalerts: Alert[]): void => {
                alerts = nalerts;
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.fetch-error"),
                    getErrorSnackbarOption()
                );
            });

        return alerts;
    };

    const onAlertWizardFinish = (
        alert: Alert,
        subscriptionGroups: SubscriptionGroup[]
    ): void => {
        createAlert(alert)
            .then((alert: Alert): void => {
                enqueueSnackbar(
                    t("message.create-success", {
                        entity: t("label.alert"),
                    }),
                    getSuccessSnackbarOption()
                );

                if (!isEmpty(subscriptionGroups)) {
                    for (const subscriptionGroup of subscriptionGroups) {
                        subscriptionGroup.alerts
                            ? subscriptionGroup.alerts.push(alert)
                            : (subscriptionGroup.alerts = [alert]);
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
                            history.push(getAlertsDetailPath(alert.id));
                        });
                }
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.create-error", {
                        entity: t("label.alert"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const onSubscriptionGroupWizardFinish = async (
        subscriptionGroup: SubscriptionGroup
    ): Promise<SubscriptionGroup> => {
        let createdSubscriptionGroup: SubscriptionGroup = (null as unknown) as SubscriptionGroup;
        await createSubscriptionGroup(subscriptionGroup)
            .then((newSubscriptionGroup: SubscriptionGroup): void => {
                enqueueSnackbar(
                    t("message.create-success", {
                        entity: t("label.subscription-group"),
                    }),
                    getSuccessSnackbarOption()
                );

                createdSubscriptionGroup = newSubscriptionGroup;
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.create-error", {
                        entity: t("label.subscription-group"),
                    }),
                    getErrorSnackbarOption()
                );
            });

        return createdSubscriptionGroup;
    };

    if (loading) {
        return (
            <PageContainer>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents centered hideTimeRange>
                <AlertWizard
                    getAllAlerts={fetchAllAlerts}
                    getAllSubscriptionGroups={fetchAllSubscriptionGroups}
                    onFinish={onAlertWizardFinish}
                    onSubscriptionGroupWizardFinish={
                        onSubscriptionGroupWizardFinish
                    }
                />
            </PageContents>
        </PageContainer>
    );
};
