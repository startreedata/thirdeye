import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SubscriptionGroupList } from "../../components/subscription-group-list/subscription-group-list.component";
import { useTimeRange } from "../../components/time-range/time-range-provider/time-range-provider.component";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { UiSubscriptionGroup } from "../../rest/dto/ui-subscription-group.interfaces";
import {
    deleteSubscriptionGroup,
    getAllSubscriptionGroups,
} from "../../rest/subscription-groups/subscription-groups.rest";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { getUiSubscriptionGroups } from "../../utils/subscription-groups/subscription-groups.util";

export const SubscriptionGroupsAllPage: FunctionComponent = () => {
    const [uiSubscriptionGroups, setUiSubscriptionGroups] = useState<
        UiSubscriptionGroup[] | null
    >(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { timeRangeDuration } = useTimeRange();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
    }, []);

    useEffect(() => {
        // Time range refreshed, fetch subscription groups
        fetchAllSubscriptionGroups();
    }, [timeRangeDuration]);

    const fetchAllSubscriptionGroups = (): void => {
        setUiSubscriptionGroups(null);

        let fetchedUiSubscriptionGroups: UiSubscriptionGroup[] = [];
        let fetchedAlerts: Alert[] = [];
        Promise.allSettled([getAllSubscriptionGroups(), getAllAlerts()])
            .then(([subscriptionGroupsResponse, alertsResponse]) => {
                // Determine if any of the calls failed
                if (
                    subscriptionGroupsResponse.status === "rejected" ||
                    alertsResponse.status === "rejected"
                ) {
                    enqueueSnackbar(
                        t("message.fetch-error"),
                        getErrorSnackbarOption()
                    );
                }

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
        if (!uiSubscriptionGroup) {
            return;
        }

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
        if (!uiSubscriptionGroup) {
            return;
        }

        deleteSubscriptionGroup(uiSubscriptionGroup.id)
            .then((subscriptionGroup) => {
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.subscription-group"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted subscription group from fetched subscription groups
                removeUiSubscriptionGroup(subscriptionGroup);
            })
            .catch(() =>
                enqueueSnackbar(
                    t("message.delete-error", {
                        entity: t("label.subscription-group"),
                    }),
                    getErrorSnackbarOption()
                )
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
        <PageContents
            centered
            hideTimeRange
            maxRouterBreadcrumbs={1}
            title={t("label.subscription-groups")}
        >
            <SubscriptionGroupList
                uiSubscriptionGroups={uiSubscriptionGroups}
                onDelete={handleSubscriptionGroupDelete}
            />
        </PageContents>
    );
};
