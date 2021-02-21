import { isEmpty } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SubscriptionGroupList } from "../../components/subscription-group-list/subscription-group-list.component";
import { SubscriptionGroupListData } from "../../components/subscription-group-list/subscription-group-list.interfaces";
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
import { getSubscriptionGroupListDatas } from "../../utils/subscription-groups/subscription-groups.util";

export const SubscriptionGroupsAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [
        subscriptionGroupListDatas,
        setSubscriptionGroupListDatas,
    ] = useState<SubscriptionGroupListData[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchAllSubscriptionGroups();
    }, []);

    useEffect(() => {
        // Fetched subscription groups or search changed, reset
        setSubscriptionGroupListDatas(subscriptionGroupListDatas);
    }, [subscriptionGroupListDatas]);

    const onDeleteSubscriptionGroup = (
        subscriptionGroupListData: SubscriptionGroupListData[]
    ): void => {
        if (isEmpty(subscriptionGroupListData)) {
            return;
        }
        for (const subscriptionGroupData of subscriptionGroupListData) {
            deleteSubscriptionGroup(subscriptionGroupData.id)
                .then((subscriptionGroup: SubscriptionGroup): void => {
                    enqueueSnackbar(
                        t("message.delete-success", {
                            entity: t("label.subscription-group"),
                        }),
                        getSuccessSnackbarOption()
                    );

                    // Remove deleted subscription group from fetched subscription groups
                    removeSubscriptionGroupListData(subscriptionGroup);
                })
                .catch((): void => {
                    enqueueSnackbar(
                        t("message.delete-error", {
                            entity: t("label.subscription-group"),
                        }),
                        getErrorSnackbarOption()
                    );
                });
        }
    };

    const fetchAllSubscriptionGroups = (): void => {
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
                    setSubscriptionGroupListDatas(
                        getSubscriptionGroupListDatas(
                            subscriptionGroupsResponse.value,
                            fetchedAlerts
                        )
                    );
                }
            })
            .finally((): void => {
                setLoading(false);
            });
    };

    const removeSubscriptionGroupListData = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        setSubscriptionGroupListDatas((subscriptionGroupListDatas) =>
            subscriptionGroupListDatas.filter(
                (
                    subscriptionGroupListData: SubscriptionGroupListData
                ): boolean => {
                    return (
                        subscriptionGroupListData.id !== subscriptionGroup.id
                    );
                }
            )
        );
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <PageContents
            centered
            hideTimeRange
            maxRouterBreadcrumbs={1}
            title={t("label.subscription-groups")}
        >
            <SubscriptionGroupList
                subscriptionGroups={subscriptionGroupListDatas}
                onDelete={onDeleteSubscriptionGroup}
            />
        </PageContents>
    );
};
