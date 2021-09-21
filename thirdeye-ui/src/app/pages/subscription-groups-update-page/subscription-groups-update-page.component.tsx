import { toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SubscriptionGroupWizard } from "../../components/subscription-group-wizard/subscription-group-wizard.component";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { isValidNumberId } from "../../utils/params/params.util";
import { getSubscriptionGroupsViewPath } from "../../utils/routes/routes.util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { SubscriptionGroupsUpdatePageParams } from "./subscription-groups-update-page.interfaces";

export const SubscriptionGroupsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [
        subscriptionGroup,
        setSubscriptionGroup,
    ] = useState<SubscriptionGroup>();
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { enqueueSnackbar } = useSnackbar();
    const params = useParams<SubscriptionGroupsUpdatePageParams>();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Fetched subscription group changed, set breadcrumbs
        setPageBreadcrumbs([
            {
                text: subscriptionGroup ? subscriptionGroup.name : "",
                onClick: (): void => {
                    if (subscriptionGroup) {
                        history.push(
                            getSubscriptionGroupsViewPath(subscriptionGroup.id)
                        );
                    }
                },
            },
        ]);
    }, [subscriptionGroup]);

    useEffect(() => {
        fetchSubscriptionGroup();
    }, []);

    const onSubscriptionGroupWizardFinish = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        updateSubscriptionGroup(subscriptionGroup).then(
            (subscriptionGroup: SubscriptionGroup): void => {
                enqueueSnackbar(
                    t("message.update-success", {
                        entity: t("label.subscription-group"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Redirect to subscription groups detail path
                history.push(
                    getSubscriptionGroupsViewPath(subscriptionGroup.id)
                );
            }
        );
    };

    const fetchSubscriptionGroup = (): void => {
        // Validate id from URL
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );
            setLoading(false);

            return;
        }

        Promise.allSettled([
            getSubscriptionGroup(toNumber(params.id)),
            getAllAlerts(),
        ])
            .then(([subscriptionGroupResponse, alertsResponse]): void => {
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

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents centered title={t("label.update")}>
            {subscriptionGroup && (
                <SubscriptionGroupWizard
                    alerts={alerts}
                    subscriptionGroup={subscriptionGroup}
                    onFinish={onSubscriptionGroupWizardFinish}
                />
            )}

            {/* No data available message */}
            {!subscriptionGroup && <NoDataIndicator />}
        </PageContents>
    );
};
