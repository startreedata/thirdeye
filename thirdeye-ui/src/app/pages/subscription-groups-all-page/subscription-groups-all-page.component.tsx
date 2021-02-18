import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { SubscriptionGroupCard } from "../../components/entity-cards/subscription-group-card/subscription-group-card.component";
import { SubscriptionGroupCardData } from "../../components/entity-cards/subscription-group-card/subscription-group-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
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
import {
    filterSubscriptionGroups,
    getSubscriptionGroupCardDatas,
} from "../../utils/subscription-groups/subscription-groups.util";

export const SubscriptionGroupsAllPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [
        subscriptionGroupCardDatas,
        setSubscriptionGroupCardDatas,
    ] = useState<SubscriptionGroupCardData[]>([]);
    const [
        filteredSubscriptionGroupCardDatas,
        setfilteredSubscriptionGroupCardDatas,
    ] = useState<SubscriptionGroupCardData[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
    const { setPageBreadcrumbs } = useAppBreadcrumbs();
    const { showDialog } = useDialog();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        setPageBreadcrumbs([]);
        fetchAllSubscriptionGroups();
    }, []);

    useEffect(() => {
        // Fetched subscription groups or search changed, reset
        setfilteredSubscriptionGroupCardDatas(
            filterSubscriptionGroups(subscriptionGroupCardDatas, searchWords)
        );
    }, [subscriptionGroupCardDatas, searchWords]);

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
                    setSubscriptionGroupCardDatas(
                        getSubscriptionGroupCardDatas(
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

    const removeSubscriptionGroupCardData = (
        subscriptionGroup: SubscriptionGroup
    ): void => {
        if (!subscriptionGroup) {
            return;
        }

        setSubscriptionGroupCardDatas((subscriptionGroupCardDatas) =>
            subscriptionGroupCardDatas.filter(
                (
                    subscriptionGroupCardData: SubscriptionGroupCardData
                ): boolean => {
                    return (
                        subscriptionGroupCardData.id !== subscriptionGroup.id
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
            <Grid container>
                {/* Search */}
                <Grid item sm={12}>
                    <SearchBar
                        autoFocus
                        setSearchQueryString
                        searchLabel={t("label.search-subscription-groups")}
                        searchStatusLabel={t("label.search-count", {
                            count: filteredSubscriptionGroupCardDatas
                                ? filteredSubscriptionGroupCardDatas.length
                                : 0,
                            total: subscriptionGroupCardDatas
                                ? subscriptionGroupCardDatas.length
                                : 0,
                        })}
                        onChange={setSearchWords}
                    />
                </Grid>

                {/* Subscription groups */}
                {filteredSubscriptionGroupCardDatas &&
                    filteredSubscriptionGroupCardDatas.map(
                        (filteredSubscriptionGroupCardData, index) => (
                            <Grid item key={index} sm={12}>
                                <SubscriptionGroupCard
                                    showViewDetails
                                    searchWords={searchWords}
                                    subscriptionGroupCardData={
                                        filteredSubscriptionGroupCardData
                                    }
                                    onDelete={onDeleteSubscriptionGroup}
                                />
                            </Grid>
                        )
                    )}
            </Grid>

            {/* No data available message */}
            {isEmpty(filteredSubscriptionGroupCardDatas) &&
                isEmpty(searchWords) && <NoDataIndicator />}

            {/* No search results available message */}
            {isEmpty(filteredSubscriptionGroupCardDatas) &&
                !isEmpty(searchWords) && (
                    <NoDataIndicator text={t("message.no-search-results")} />
                )}
        </PageContents>
    );
};
