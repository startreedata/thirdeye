import { Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
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
import { createSubscriptionGroup } from "../../rest/subscription-groups/subscription-groups.rest";
import { getSubscriptionGroupsViewPath } from "../../utils/routes/routes.util";

export const SubscriptionGroupsCreatePage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        fetchAllAlerts();
    }, []);

    const onSubscriptionGroupWizardFinish = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        createSubscriptionGroup(subscriptionGroup).then(
            (subscriptionGroup: SubscriptionGroup): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", {
                        entity: t("label.subscription-group"),
                    })
                );

                // Redirect to subscription groups detail path
                navigate(getSubscriptionGroupsViewPath(subscriptionGroup.id));
            }
        );
    };

    const fetchAllAlerts = (): void => {
        getAllAlerts()
            .then((alerts: Alert[]): void => {
                setAlerts(alerts);
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
                title={t("label.create-entity", {
                    entity: t("label.subscription-group"),
                })}
            />
            <PageContentsGridV1>
                <Grid item xs={12}>
                    <SubscriptionGroupWizard
                        alerts={alerts}
                        onFinish={onSubscriptionGroupWizardFinish}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
