import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationPageHeader } from "../../components/configuration-page-header/configuration-page-header.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { SubscriptionGroupListV1 } from "../../components/subscription-group-list-v1/subscription-group-list-v1.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";
import {
    deleteSubscriptionGroup,
    getAllSubscriptionGroups,
} from "../../rest/subscription-groups/subscription-groups.rest";
import { getUiSubscriptionGroups } from "../../utils/subscription-groups/subscription-groups.util";

export const SubscriptionGroupsAllPage: FunctionComponent = () => {
    const [uiSubscriptionGroups, setUiSubscriptionGroups] = useState<
        UiSubscriptionGroup[] | null
    >(null);
    const { showDialog } = useDialog();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Time range refreshed, fetch subscription groups
        fetchAllSubscriptionGroups();
    }, []);

    const fetchAllSubscriptionGroups = (): void => {
        setUiSubscriptionGroups(null);

        let fetchedUiSubscriptionGroups: UiSubscriptionGroup[] = [];
        let fetchedAlerts: Alert[] = [];
        Promise.allSettled([getAllSubscriptionGroups(), getAllAlerts()])
            .then(([subscriptionGroupsResponse, alertsResponse]) => {
                // Attempt to gather data
                if (alertsResponse.status === "fulfilled") {
                    fetchedAlerts = alertsResponse.value;
                }
                if (subscriptionGroupsResponse.status === "fulfilled") {
                    fetchedUiSubscriptionGroups = getUiSubscriptionGroups(
                        subscriptionGroupsResponse.value,
                        fetchedAlerts
                    );
                }
            })
            .finally(() => {
                setUiSubscriptionGroups(fetchedUiSubscriptionGroups);
            });
    };

    const handleSubscriptionGroupDelete = (
        uiSubscriptionGroup: UiSubscriptionGroup
    ): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: uiSubscriptionGroup.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleSubscriptionGroupDeleteOk(uiSubscriptionGroup),
        });
    };

    const handleSubscriptionGroupDeleteOk = (
        uiSubscriptionGroup: UiSubscriptionGroup
    ): void => {
        deleteSubscriptionGroup(uiSubscriptionGroup.id).then(
            (subscriptionGroup) => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.delete-success", {
                        entity: t("label.subscription-group"),
                    })
                );

                // Remove deleted subscription group from fetched subscription groups
                removeUiSubscriptionGroup(subscriptionGroup);
            }
        );
    };

    const removeUiSubscriptionGroup = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        setUiSubscriptionGroups(
            (uiSubscriptionGroups) =>
                uiSubscriptionGroups &&
                uiSubscriptionGroups.filter(
                    (uiSubscriptionGroup) =>
                        uiSubscriptionGroup.id !== subscriptionGroup.id
                )
        );
    };

    return (
        <PageV1>
            <ConfigurationPageHeader selectedIndex={4} />
            <PageContentsGridV1 fullHeight>
                <SubscriptionGroupListV1
                    subscriptionGroups={uiSubscriptionGroups}
                    onDelete={handleSubscriptionGroupDelete}
                />
            </PageContentsGridV1>
        </PageV1>
    );
};
