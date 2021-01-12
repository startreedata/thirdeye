import { kebabCase } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SubscriptionGroupWizard } from "../../components/subscription-group-wizard/subscription-group-wizard.component";
import { SubscriptionGroupWizardStep } from "../../components/subscription-group-wizard/subscription-group-wizard.interfaces";
import { getAllAlerts } from "../../rest/alerts-rest/alerts-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { createSubscriptionGroup } from "../../rest/subscription-groups-rest/subscription-groups-rest";
import {
    getSubscriptionGroupsCreatePath,
    getSubscriptionGroupsDetailPath,
} from "../../utils/routes-util/routes-util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar-util/snackbar-util";

export const SubscriptionGroupsCreatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [alerts, setAlerts] = useState<Alert[]>([]);
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
                    history.push(getSubscriptionGroupsCreatePath());
                },
            },
            // Empty page breadcrumb as a placeholder for subscription group wizard step
            {
                text: "",
            },
        ]);
    }, []);

    useEffect(() => {
        // Fetch data
        fetchData();
    }, []);

    const fetchData = (): void => {
        getAllAlerts()
            .then((alerts: Alert[]): void => {
                setAlerts(alerts);
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

    const onSubscriptionGroupWizardStepChange = (
        subscriptionGroupWizardStep: SubscriptionGroupWizardStep
    ): void => {
        // Update page breadcrumbs
        popPageBreadcrumb();
        pushPageBreadcrumb({
            text: t(
                `label.${kebabCase(
                    SubscriptionGroupWizardStep[subscriptionGroupWizardStep]
                )}`
            ),
        });
    };

    const onSubscriptionGroupWizardFinish = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        createSubscriptionGroup(subscriptionGroup)
            .then((subscriptionGroup: SubscriptionGroup): void => {
                // Redirect to subscription groups detail path
                history.push(
                    getSubscriptionGroupsDetailPath(subscriptionGroup.id)
                );

                enqueueSnackbar(
                    t("message.create-success", {
                        entity: t("label.subscription-group"),
                    }),
                    getSuccessSnackbarOption()
                );
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.create-error", {
                        entity: t("label.subscription-group"),
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
                <SubscriptionGroupWizard
                    alerts={alerts}
                    onChange={onSubscriptionGroupWizardStepChange}
                    onFinish={onSubscriptionGroupWizardFinish}
                />
            </PageContents>
        </PageContainer>
    );
};
