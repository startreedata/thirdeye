import { Grid } from "@material-ui/core";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { SubscriptionGroupWizard } from "../../components/subscription-group-wizard/subscription-group-wizard.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { isValidNumberId } from "../../utils/params/params.util";
import { getSubscriptionGroupsViewPath } from "../../utils/routes/routes.util";
import { SubscriptionGroupsUpdatePageParams } from "./subscription-groups-update-page.interfaces";

export const SubscriptionGroupsUpdatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [subscriptionGroup, setSubscriptionGroup] =
        useState<SubscriptionGroup>();
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const params = useParams<SubscriptionGroupsUpdatePageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

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
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", {
                        entity: t("label.subscription-group"),
                    })
                );

                // Redirect to subscription groups detail path
                navigate(getSubscriptionGroupsViewPath(subscriptionGroup.id));
            }
        );
    };

    const fetchSubscriptionGroup = (): void => {
        // Validate id from URL
        if (params.id && !isValidNumberId(params.id)) {
            notify(
                NotificationTypeV1.Error,
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                })
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
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <PageV1>
            <PageHeader
                title={t("label.update-entity", {
                    entity: t("label.subscription-group"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    {subscriptionGroup && (
                        <SubscriptionGroupWizard
                            alerts={alerts}
                            subscriptionGroup={subscriptionGroup}
                            onFinish={onSubscriptionGroupWizardFinish}
                        />
                    )}

                    {/* No data available message */}
                    {!subscriptionGroup && <NoDataIndicator />}
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
