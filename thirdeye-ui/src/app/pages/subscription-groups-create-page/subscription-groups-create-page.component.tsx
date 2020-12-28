import { kebabCase } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router-dom";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SubscriptionGroupWizard } from "../../components/subscription-group-wizard/subscription-group-wizard.component";
import { SubscriptionGroupWizardStep } from "../../components/subscription-group-wizard/subscription-group-wizard.interfaces";
import { getAllAlerts } from "../../rest/alert-rest/alert-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { createSubscriptionGroup } from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import {
    getSubscriptionGroupsCreatePath,
    getSubscriptionGroupsDetailPath,
} from "../../utils/routes-util/routes-util";
import { getErrorSnackbarOption } from "../../utils/snackbar-util/snackbar-util";

export const SubscriptionGroupsCreatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [
        setPageBreadcrumbs,
        pushPageBreadcrumb,
        popPageBreadcrumb,
    ] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
        state.pushPageBreadcrumb,
        state.popPageBreadcrumb,
    ]);
    const history = useHistory();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.create"),
                pathFn: getSubscriptionGroupsCreatePath,
            },
            // Empty page breadcrumb as a placeholder for subscription group wizard step
            {
                text: "",
            },
        ]);
    }, []);

    useEffect(() => {
        setLoading(false);
    }, []);

    const onSubscriptionGroupWizardStepChange = (
        subscriptionGroupWizardStep: SubscriptionGroupWizardStep
    ): void => {
        // Update page breadcrubs
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

    const fetchAlerts = (): Promise<Alert[]> => {
        return new Promise<Alert[]>((resolve, reject): void => {
            getAllAlerts()
                .then((alerts: Alert[]): void => {
                    resolve(alerts);
                })
                .catch((error): void => {
                    enqueueSnackbar(
                        t("message.fetch-error"),
                        getErrorSnackbarOption()
                    );

                    reject(error);
                });
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
                    getAlerts={fetchAlerts}
                    onChange={onSubscriptionGroupWizardStepChange}
                    onFinish={onSubscriptionGroupWizardFinish}
                />
            </PageContents>
        </PageContainer>
    );
};
