import { Grid, Typography } from "@material-ui/core";
import { cloneDeep, toNumber } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useParams } from "react-router-dom";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageContents } from "../../components/page-contents/page-contents.component";
import { SubscriptionGroupCard } from "../../components/subscription-group-card/subscription-group-card.component";
import {
    SubscriptionGroupAlert,
    SubscriptionGroupCardData,
} from "../../components/subscription-group-card/subscription-group-card.interfaces";
import { TransferList } from "../../components/transfer-list/transfer-list.component";
import { getAllAlerts } from "../../rest/alert-rest/alert-rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import {
    deleteSubscriptionGroup,
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "../../rest/subscription-group-rest/subscription-group-rest";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { isValidNumberId } from "../../utils/params-util/params-util";
import {
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsDetailPath,
} from "../../utils/routes-util/routes-util";
import {
    getErrorSnackbarOption,
    getSuccessSnackbarOption,
} from "../../utils/snackbar-util/snackbar-util";
import {
    getSubscriptionGroupAlerts,
    getSubscriptionGroupCardData,
} from "../../utils/subscription-group-util/subscription-group-util";
import { SubscriptionGroupsDetailPageParams } from "./subscription-groups-detail-page.interfaces";

export const SubscriptionGroupsDetailPage: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [subscriptionGroupCardData, setSubscriptionGroupCardData] = useState<
        SubscriptionGroupCardData
    >();
    const [alerts, setAlerts] = useState<Alert[]>([]);
    const [setPageBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setPageBreadcrumbs,
    ]);
    const params = useParams<SubscriptionGroupsDetailPageParams>();
    const history = useHistory();
    const { enqueueSnackbar } = useSnackbar();
    const { t } = useTranslation();

    useEffect(() => {
        // Create page breadcrumbs
        setPageBreadcrumbs([
            {
                text: subscriptionGroupCardData
                    ? subscriptionGroupCardData.name
                    : "",
                pathFn: (): string => {
                    return subscriptionGroupCardData
                        ? getSubscriptionGroupsDetailPath(
                              subscriptionGroupCardData.id
                          )
                        : "";
                },
            },
        ]);
    }, [subscriptionGroupCardData]);

    useEffect(() => {
        // Fetch data
        fetchData();
    }, [params.id]);

    const fetchData = (): void => {
        let fetchedAlerts: Alert[] = [];

        // Validate alert id from URL
        if (!isValidNumberId(params.id)) {
            enqueueSnackbar(
                t("message.invalid-id", {
                    entity: t("label.subscription-group"),
                    id: params.id,
                }),
                getErrorSnackbarOption()
            );

            return;
        }

        Promise.allSettled([
            getSubscriptionGroup(toNumber(params.id)),
            getAllAlerts(),
        ])
            .then(([subscriptionGroupResponse, alertsResponse]): void => {
                // Determine if any of the calls failed
                if (
                    subscriptionGroupResponse.status === "rejected" ||
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
                if (subscriptionGroupResponse.status === "fulfilled") {
                    setSubscriptionGroupCardData(
                        getSubscriptionGroupCardData(
                            subscriptionGroupResponse.value,
                            fetchedAlerts
                        )
                    );
                }
            })
            .finally((): void => {
                setAlerts(fetchedAlerts);

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
            .then((): void => {
                // Redirect to subscription groups all path
                history.push(getSubscriptionGroupsAllPath());

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

    const onSubscriptionGroupAlertsChange = (
        subscriptiongroupAlerts: SubscriptionGroupAlert[]
    ): void => {
        if (
            !subscriptionGroupCardData ||
            !subscriptionGroupCardData.subscriptionGroup
        ) {
            return;
        }

        // Create a copy of subscription group and update alerts
        const subscriptionGroupCopy = cloneDeep(
            subscriptionGroupCardData.subscriptionGroup
        );
        subscriptionGroupCopy.alerts = subscriptiongroupAlerts as Alert[];

        // Update
        updateSubscriptionGroup(subscriptionGroupCopy)
            .then((subscriptionGroup: SubscriptionGroup): void => {
                // Replace updated subscription group as fetched subscription group
                setSubscriptionGroupCardData(
                    getSubscriptionGroupCardData(subscriptionGroup, alerts)
                );

                enqueueSnackbar(
                    t("message.update-success", {
                        entity: t("label.subscription-group"),
                    }),
                    getSuccessSnackbarOption()
                );
            })
            .catch((): void => {
                enqueueSnackbar(
                    t("message.update-error", {
                        entity: t("label.subscription-group"),
                    }),
                    getErrorSnackbarOption()
                );
            });
    };

    const getSubscriptionGroupAlertId = (
        subscriptionGroupAlert: SubscriptionGroupAlert
    ): number => {
        if (!subscriptionGroupAlert) {
            return -1;
        }

        return subscriptionGroupAlert.id;
    };

    const getSubscriptionGroupAlertName = (
        subscriptionGroupAlert: SubscriptionGroupAlert
    ): string => {
        if (!subscriptionGroupAlert) {
            return "";
        }

        return subscriptionGroupAlert.name;
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
                title={
                    subscriptionGroupCardData
                        ? subscriptionGroupCardData.name
                        : ""
                }
            >
                {subscriptionGroupCardData && (
                    <Grid container>
                        {/* Subscription Group */}
                        <Grid item md={12}>
                            <SubscriptionGroupCard
                                hideViewDetailsLinks
                                subscriptionGroup={subscriptionGroupCardData}
                                onDelete={onDeleteSubscriptionGroup}
                            />
                        </Grid>

                        {/* Spacer */}
                        <Grid item md={12} />

                        {/* Subscribed alerts */}
                        <Grid item md={12}>
                            <Typography variant="h6">
                                {t("label.alerts")}
                            </Typography>
                        </Grid>

                        {/* Alerts transfer list */}
                        <Grid item md={12}>
                            <TransferList<SubscriptionGroupAlert>
                                fromLabel={t("label.all-alerts")}
                                fromList={getSubscriptionGroupAlerts(alerts)}
                                listItemKeyFn={getSubscriptionGroupAlertId}
                                listItemTextFn={getSubscriptionGroupAlertName}
                                toLabel={t("label.subscribed-alerts")}
                                toList={subscriptionGroupCardData.alerts}
                                onChange={onSubscriptionGroupAlertsChange}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available message */}
                {!subscriptionGroupCardData && <NoDataIndicator />}
            </PageContents>
        </PageContainer>
    );
};
