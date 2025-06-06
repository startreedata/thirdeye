/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Icon } from "@iconify/react";
import {
    Box,
    Button,
    Card,
    CardContent,
    Grid,
    ThemeProvider,
    Typography,
} from "@material-ui/core";
import { Functions, Schedule, Storage, TrendingUp } from "@material-ui/icons";
import { AxiosError } from "axios";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams, useSearchParams } from "react-router-dom";
import { AlertAccuracyColored } from "../../components/alert-accuracy-colored/alert-accuracy-colored.component";
import { AlertChartV2 } from "../../components/alert-view/alert-chart-v2/alert-chart-v2.component";
import AlertDrawer from "../../components/alert-view/alert-drawer/alert-drawer.component";
import { AlertStatus } from "../../components/alert-view/alert-status/alert-status.component";
import { EnumerationItemsTableV2 } from "../../components/alert-view/enumeration-items-table-v2/enumeration-items-table-v2.component";
import { InfoBlock } from "../../components/alert-view/info-block/info-block.component";
import { HelpDrawerV1 } from "../../components/help-drawer-v1/help-drawer-v1.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeButtonWithContext } from "../../components/time-range/time-range-button-with-context-v2/time-range-button.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    DataGridSortOrderV1,
    NotificationTypeV1,
    NotificationV1,
    PageContentsGridV1,
    PageV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import {
    useGetAlertInsight,
    useResetAlert,
} from "../../rest/alerts/alerts.actions";
import {
    getAlert,
    getAlertStats,
    updateAlert,
} from "../../rest/alerts/alerts.rest";
import { getAnomalies } from "../../rest/anomalies/anomalies.rest";
import { Alert } from "../../rest/dto/alert.interfaces";
import { getEnumerationItems } from "../../rest/enumeration-items/enumeration-items.rest";
import { useFetchQuery } from "../../rest/hooks/useFetchQuery";
import {
    determineTimezoneFromAlertInEvaluation,
    getGranularityLabelFromValue,
} from "../../utils/alerts/alerts.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { QUERY_PARAM_KEY_FOR_EXPANDED } from "../../utils/params/params.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getAlertsAllPath } from "../../utils/routes/routes.util";
import {
    createAlertPageTheme,
    easyAlertStyles,
} from "../alerts-create-page/alerts-create-easy-page/alerts-create-easy-page.styles";
import { useAlertsViewPageV2Styles } from "./alerts-view-page-v2.styles";
import {
    CONCAT_SEPARATOR,
    QUERY_PARAM_KEY_ANOMALIES_RETRY,
    QUERY_PARAM_KEY_FOR_SEARCH,
    QUERY_PARAM_KEY_FOR_SORT,
    QUERY_PARAM_KEY_FOR_SORT_KEY,
} from "./alerts-view-page-v2.utils";
import { AlertsViewPageParams } from "./alerts-view-page.interfaces-v2";

