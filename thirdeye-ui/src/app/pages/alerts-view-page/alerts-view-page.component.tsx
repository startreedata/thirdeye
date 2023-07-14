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
import {
    Box,
    Button,
    ButtonGroup,
    Card,
    CardContent,
    Grid,
    Typography,
} from "@material-ui/core";
import KeyboardArrowDownIcon from "@material-ui/icons/KeyboardArrowDown";
import React, {
    FunctionComponent,
    MouseEvent,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useParams, useSearchParams } from "react-router-dom";
import { AlertAccuracyColored } from "../../components/alert-accuracy-colored/alert-accuracy-colored.component";
import { AlertOptionsButton } from "../../components/alert-view/alert-options-button/alert-options-button.component";
import { EnumerationItemMerger } from "../../components/alert-view/enumeration-item-merger/enumeration-item-merger.component";
import { DetectionEvaluationForRender } from "../../components/alert-view/enumeration-item-merger/enumeration-item-merger.interfaces";
import { EnumerationItemsTable } from "../../components/alert-view/enumeration-items-table/enumeration-items-table.component";
import { AlertViewSubHeader } from "../../components/alert-view/sub-header/alert-sub-header.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
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
    useGetAlert,
    useGetEvaluation,
    useResetAlert,
} from "../../rest/alerts/alerts.actions";
import { getAlertStats, updateAlert } from "../../rest/alerts/alerts.rest";
import { useGetAnomalies } from "../../rest/anomalies/anomaly.actions";
import { Alert } from "../../rest/dto/alert.interfaces";
import {
    createAlertEvaluation,
    determineTimezoneFromAlertInEvaluation,
    extractDetectionEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../utils/alerts/alerts.util";
import { generateNameForDetectionResult } from "../../utils/enumeration-items/enumeration-items.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { QUERY_PARAM_KEY_FOR_EXPANDED } from "../../utils/params/params.util";
import { getAlertsAllPath } from "../../utils/routes/routes.util";
import { AlertsViewPageParams } from "./alerts-view-page.interfaces";
import {
    CONCAT_SEPARATOR,
    QUERY_PARAM_KEY_ANOMALIES_RETRY,
    QUERY_PARAM_KEY_FOR_SEARCH,
    QUERY_PARAM_KEY_FOR_SORT,
    QUERY_PARAM_KEY_FOR_SORT_KEY,
} from "./alerts-view-page.utils";

export const AlertsViewPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const { notify, remove: removeNotification } = useNotificationProviderV1();
    const { id: alertId } = useParams<AlertsViewPageParams>();

    // Used for the scenario when user first creates an alert but no anomalies generated yet
    const [refreshAttempts, setRefreshAttempts] = useState(0);
    const [autoRefreshNotification, setAutoRefreshNotification] =
        useState<NotificationV1 | null>(null);
    const [resetStatusNotification, setResetStatusNotification] =
        useState<NotificationV1 | null>(null);

    const {
        alert: alertThatWasReset,
        resetAlert,
        status: resetAlertRequestStatus,
        errorMessages: resetAlertRequestErrors,
    } = useResetAlert();
    const {
        alert,
        getAlert,
        errorMessages: getAlertErrorMessages,
        status: getAlertStatus,
    } = useGetAlert();
    const {
        evaluation,
        getEvaluation,
        errorMessages: getEvaluationErrorMessages,
        status: evaluationRequestStatus,
    } = useGetEvaluation();
    const {
        anomalies,
        getAnomalies,
        errorMessages: getAnomaliesErrorsMessages,
        status: anomaliesRequestStatus,
        resetData: resetAnomaliesData,
    } = useGetAnomalies();
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
    const [searchTerm, sortOrder, sortKey] = useMemo(
        () => [
            searchParams.get(QUERY_PARAM_KEY_FOR_SEARCH),
            (searchParams.get(
                QUERY_PARAM_KEY_FOR_SORT
            ) as DataGridSortOrderV1) || DataGridSortOrderV1.DESC,
            searchParams.get(QUERY_PARAM_KEY_FOR_SORT_KEY) ?? "lastAnomalyTs",
        ],
        [searchParams]
    );

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

    useEffect(() => {
        if (!evaluation) {
            return;
        }
        const extracted = extractDetectionEvaluation(evaluation);

        // Automatically expand the only item in the response
        if (extracted.length === 1) {
            const nameForOnlyItem = generateNameForDetectionResult(
                extracted[0]
            );
            searchParams.set(QUERY_PARAM_KEY_FOR_EXPANDED, nameForOnlyItem);
            setSearchParams(searchParams, { replace: true });
            setExpanded([nameForOnlyItem]);
        }
    }, [evaluation]);

    const fetchData = (): void => {
        if (!alert || !startTime || !endTime) {
            return;
        }
        getAnomalies({
            alertId: alert.id,
            startTime,
            endTime,
        });
        getEvaluation(createAlertEvaluation(alert, startTime, endTime));
    };

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
        getAlert(Number(alertId));
    }, [alertId]);

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        fetchData();
    }, [alert, startTime, endTime]);

    useEffect(() => {
        notifyIfErrors(
            getAlertStatus,
            getAlertErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alert"),
            })
        );
    }, [getAlertErrorMessages, getAlertStatus]);

    useEffect(() => {
        notifyIfErrors(
            evaluationRequestStatus,
            getEvaluationErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.chart-data"),
            })
        );
    }, [getEvaluationErrorMessages, evaluationRequestStatus]);

    useEffect(() => {
        notifyIfErrors(
            anomaliesRequestStatus,
            getAnomaliesErrorsMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.anomalies"),
            })
        );
    }, [anomaliesRequestStatus, getAnomaliesErrorsMessages]);

    /**
     * If anomalies list is empty and QUERY_PARAM_KEY_ANOMALIES_RETRY flag exists
     */
    useEffect(() => {
        if (
            anomalies &&
            anomalies.length === 0 &&
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
    }, [alert, anomalies]);
    /**
     * Automatic retry loading anomalies flow
     */
    useEffect(() => {
        if (
            anomalies &&
            anomalies.length > 0 &&
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
            (anomalies && anomalies.length > 0) ||
            anomaliesRequestStatus !== ActionStatus.Working
        ) {
            if (autoRefreshNotification) {
                removeNotification(autoRefreshNotification);
                setAutoRefreshNotification(null);
            }

            return;
        }

        if (alert && searchParams.has(QUERY_PARAM_KEY_ANOMALIES_RETRY)) {
            if (refreshAttempts < 3) {
                setTimeout(() => {
                    setRefreshAttempts(refreshAttempts + 1);
                    getAnomalies({
                        alertId: alert.id,
                        startTime,
                        endTime,
                    }).then((newAnomalies) => {
                        if (newAnomalies && newAnomalies.length > 0) {
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
    }, [anomalies, alert]);

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

        updateAlert(updatedAlert).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", { entity: t("label.alert") })
            );

            // Replace updated alert as fetched alert
            fetchData();
        });
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
        fetchData();
    };

    return (
        <PageV1>
            <PageHeader
                transparentBackground
                breadcrumbs={[
                    {
                        label: t("label.alerts"),
                        link: getAlertsAllPath(),
                    },
                    {
                        label: alert ? alert.name : "",
                    },
                ]}
                customActions={
                    alert ? (
                        <AlertOptionsButton
                            alert={alert}
                            handleAlertResetClick={() => {
                                resetAnomaliesData();
                                resetAlert(alert.id).then((newlyResetAlert) => {
                                    getAnomalies({
                                        alertId: newlyResetAlert?.id,
                                        startTime,
                                        endTime,
                                    });

                                    searchParams.set(
                                        QUERY_PARAM_KEY_ANOMALIES_RETRY,
                                        "true"
                                    );
                                    setSearchParams(searchParams, {
                                        replace: true,
                                    });
                                });
                            }}
                            openButtonRenderer={(
                                clickHandler: (
                                    event: MouseEvent<HTMLElement>
                                ) => void
                            ) => {
                                return (
                                    <ButtonGroup
                                        color="primary"
                                        size="small"
                                        variant="outlined"
                                        onClick={clickHandler}
                                    >
                                        <Button>{t("label.options")}</Button>
                                        <Button>
                                            <KeyboardArrowDownIcon />
                                        </Button>
                                    </ButtonGroup>
                                );
                            }}
                            onChange={handleAlertChange}
                            onDetectionRerunSuccess={
                                handleAnomalyDetectionRerun
                            }
                        />
                    ) : (
                        ""
                    )
                }
                subtitle={
                    <>
                        {alert?.description && (
                            <Box paddingBottom={1}>
                                <Typography
                                    color="textSecondary"
                                    variant="subtitle2"
                                >
                                    {alert?.description}
                                </Typography>
                            </Box>
                        )}
                        <AlertAccuracyColored
                            alertId={Number(alertId) as number}
                            end={endTime}
                            label={t("label.overall-entity", {
                                entity: t("label.accuracy"),
                            })}
                            start={startTime}
                        />
                    </>
                }
                title={alert?.name || ""}
            >
                {getAlertStatus === ActionStatus.Working && (
                    <SkeletonV1 width="512px" />
                )}
            </PageHeader>

            <PageContentsGridV1>
                {/* Alert sub header */}
                <Grid item xs={12}>
                    <LoadingErrorStateSwitch
                        isError={getAlertStatus === ActionStatus.Error}
                        isLoading={
                            getAlertStatus === ActionStatus.Initial ||
                            getAlertStatus === ActionStatus.Working
                        }
                        loadingState={<SkeletonV1 />}
                    >
                        <AlertViewSubHeader
                            alert={alert as Alert}
                            timezone={determineTimezoneFromAlertInEvaluation(
                                evaluation?.alert.template
                            )}
                        />
                    </LoadingErrorStateSwitch>
                </Grid>

                {/* Alert evaluation time series */}
                <Grid item xs={12}>
                    <LoadingErrorStateSwitch
                        errorState={
                            <Card variant="outlined">
                                <CardContent>
                                    <Box pb={20} pt={20}>
                                        <NoDataIndicator>
                                            <Box p={1}>
                                                <Button
                                                    color="primary"
                                                    onClick={fetchData}
                                                >
                                                    {t(
                                                        "label.reload-chart-data"
                                                    )}
                                                </Button>
                                            </Box>
                                        </NoDataIndicator>
                                    </Box>
                                </CardContent>
                            </Card>
                        }
                        isError={
                            evaluationRequestStatus === ActionStatus.Error ||
                            getAlertStatus === ActionStatus.Error
                        }
                        isLoading={
                            evaluationRequestStatus === ActionStatus.Working ||
                            evaluationRequestStatus === ActionStatus.Initial ||
                            getAlertStatus === ActionStatus.Working ||
                            getAlertStatus === ActionStatus.Initial
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
                        {evaluation && (
                            <EnumerationItemMerger
                                anomalies={anomalies || []}
                                detectionEvaluations={extractDetectionEvaluation(
                                    evaluation
                                )}
                            >
                                {(
                                    detectionEvaluations: DetectionEvaluationForRender[]
                                ) => {
                                    return (
                                        <EnumerationItemsTable
                                            alertId={Number(alertId)}
                                            detectionEvaluations={
                                                detectionEvaluations
                                            }
                                            expanded={expanded}
                                            hideTime={shouldHideTimeInDatetimeFormat(
                                                evaluation?.alert?.template
                                            )}
                                            initialSearchTerm={searchTerm || ""}
                                            sortKey={sortKey}
                                            sortOrder={sortOrder}
                                            timezone={determineTimezoneFromAlertInEvaluation(
                                                evaluation?.alert?.template
                                            )}
                                            onExpandedChange={
                                                handleExpandedChange
                                            }
                                            onSearchTermChange={
                                                handleSearchTermChange
                                            }
                                            onSortKeyChange={
                                                handleSortKeyChange
                                            }
                                            onSortOrderChange={
                                                handleSortOrderChange
                                            }
                                        />
                                    );
                                }}
                            </EnumerationItemMerger>
                        )}
                    </LoadingErrorStateSwitch>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
