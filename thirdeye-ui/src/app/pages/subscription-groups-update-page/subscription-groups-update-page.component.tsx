import { kebabCase, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SubscriptionGroupWizard } from "../../components/subscription-group-wizard/subscription-group-wizard.component";
import { SubscriptionGroupWizardStep } from "../../components/subscription-group-wizard/subscription-group-wizard.interfaces";
import { getAllAlerts } from "../../rest/alert-rest/alert-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { isValidNumberId } from "../../utils/params-util/params-util";
import {
    getSubscriptionGroupsDetailPath,
    getSubscriptionGroupsUpdatePath,
} from "../../utils/routes-util/routes-util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar-util/snackbar-util";
import { SubscriptionGroupsUpdatePageParams } from "./subscription-groups-update-page.interfaces";

export const SubscriptionGroupsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [subscriptionGroup, setSubscriptionGroup] = useState<
        SubscriptionGroup
    >();
    const [
        setPageBreadcrumbs,
        pushPageBreadcrumb,
        popPageBreadcrumb,
    ] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
        state.pushPageBreadcrumb,
        state.popPageBreadcrumb,
    ]);
    const params = useParams<SubscriptionGroupsUpdatePageParams>();
    const history = useHistory();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: subscriptionGroup ? subscriptionGroup.name : "",
                pathFn: (): string => {
                    return subscriptionGroup
                        ? getSubscriptionGroupsDetailPath(subscriptionGroup.id)
                        : "";
                },
            },
            {
                text: t("label.update"),
                pathFn: (): string => {
                    return subscriptionGroup
                        ? getSubscriptionGroupsUpdatePath(subscriptionGroup.id)
                        : "";
                },
            },
            // Empty page breadcrumb as a placeholder for subscription group wizard step
            {
                text: "",
            },
        ]);
    }, [subscriptionGroup]);

    useEffect(() => {
        // Fetch data
        fetchData();
    }, [params.id]);

    const fetchData = (): void => {
        // Validate alert id from URL
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );

            return;
        }

        getSubscriptionGroup(toNumber(params.id))
            .then((subscriptionGroup: SubscriptionGroup): void => {
                setSubscriptionGroup(subscriptionGroup);
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
        updateSubscriptionGroup(subscriptionGroup)
            .then((subscriptionGroup: SubscriptionGroup): void => {
                // Redirect to subscription groups detail path
                history.push(
                    getSubscriptionGroupsDetailPath(subscriptionGroup.id)
                );

                enqueueSnackbar(
                    t("message.update-success", {
                        entity: t("label.subscription-group"),
                    }),
                    getSuccessSnackbarOption()
                );
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", {
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
                {subscriptionGroup && (
                    <SubscriptionGroupWizard
                        getAlerts={fetchAlerts}
                        subscriptionGroup={subscriptionGroup}
                        onChange={onSubscriptionGroupWizardStepChange}
                        onFinish={onSubscriptionGroupWizardFinish}
                    />
                )}

                {/* No data available message */}
                {!subscriptionGroup && <NoDataIndicator />}
            </PageContents>
        </PageContainer>
    );
};