export const AlertsViewPageV2: FunctionComponent = () => {
    const { t } = useTranslation();
    const { notify, remove: removeNotification } = useNotificationProviderV1();
    const { id: alertId } = useParams<AlertsViewPageParams>();
    const styles = useAlertsViewPageV2Styles();
    const classes = easyAlertStyles();

    // Used for the scenario when user first creates an alert but no anomalies generated yet
    const [refreshAttempts, setRefreshAttempts] = useState(0);
    const [autoRefreshNotification, setAutoRefreshNotification] =
        useState<NotificationV1 | null>(null);
    const [resetStatusNotification, setResetStatusNotification] =
        useState<NotificationV1 | null>(null);
    const { alertInsight, getAlertInsight } = useGetAlertInsight();

    const [searchParams, setSearchParams] = useSearchParams();

    const [expanded, setExpanded] = useState<string[]>(
        searchParams.has(QUERY_PARAM_KEY_FOR_EXPANDED)
            ? (searchParams.get(QUERY_PARAM_KEY_FOR_EXPANDED) as string).split(
                  CONCAT_SEPARATOR
              )
            : []
    );
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );

    const {
        alert: alertThatWasReset,
        status: resetAlertRequestStatus,
        errorMessages: resetAlertRequestErrors,
    } = useResetAlert();

    const getAlertQuery = useFetchQuery({
        queryKey: ["alert", alertId],
        queryFn: () => {
            return getAlert(Number(alertId));
        },
    });

    const getEnumerationItemsQuery = useFetchQuery({
        queryKey: ["enumerationItems", alertId],
        queryFn: () => {
            return getEnumerationItems({ alertId: Number(alertId) });
        },
    });

    const getAnomaliesQuery = useFetchQuery({
        queryKey: ["anomalies", alertId, startTime, endTime],
        queryFn: () => {
            return getAnomalies({
                alertId,
                startTime,
                endTime,
            });
        },
    });

    const [searchTerm, sortOrder, sortKey] = useMemo(
        () => [
            searchParams.get(QUERY_PARAM_KEY_FOR_SEARCH),
            (searchParams.get(
                QUERY_PARAM_KEY_FOR_SORT
            ) as DataGridSortOrderV1) || DataGridSortOrderV1.ASC,
            searchParams.get(QUERY_PARAM_KEY_FOR_SORT_KEY) ??
                "enumerationItem.name",
        ],
        [searchParams]
    );

    useEffect(() => {
        getAlertInsight({ alertId: Number(alertId) });
    }, []);

    useEffect(() => {
        if (
            !alertInsight?.analysisRunInfo?.success &&
            alertInsight?.analysisRunInfo?.message
        ) {
            notifyIfErrors(
                ActionStatus.Error,
                [{ message: alertInsight.analysisRunInfo.message }],
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.alert-insight"),
                })
            );
        }
    }, [alertInsight?.analysisRunInfo]);

    const fetchStats = (): void => {
        getAlertStats({
            alertId: Number(alertId),
            startTime,
            endTime,
        });
    };

    useEffect(() => {
        if (Number(alertId)) {
            fetchStats();
        }
    }, [alertId, startTime, endTime]);

    // Handle communicating status to the user
    useEffect(() => {
        if (
            resetAlertRequestStatus === ActionStatus.Done &&
            alertThatWasReset
        ) {
            setResetStatusNotification(
                notify(
                    NotificationTypeV1.Success,
                    t("message.alert-reset-success-will-reload", {
                        alertName: alertThatWasReset.name,
                    })
                )
            );
        }
        notifyIfErrors(
            resetAlertRequestStatus,
            resetAlertRequestErrors,
            notify,
            t("message.alert-reset-error")
        );
    }, [resetAlertRequestStatus]);

    useEffect(() => {
        getAnomaliesQuery.isError &&
            notifyIfErrors(
                ActionStatus.Error,
                getErrorMessages(getAnomaliesQuery.error as AxiosError),
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.anomalies"),
                })
            );
    }, [getAnomaliesQuery.isError]);

    /**
     * If anomalies list is empty and QUERY_PARAM_KEY_ANOMALIES_RETRY flag exists
     */
    useEffect(() => {
        if (
            getAnomaliesQuery.data &&
            getAnomaliesQuery.data.length === 0 &&
            searchParams.has(QUERY_PARAM_KEY_ANOMALIES_RETRY)
        ) {
            // If not in reset flow, assume alert was just created
            resetStatusNotification === null &&
                autoRefreshNotification === null &&
                setAutoRefreshNotification(
                    notify(
                        NotificationTypeV1.Info,
                        t(
                            "message.looks-like-this-alert-was-just-created-this-page-a"
                        )
                    )
                );
        }
    }, [getAnomaliesQuery.data]);
    /**
     * Automatic retry loading anomalies flow
     */
    useEffect(() => {
        if (
            getAnomaliesQuery.data &&
            getAnomaliesQuery.data.length > 0 &&
            searchParams.has(QUERY_PARAM_KEY_ANOMALIES_RETRY)
        ) {
            searchParams.delete(QUERY_PARAM_KEY_ANOMALIES_RETRY);
            setSearchParams(searchParams, { replace: true });

            if (resetStatusNotification) {
                removeNotification(resetStatusNotification);
                setResetStatusNotification(null);
                alertThatWasReset &&
                    notify(
                        NotificationTypeV1.Success,
                        t("message.alert-reset-successful", {
                            alertName: alertThatWasReset.name,
                        })
                    );
            }
        }

        if (
            (getAnomaliesQuery.data && getAnomaliesQuery.data.length > 0) ||
            getAnomaliesQuery.isLoading
        ) {
            if (autoRefreshNotification) {
                removeNotification(autoRefreshNotification);
                setAutoRefreshNotification(null);
            }

            return;
        }

        if (!!alert && searchParams.has(QUERY_PARAM_KEY_ANOMALIES_RETRY)) {
            if (refreshAttempts < 3) {
                setTimeout(() => {
                    setRefreshAttempts(refreshAttempts + 1);
                    getAnomaliesQuery.refetch().then((newAnomalies) => {
                        if (newAnomalies.data && newAnomalies.data.length > 0) {
                            fetchStats();
                        }
                    });
                }, 5000);
            } else {
                if (autoRefreshNotification) {
                    removeNotification(autoRefreshNotification);
                    setAutoRefreshNotification(null);
                }

                if (resetStatusNotification) {
                    removeNotification(resetStatusNotification);
                    setResetStatusNotification(null);
                }

                if (searchParams.has(QUERY_PARAM_KEY_ANOMALIES_RETRY)) {
                    searchParams.delete(QUERY_PARAM_KEY_ANOMALIES_RETRY);
                    setSearchParams(searchParams, { replace: true });
                }

                notify(
                    NotificationTypeV1.Warning,
                    t("message.no-data-for-entity-for-date-range", {
                        entity: t("label.anomalies"),
                    })
                );
            }
        }
    }, [getAnomaliesQuery.data]);

    const handleExpandedChange = (newExpanded: string[]): void => {
        if (newExpanded.length > 0) {
            searchParams.set(
                QUERY_PARAM_KEY_FOR_EXPANDED,
                newExpanded.join(CONCAT_SEPARATOR)
            );
        } else {
            searchParams.delete(QUERY_PARAM_KEY_FOR_EXPANDED);
        }
        setSearchParams(searchParams, { replace: true });
        setExpanded(newExpanded);
    };

    const handleAlertChange = (updatedAlert: Alert): void => {
        if (!updatedAlert) {
            return;
        }

        updateAlert(updatedAlert).then(
            () => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.update-success", { entity: t("label.alert") })
                );

                getAlertQuery.refetch();
            },
            (error) => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.update-error", {
                        entity: t("label.alert"),
                    })
                );
            }
        );
    };

    const handleSearchTermChange = (newTerm: string): void => {
        if (newTerm) {
            searchParams.set(QUERY_PARAM_KEY_FOR_SEARCH, newTerm);
        } else {
            searchParams.delete(QUERY_PARAM_KEY_FOR_SEARCH);
        }
        setSearchParams(searchParams, { replace: true });
    };

    const handleSortKeyChange = (newKey: string): void => {
        if (newKey) {
            searchParams.set(QUERY_PARAM_KEY_FOR_SORT_KEY, newKey);
        } else {
            searchParams.delete(QUERY_PARAM_KEY_FOR_SEARCH);
        }
        setSearchParams(searchParams, { replace: true });
    };

    const handleSortOrderChange = (newOrder: DataGridSortOrderV1): void => {
        if (newOrder) {
            searchParams.set(QUERY_PARAM_KEY_FOR_SORT, newOrder);
        } else {
            searchParams.delete(QUERY_PARAM_KEY_FOR_SORT);
        }
        setSearchParams(searchParams, { replace: true });
    };

    const handleAnomalyDetectionRerun = (): void => {
        // fetchData();
    };

    const isDxAlert =
        getAlertQuery.data?.templateProperties?.enumerationItems ||
        getAlertQuery?.data?.templateProperties.enumeratorQuery;

    return (
        <ThemeProvider theme={createAlertPageTheme}>
            <PageV1 className={styles.page}>
                <PageHeader
                    transparentBackground
                    breadcrumbs={[
                        {
                            label: t("label.alerts"),
                            link: getAlertsAllPath(),
                        },
                        {
                            label: getAlertQuery?.data?.name || "",
                        },
                    ]}
                    customActions={
                        <HelpDrawerV1
                            title={`${t("label.need-help")}?`}
                            trigger={(handleOpen) => (
                                <Button
                                    className={classes.infoButton}
                                    color="primary"
                                    size="small"
                                    variant="outlined"
                                    onClick={handleOpen}
                                >
                                    <Box component="span" mr={1}>
                                        {t("label.need-help")}
                                    </Box>
                                    <Box component="span" display="flex">
                                        <Icon
                                            fontSize={24}
                                            icon="mdi:question-mark-circle-outline"
                                        />
                                    </Box>
                                </Button>
                            )}
                        />
                    }
                    subtitle={
                        <>
                            {getAlertQuery.data?.description && (
                                <Box paddingBottom={1}>
                                    <Typography
                                        color="textSecondary"
                                        variant="subtitle2"
                                    >
                                        {getAlertQuery.data?.description}
                                    </Typography>
                                </Box>
                            )}
                            <Grid container alignItems="center" direction="row">
                                <Grid item>
                                    <AlertStatus
                                        alert={getAlertQuery.data as Alert}
                                    />
                                </Grid>
                                <Grid item>
                                    <AlertAccuracyColored
                                        alertId={Number(alertId) as number}
                                        end={endTime}
                                        label={t("label.overall-entity", {
                                            entity: t("label.accuracy"),
                                        })}
                                        start={startTime}
                                        typographyProps={{
                                            color: "textPrimary",
                                        }}
                                    />
                                </Grid>
                            </Grid>
                        </>
                    }
                    title={getAlertQuery.data?.name || ""}
                >
                    {getAlertQuery.isLoading && <SkeletonV1 width="512px" />}
                </PageHeader>

                <PageContentsGridV1>
                    <Grid item xs={2}>
                        <AlertDrawer
                            alert={getAlertQuery.data as Alert}
                            onChange={handleAlertChange}
                            onDetectionRerunSuccess={
                                handleAnomalyDetectionRerun
                            }
                        />
                    </Grid>
                    <Grid item xs={10}>
                        <Card className={styles.detailsCard}>
                            <Grid container spacing={2}>
                                <Grid item xs={3}>
                                    <InfoBlock
                                        icon={<Storage />}
                                        title={t("label.dataset-name")}
                                        value={
                                            getAlertQuery.data
                                                ?.templateProperties?.dataset ||
                                            "-"
                                        }
                                    />
                                </Grid>
                                <Grid item xs={3}>
                                    <InfoBlock
                                        icon={<Functions />}
                                        title={t(
                                            "label.aggregation-function-of-metric"
                                        )}
                                        value={
                                            getAlertQuery.data
                                                ?.templateProperties
                                                ?.aggregationFunction || "-"
                                        }
                                    />
                                </Grid>
                                <Grid item xs={3}>
                                    <InfoBlock
                                        icon={<TrendingUp />}
                                        title={t("label.detection-algorithm")}
                                        value={
                                            getAlertQuery.data?.template
                                                ?.name || "-"
                                        }
                                    />
                                </Grid>
                                <Grid item xs={3}>
                                    <InfoBlock
                                        icon={<Schedule />}
                                        title={t("label.granularity")}
                                        value={
                                            getGranularityLabelFromValue(
                                                (getAlertQuery.data
                                                    ?.templateProperties
                                                    ?.monitoringGranularity as string) ||
                                                    ""
                                            ) || "-"
                                        }
                                    />
                                </Grid>
                            </Grid>
                        </Card>
                        <Grid
                            container
                            alignItems="center"
                            className={styles.metricHeaderContainer}
                            direction="row"
                            justifyContent="space-between"
                        >
                            <Grid item>
                                <Typography variant="h6">
                                    {isDxAlert
                                        ? `${
                                              getEnumerationItemsQuery.data
                                                  ?.length
                                          } ${t("label.dimensions")}`
                                        : t("label.single-metric")}
                                </Typography>
                            </Grid>
                            <Grid item>
                                <Box alignItems="center" display="flex">
                                    <Typography
                                        className={styles.timeRangeLabel}
                                    >
                                        {t("label.date-range")}:
                                    </Typography>
                                    <TimeRangeButtonWithContext
                                        hideQuickExtend
                                        btnGroupColor="primary"
                                        maxDate={alertInsight?.datasetEndTime}
                                        minDate={alertInsight?.datasetStartTime}
                                        timezone={determineTimezoneFromAlertInEvaluation(
                                            alertInsight?.templateWithProperties
                                        )}
                                    />
                                </Box>
                            </Grid>
                        </Grid>

                        <LoadingErrorStateSwitch
                            errorState={
                                <Card variant="outlined">
                                    <CardContent>
                                        <Box pb={20} pt={20}>
                                            <NoDataIndicator>
                                                <Box p={1}>
                                                    <Typography>
                                                        Experienced error while
                                                        loading alert. Try
                                                        reloading the alert
                                                        data.
                                                    </Typography>
                                                    <Box pt={1}>
                                                        <Button
                                                            color="primary"
                                                            onClick={() =>
                                                                getAlertQuery.refetch()
                                                            }
                                                        >
                                                            {t("label.reload")}
                                                        </Button>
                                                    </Box>
                                                </Box>
                                            </NoDataIndicator>
                                        </Box>
                                    </CardContent>
                                </Card>
                            }
                            isError={getAlertQuery.isError}
                            isLoading={
                                getAnomaliesQuery.isLoading ||
                                getEnumerationItemsQuery.isLoading ||
                                getAlertQuery.isLoading
                            }
                            loadingState={
                                <Card variant="outlined">
                                    <CardContent>
                                        <SkeletonV1 />
                                        <SkeletonV1 />
                                        <SkeletonV1 />
                                        <SkeletonV1 />
                                        <SkeletonV1 />
                                        <SkeletonV1 />
                                    </CardContent>
                                </Card>
                            }
                        >
                            {isDxAlert ? (
                                <EnumerationItemsTableV2
                                    alertId={Number(alertId)}
                                    anomalies={getAnomaliesQuery.data || []}
                                    endTime={endTime}
                                    enumerationsItems={
                                        getEnumerationItemsQuery.data || []
                                    }
                                    expanded={expanded}
                                    initialSearchTerm={searchTerm || ""}
                                    sortKey={sortKey}
                                    sortOrder={sortOrder}
                                    startTime={startTime}
                                    onExpandedChange={handleExpandedChange}
                                    onSearchTermChange={handleSearchTermChange}
                                    onSortKeyChange={handleSortKeyChange}
                                    onSortOrderChange={handleSortOrderChange}
                                />
                            ) : (
                                <AlertChartV2
                                    alertId={Number(alertId)}
                                    anomalies={getAnomaliesQuery.data || []}
                                    endTime={endTime}
                                    startTime={startTime}
                                />
                            )}
                        </LoadingErrorStateSwitch>
                    </Grid>
                </PageContentsGridV1>
            </PageV1>
        </ThemeProvider>
    );
};
