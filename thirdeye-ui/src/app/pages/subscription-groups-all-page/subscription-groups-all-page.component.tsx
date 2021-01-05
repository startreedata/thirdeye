import { Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { SubscriptionGroupCard } from "../../components/entity-card/subscription-group-card/subscription-group-card.component";
import { SubscriptionGroupCardData } from "../../components/entity-card/subscription-group-card/subscription-group-card.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
import { getAllAlerts } from "../../rest/alert-rest/alert-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    deleteSubscriptionGroup,
    getAllSubscriptionGroups,
} from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { getSubscriptionGroupsAllPath } from "../../utils/routes-util/routes-util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar-util/snackbar-util";
import {
    filterSubscriptionGroups,
    getSubscriptionGroupCardDatas,
} from "../../utils/subscription-groups-util/subscription-groups-util";

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
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: t("label.all"),
                pathFn: getSubscriptionGroupsAllPath,
            },
        ]);
    }, []);

    useEffect(() => {
        // Fetch data
        fetchData();
    }, []);

    useEffect(() => {
        // Fetched data, or search changed, reset
        setfilteredSubscriptionGroupCardDatas(
            filterSubscriptionGroups(subscriptionGroupCardDatas, searchWords)
        );
    }, [subscriptionGroupCardDatas, searchWords]);

    const fetchData = (): void => {
        let fetchedSubscriptionGroupCardDatas: SubscriptionGroupCardData[] = [];
        let fetchedAlerts: Alert[] = [];

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

                setLoading(false);
            });
    };

    const onDeleteSubscriptionGroup = (
        subscriptionGroupCardData: SubscriptionGroupCardData
    ): void => {
        if (!subscriptionGroupCardData) {
            return;
        }

        // Delete
        deleteSubscriptionGroup(subscriptionGroupCardData.id)
            .then((subscriptionGroup: SubscriptionGroup): void => {
                // Remove deleted subscription group from fetched subscription
                // groups
                removeSubscriptionGroupCardData(subscriptionGroup);

                enqueueSnackbar(
                    t("message.delete-success", {
                        entity: t("label.subscription-group"),
                    }),
                    getSuccessSnackbarOption()
                );
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
        return (
            <PageContainer>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer>
            <PageContents
                centered
                hideTimeRange
                title={t("label.subscription-groups")}
            >
                <Grid container>
                    {/* Search */}
                    <Grid item md={12}>
                        <SearchBar
                            autoFocus
                            setSearchQueryString
                            label={t("label.search-subscription-groups")}
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
                                <Grid item key={index} md={12}>
                                    <SubscriptionGroupCard
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
                {isEmpty(filteredSubscriptionGroupCardDatas) && (
                    <NoDataIndicator />
                )}
            </PageContents>
        </PageContainer>
    );
};
