import { Grid } from "@material-ui/core";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AppToolbarConfiguration } from "../../components/app-toolbar-configuration/app-toolbar-configuration.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
import { SubscriptionGroupCard } from "../../components/subscription-group-card/subscription-group-card.component";
import { SubscriptionGroupCardData } from "../../components/subscription-group-card/subscription-group-card.interfaces";
import { getAllAlerts } from "../../rest/alert-rest/alert-rest";
import { getAllSubscriptionGroups } from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { getConfigurationSubscriptionGroupsAllPath } from "../../utils/routes-util/routes-util";
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";
import {
    filterSubscriptionGroups,
    getSubscriptionGroupCardDatas,
} from "../../utils/subscription-group-util/subscription-group-util";

export const ConfigurationSubscriptionGroupsAllPage: FunctionComponent = () => {
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
                pathFn: getConfigurationSubscriptionGroupsAllPath,
            },
        ]);
    }, []);

    useEffect(() => {
        // Fetch data
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    useEffect(() => {
        // Fetched data, or search changed, reset
        setfilteredSubscriptionGroupCardDatas(
            filterSubscriptionGroups(subscriptionGroupCardDatas, searchWords)
        );
    }, [subscriptionGroupCardDatas, searchWords]);

    const fetchData = async (): Promise<void> => {
        let fetchedSubscriptionGroupCardDatas: SubscriptionGroupCardData[] = [];
        const [
            subscriptionGroupsResponse,
            alertsResponse,
        ] = await Promise.allSettled([
            getAllSubscriptionGroups(),
            getAllAlerts(),
        ]);

        if (
            alertsResponse.status === "rejected" ||
            subscriptionGroupsResponse.status === "rejected"
        ) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } else {
            fetchedSubscriptionGroupCardDatas = getSubscriptionGroupCardDatas(
                subscriptionGroupsResponse.value,
                alertsResponse.value
            );
        }

        setSubscriptionGroupCardDatas(fetchedSubscriptionGroupCardDatas);
    };

    if (loading) {
        return (
            <PageContainer appToolbar={<AppToolbarConfiguration />}>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer appToolbar={<AppToolbarConfiguration />}>
            <PageContents
                contentsCenterAlign
                hideTimeRange
                title={t("label.subscription-groups")}
            >
                <Grid container md={12}>
                    {/* Search */}
                    <Grid item md={12}>
                        <SearchBar
                            autoFocus
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
                                        subscriptionGroup={
                                            filteredSubscriptionGroupCardData
                                        }
                                    />
                                </Grid>
                            )
                        )}
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
