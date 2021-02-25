import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { SubscriptionGroupCardData } from "../../components/entity-cards/subscription-group-card/subscription-group-card.interfaces";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SubscriptionGroupList } from "../../components/subscription-group-list/subscription-group-list.component";
import { getAllAlerts } from "../../rest/alerts/alerts.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    deleteSubscriptionGroup,
    getAllSubscriptionGroups,
} from "../../rest/subscription-groups/subscription-groups.rest";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar/snackbar.util";
import { getSubscriptionGroupCardDatas } from "../../utils/subscription-groups/subscription-groups.util";

export const SubscriptionGroupsAllPage: FunctionComponent = () => {
    const [
        subscriptionGroupCardDatas,
        setSubscriptionGroupCardDatas,
    ] = useState<SubscriptionGroupCardData[] | null>(null);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchAllSubscriptionGroups();
    }, []);

    const onDeleteSubscriptionGroup = (
        subscriptionGroupCardData: SubscriptionGroupCardData
    ): void => {
        if (!subscriptionGroupCardData) {
            return;
        }

        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", {
                name: subscriptionGroupCardData.name,
            }),
            okButtonLabel: t("label.delete"),
            onOk: (): void => {
                onDeleteSubscriptionGroupConfirmation(
                    subscriptionGroupCardData
                );
            },
        });
    };

    const onDeleteSubscriptionGroupConfirmation = (
        subscriptionGroupCardData: SubscriptionGroupCardData
    ): void => {
        if (!subscriptionGroupCardData) {
            return;
        }

        deleteSubscriptionGroup(subscriptionGroupCardData.id)
            .then((subscriptionGroup: SubscriptionGroup): void => {
                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.subscription-group"),
                    }),
                    getSuccessSnackbarOption()
                );

                // Remove deleted subscription group from fetched subscription groups
                removeSubscriptionGroupCardData(subscriptionGroup);
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.delete-error", {
                        entity: t("label.subscription-group"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const fetchAllSubscriptionGroups = (): void => {
        setSubscriptionGroupCardDatas(null);
        let fetchedSubscriptionGroupCardDatas: SubscriptionGroupCardData[] = [];
        Promise.allSettled([getAllSubscriptionGroups(), getAllAlerts()])
            .then(([subscriptionGroupsResponse, alertsResponse]): void => {
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
                let fetchedAlerts: Alert[] = [];
                if (alertsResponse.status === "fulfilled") {
                    fetchedAlerts = alertsResponse.value;
                }
                if (subscriptionGroupsResponse.status === "fulfilled") {
                    fetchedSubscriptionGroupCardDatas = getSubscriptionGroupCardDatas(
                        subscriptionGroupsResponse.value,
                        fetchedAlerts
                    );
                }
            })
            .finally((): void => {
                setSubscriptionGroupCardDatas(
                    fetchedSubscriptionGroupCardDatas
                );
            });
    };

    const removeSubscriptionGroupCardData = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        setSubscriptionGroupCardDatas(
            (subscriptionGroupCardDatas) =>
                subscriptionGroupCardDatas &&
                subscriptionGroupCardDatas.filter(
                    (
                        subscriptionGroupCardData: SubscriptionGroupCardData
                    ): boolean => {
                        return (
                            subscriptionGroupCardData.id !==
                            subscriptionGroup.id
                        );
                    }
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
                subscriptionGroupCardDatas={subscriptionGroupCardDatas}
                onDelete={onDeleteSubscriptionGroup}
            />
        </PageContents>
    );
};
