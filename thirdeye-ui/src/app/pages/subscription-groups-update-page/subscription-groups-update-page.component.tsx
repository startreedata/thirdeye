import { kebabCase, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SubscriptionGroupWizard } from "../../components/subscription-group-wizard/subscription-group-wizard.component";
import { SubscriptionGroupWizardStep } from "../../components/subscription-group-wizard/subscription-group-wizard.interfaces";
import { getAllAlerts } from "../../rest/alerts-rest/alerts-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "../../rest/subscription-groups-rest/subscription-groups-rest";
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
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [
        subscriptionGroup,
        setSubscriptionGroup,
    ] = useState<SubscriptionGroup>();
    const {
        setPageBreadcrumbs,
        pushPageBreadcrumb,
        popPageBreadcrumb,
    } = useAppBreadcrumbs();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<SubscriptionGroupsUpdatePageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: subscriptionGroup ? subscriptionGroup.name : "",
                onClick: (): void => {
                    if (subscriptionGroup) {
                        history.push(
                            getSubscriptionGroupsDetailPath(
                                subscriptionGroup.id
                            )
                        );
                    }
                },
            },
            {
                text: t("label.update"),
                onClick: (): void => {
                    if (subscriptionGroup) {
                        history.push(
                            getSubscriptionGroupsUpdatePath(
                                subscriptionGroup.id
                            )
                        );
                    }
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
    }, []);

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

        Promise.allSettled([
            getSubscriptionGroup(toNumber(params.id)),
            getAllAlerts(),
        ])
            .then(([subscriptionGroupResponse, alertsResponse]): void => {
                // Determine if any of the calls failed
                if (
                    subscriptionGroupResponse.status === "rejected" ||
                    alertsResponse.status === "rejected"
                ) {
                    enqueueSnackbar(
                        t("message.fetch-error"),
                        getErrorSnackbarOption()
                    );
                }

                // Attempt to gather data
                if (subscriptionGroupResponse.status === "fulfilled") {
                    setSubscriptionGroup(subscriptionGroupResponse.value);
                }
                if (alertsResponse.status === "fulfilled") {
                    setAlerts(alertsResponse.value);
                }
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
                        alerts={alerts}
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
