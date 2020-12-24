import { Grid } from "@material-ui/core";
import { cloneDeep, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { AppToolbarConfiguration } from "../../components/app-toolbar-configuration/app-toolbar-configuration.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardData,
} from "../../components/subscription-group-card/subscription-group-card.interfaces";
import { TransferList } from "../../components/transfer-list/transfer-list.component";
import { getAllAlerts } from "../../rest/alert-rest/alert-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { isValidNumberId } from "../../utils/params-util/params-util";
import { getConfigurationSubscriptionGroupsDetailPath } from "../../utils/routes-util/routes-util";
import { SnackbarOption } from "../../utils/snackbar-util/snackbar-util";
import {
    createEmptySubscriptionGroupCardData,
    getSubscriptionGroupCardData,
} from "../../utils/subscription-group-util/subscription-group-util";
import { SubscriptionGroupCard } from "./../../components/subscription-group-card/subscription-group-card.component";
import { ConfigurationSubscriptionGroupDetailsPageParams } from "./configuration-subscription-group-details-page.interfaces";

export const ConfigurationSubscriptionGroupsDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [subscriptionGroupCardData, setSubscriptionGroupCardData] = useState<
        SubscriptionGroupCardData
    >({} as SubscriptionGroupCardData);
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const params = useParams<ConfigurationSubscriptionGroupDetailsPageParams>();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: subscriptionGroupCardData
                    ? subscriptionGroupCardData.name
                    : t("label.no-data-available-marker"),
                pathFn: (): string => {
                    return subscriptionGroupCardData
                        ? getConfigurationSubscriptionGroupsDetailPath(
                              subscriptionGroupCardData.id
                          )
                        : "";
                },
            },
        ]);
    }, [subscriptionGroupCardData]);

    useEffect(() => {
        // Fetch data
        const init = async (): Promise<void> => {
            await fetchData();

            setLoading(false);
        };

        init();
    }, []);

    const fetchData = async (): Promise<void> => {
        let fetchedSubscriptionGroupCardData = createEmptySubscriptionGroupCardData();
        let fetchedAlerts: Alert[] = [];

        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                }),
                SnackbarOption.ERROR
            );

            setSubscriptionGroupCardData(fetchedSubscriptionGroupCardData);

            return;
        }

        const [
            subscriptionGroupResponse,
            alertsResponse,
        ] = await Promise.allSettled([
            getSubscriptionGroup(toNumber(params.id)),
            getAllAlerts(),
        ]);

        if (
            subscriptionGroupResponse.status === "rejected" ||
            alertsResponse.status === "rejected"
        ) {
            enqueueSnackbar(t("message.fetch-error"), SnackbarOption.ERROR);
        } else {
            fetchedSubscriptionGroupCardData = getSubscriptionGroupCardData(
                subscriptionGroupResponse.value,
                alertsResponse.value
            );
            fetchedAlerts = alertsResponse.value;
        }

        setSubscriptionGroupCardData(fetchedSubscriptionGroupCardData);
        setAlerts(fetchedAlerts);
    };

    const onSubscriptionGroupAlertsChange = async (
        subscriptiongroupAlerts: SubscriptionGroupAlert[]
    ): Promise<void> => {
        if (
            !subscriptionGroupCardData ||
            !subscriptionGroupCardData.subscriptionGroup
        ) {
            return;
        }

        let subscriptionGroupCopy = cloneDeep(
            subscriptionGroupCardData.subscriptionGroup
        );
        subscriptionGroupCopy.alerts = subscriptiongroupAlerts as Alert[];

        try {
            subscriptionGroupCopy = await updateSubscriptionGroup(
                subscriptionGroupCopy
            );

            // Replace updated subscription as fetched subscription group
            setSubscriptionGroupCardData(
                getSubscriptionGroupCardData(subscriptionGroupCopy, alerts)
            );
        } catch (error) {
            enqueueSnackbar(
                t("message.update-error", {
                    entity: t("label.subscription-group"),
                }),
                SnackbarOption.ERROR
            );

            // Undo changes to the subscription group
            subscriptionGroupCardData.alerts = Array.from(
                subscriptionGroupCardData.alerts
            );
        }
    };

    const transferListGetKey = (alert: SubscriptionGroupAlert): number => {
        if (!alert) {
            return -1;
        }

        return alert.id;
    };

    const transferListRenderer = (alert: SubscriptionGroupAlert): string => {
        if (!alert) {
            return "";
        }

        return alert.name;
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
                title={
                    subscriptionGroupCardData
                        ? subscriptionGroupCardData.name
                        : t("label.no-data-available-marker")
                }
            >
                <Grid container>
                    {/* Subscription Group */}
                    <Grid item md={12}>
                        <SubscriptionGroupCard
                            hideViewDetailsLinks
                            subscriptionGroup={subscriptionGroupCardData}
                        />
                    </Grid>

                    {/* Alerts transfer list */}
                    <Grid item md={12}>
                        <TransferList<Alert>
                            fromLabel={t("label.all-alerts")}
                            fromList={alerts}
                            getKey={transferListGetKey}
                            renderer={transferListRenderer}
                            toLabel={t("label.subscribed-alerts")}
                            toList={subscriptionGroupCardData.alerts as Alert[]}
                            onChange={onSubscriptionGroupAlertsChange}
                        />
                    </Grid>
                </Grid>
            </PageContents>
        </PageContainer>
    );
};
