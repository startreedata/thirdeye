import { kebabCase } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { AlertWizard } from "../../components/alert-wizard/alert-wizard.component";
import { AlertWizardStep } from "../../components/alert-wizard/alert-wizard.interfaces";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { createAlert } from "../../rest/alerts-rest/alerts-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { getAllSubscriptionGroups } from "../../rest/subscription-groups-rest/subscription-groups-rest";
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
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);
    const {
        setPageBreadcrumbs,
        pushPageBreadcrumb,
        popPageBreadcrumb,
    } = useAppBreadcrumbs();
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
            // Empty page breadcrumb as a placeholder for alert wizard step
            {
                text: "",
            },
        ]);
    }, []);

    useEffect(() => {
        fetchData();
    }, []);

    const fetchData = (): void => {
        getAllSubscriptionGroups()
            .then((subscriptionGroups: SubscriptionGroup[]): void => {
                setSubscriptionGroups(subscriptionGroups);
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

    const onAlertWizardStepChange = (
        alertWizardStep: AlertWizardStep
    ): void => {
        // Update page breadcrumbs
        popPageBreadcrumb();
        pushPageBreadcrumb({
            text: t(`label.${kebabCase(AlertWizardStep[alertWizardStep])}`),
        });
    };

    const onAlertWizardFinish = (alert: Alert): void => {
        createAlert(alert)
            .then((alert: Alert): void => {
                enqueueSnackbar(
                    t("message.create-success", {
                        entity: t("label.alert"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Redirect to alerts detail path
                history.push(getAlertsDetailPath(alert.id));
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
                    subscriptionGroups={subscriptionGroups}
                    onChange={onAlertWizardStepChange}
                    onFinish={onAlertWizardFinish}
                />
            </PageContents>
        </PageContainer>
    );
};
