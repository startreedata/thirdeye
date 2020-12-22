import { Grid } from "@material-ui/core";
import { isEmpty, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { ConfigurationToolbar } from "../../components/configuration-toolbar/configuration-toolbar.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SubscriptionGroupCardData } from "../../components/subscription-group-card/subscription-group.interfaces";
import { getAllAlerts } from "../../rest/alert-rest/alert-rest";
import { getSubscriptionGroup } from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { isValidNumberId } from "../../utils/params-util/params-util";
import { getConfigurationSubscriptionGroupsDetailPath } from "../../utils/routes-util/routes-util";
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";
import { getSubscriptionGroupCardData } from "../../utils/subscription-group-util/subscription-group-util";
import { SubscriptionGroupCard } from "./../../components/subscription-group-card/subscription-group-card.component";
import { SubscriptionGroupPageParams } from "./configuration-subscription-group-details-page.interfaces";

export const ConfigurationSubscriptionGroupsDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const [subscriptionGroup, setSubscriptionGroup] = useState<
        SubscriptionGroupCardData
    >({} as SubscriptionGroupCardData);

    const params = useParams<SubscriptionGroupPageParams>();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: subscriptionGroup
                    ? subscriptionGroup.name
                    : t("label.no-data-available-marker"),
                pathFn: (): string => {
                    return subscriptionGroup
                        ? getConfigurationSubscriptionGroupsDetailPath(
                              subscriptionGroup.id
                          )
                        : "";
                },
            },
        ]);
    }, [subscriptionGroup]);

    useEffect(() => {
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    const fetchData = async (): Promise<void> => {
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                }),
                SnackbarOption.ERROR
            );

            return;
        }

        let subscriptionGroup = {} as SubscriptionGroupCardData;
        const [
            subscriptionGroupResponse,
            alertsResponse,
        ] = await Promise.allSettled([
            getSubscriptionGroup(toNumber(params.id)),
            getAllAlerts(),
        ]);

        if (
            alertsResponse.status === "rejected" ||
            subscriptionGroupResponse.status === "rejected"
        ) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } else {
            subscriptionGroup = getSubscriptionGroupCardData(
                subscriptionGroupResponse.value,
                alertsResponse.value
            );
        }

        setSubscriptionGroup(subscriptionGroup);
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
                {!isEmpty(subscriptionGroup) && (
                    <Grid container>
                        {/* Subscription Group */}
                        <Grid item md={12}>
                            <SubscriptionGroupCard
                                hideViewDetailsLinks
                                subscriptionGroup={subscriptionGroup}
                            />
                        </Grid>
                    </Grid>
                )}
            </PageContents>
        </PageContainer>
    );
};
