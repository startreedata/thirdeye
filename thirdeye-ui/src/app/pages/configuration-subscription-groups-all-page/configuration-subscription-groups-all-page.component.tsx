import { Grid } from "@material-ui/core";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { ConfigurationToolbar } from "../../components/configuration-toolbar/configuration-toolbar.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SearchBar } from "../../components/search-bar/search-bar.component";
import { SubscriptionGroupCard } from "../../components/subscription-group-card/subscription-group-card.component";
import { SubscriptionGroupCardData } from "../../components/subscription-group-card/subscription-group.interfaces";
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
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);

    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroupCardData[]
    >([]);
    const [
        filteredSubscriptionGroups,
        setfilteredSubscriptionGroups,
    ] = useState<SubscriptionGroupCardData[]>([]);
    const [searchWords, setSearchWords] = useState<string[]>([]);
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
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    const fetchData = async (): Promise<void> => {
        let subscriptionGroups: SubscriptionGroupCardData[] = [];

        const [
            alertsResponse,
            subscriptionGroupsResponse,
        ] = await Promise.allSettled([
            getAllAlerts(),
            getAllSubscriptionGroups(),
        ]);

        if (
            alertsResponse.status === "rejected" ||
            subscriptionGroupsResponse.status === "rejected"
        ) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } else {
            subscriptionGroups = getSubscriptionGroupCardDatas(
                subscriptionGroupsResponse.value,
                alertsResponse.value
            );
        }

        setSubscriptionGroups(subscriptionGroups);
        setfilteredSubscriptionGroups(
            filterSubscriptionGroups(subscriptionGroups, searchWords)
        );
    };

    const onSearch = (searchWords: string[]): void => {
        setSearchWords(searchWords);
        setfilteredSubscriptionGroups(
            filterSubscriptionGroups(subscriptionGroups, searchWords)
        );
    };

    if (loading) {
        return (
            <PageContainer toolbar={<ConfigurationToolbar />}>
                <LoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <PageContainer toolbar={<ConfigurationToolbar />}>
            <PageContents
                contentsCenterAlign
                hideTimeRange
                title={t("label.subscription-groups")}
            >
                <Grid container>
                    <Grid item md={12}>
                        <SearchBar
                            autoFocus
                            searchStatusLabel={t("label.search-count", {
                                count: filteredSubscriptionGroups.length,
                                total: subscriptionGroups.length,
                            })}
                            onChange={onSearch}
                        />
                    </Grid>

                    {filteredSubscriptionGroups.map((subscriptionGroup) => (
                        <Grid item key={subscriptionGroup.id} md={12}>
                            <SubscriptionGroupCard
                                searchWords={searchWords}
                                subscriptionGroup={subscriptionGroup}
                            />
                        </Grid>
                    ))}
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
