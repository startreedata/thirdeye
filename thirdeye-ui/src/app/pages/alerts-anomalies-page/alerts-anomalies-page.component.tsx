/*
 * Copyright 2022 StarTree Inc
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
import { Box, FormControlLabel, Grid, Switch } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useParams, useSearchParams } from "react-router-dom";
import { AnomalyListV1 } from "../../components/anomaly-list-v1/anomaly-list-v1.component";
import { AnomalyQuickFilters } from "../../components/anomaly-quick-filters/anomaly-quick-filters.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import {
    useGetAlert,
    useGetAlertInsight,
} from "../../rest/alerts/alerts.actions";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { useGetAnomalies } from "../../rest/anomalies/anomaly.actions";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { useGetEnumerationItem } from "../../rest/enumeration-items/enumeration-items.actions";
import { determineTimezoneFromAlertInEvaluation } from "../../utils/alerts/alerts.util";
import {
    filterAnomaliesByFunctions,
    getUiAnomalies,
    isAnomalyIgnored,
} from "../../utils/anomalies/anomalies.util";
import {
    makeDeleteRequest,
    promptDeleteConfirmation,
} from "../../utils/bulk-delete/bulk-delete.util";
import { generateNameForEnumerationItem } from "../../utils/enumeration-items/enumeration-items.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import {
    getAlertsAlertPath,
    getAlertsAllPath,
} from "../../utils/routes/routes.util";
import {
    AlertsAnomaliesParams,
    ENUMERATION_ITEM_QUERY_PARAM_KEY,
    FILTER_IGNORED_ANOMALIES_QUERY_PARAM_KEY,
} from "./alerts-anomalies-page.interfaces";

export const AlertsAnomaliesPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const { notify } = useNotificationProviderV1();
    const { id: alertId } = useParams<AlertsAnomaliesParams>();
    // Insights is used here to get timezone information
    const { alertInsight, getAlertInsight } = useGetAlertInsight();
    const {
        alert,
        getAlert,
        errorMessages: getAlertErrorMessages,
        status: getAlertStatus,
    } = useGetAlert();
    const {
        anomalies,
        getAnomalies,
        errorMessages: getAnomaliesErrorsMessages,
        status: anomaliesRequestStatus,
    } = useGetAnomalies();
    const {
        enumerationItem,
        getEnumerationItem,
        errorMessages: getEnumerationItemErrorsMessages,
        status: getEnumerationItemStatus,
    } = useGetEnumerationItem();
    const [searchParams, setSearchParams] = useSearchParams();
    const [startTime, endTime, enumerationItemIdStr, filterIgnoredAnomalies] =
        useMemo(
            () => [
                Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
                Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
                searchParams.get(ENUMERATION_ITEM_QUERY_PARAM_KEY),
                searchParams.get(FILTER_IGNORED_ANOMALIES_QUERY_PARAM_KEY) ===
                    "true",
            ],
            [searchParams]
        );

    const setShowIgnored = (newState: boolean): void => {
        searchParams.set(
            FILTER_IGNORED_ANOMALIES_QUERY_PARAM_KEY,
            `${!newState}`
        );
        setSearchParams(searchParams);
    };

    const handleToggleIgnoredAnomalies = (
        event: React.ChangeEvent<HTMLInputElement>
    ): void => {
        setShowIgnored(event.target.checked);
    };

    const fetchData = (): void => {
        if (!alert || !startTime || !endTime) {
            return;
        }
        getAnomalies({
            alertId: alert.id,
            startTime,
            endTime,
            enumerationItemId: Number(enumerationItemIdStr),
            filterIgnoredAnomalies: false,
        });
    };

    const uiAnomalies = useMemo(() => {
        if (!anomalies) {
            return null;
        }

        // All anomalies being fetched, and then filtered here at UI level
        const filteredAnomalies = filterAnomaliesByFunctions(
            anomalies,
            filterIgnoredAnomalies ? [(a) => !isAnomalyIgnored(a)] : []
        );

        return getUiAnomalies(filteredAnomalies);
    }, [anomalies, filterIgnoredAnomalies]);

    useEffect(() => {
        if (enumerationItemIdStr) {
            getEnumerationItem(Number(enumerationItemIdStr));
        }
    }, [enumerationItemIdStr]);

    useEffect(() => {
        getAlert(Number(alertId));
        getAlertInsight({ alertId: Number(alertId) });
    }, [alertId]);

    useEffect(() => {
        // Fetched alert changed, fetch alert evaluation
        fetchData();
    }, [alert, startTime, endTime, enumerationItemIdStr]);

    useEffect(() => {
        notifyIfErrors(
            getEnumerationItemStatus,
            getEnumerationItemErrorsMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.enumeration-item"),
            })
        );
    }, [getEnumerationItemErrorsMessages, getEnumerationItemStatus]);

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
            anomaliesRequestStatus,
            getAnomaliesErrorsMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.anomalies"),
            })
        );
    }, [anomaliesRequestStatus, getAnomaliesErrorsMessages]);

    const handleAnomalyDelete = (uiAnomaliesToDelete: UiAnomaly[]): void => {
        promptDeleteConfirmation(
            uiAnomaliesToDelete,
            () => {
                uiAnomalies &&
                    makeDeleteRequest(
                        uiAnomaliesToDelete,
                        deleteAnomaly,
                        t,
                        notify,
                        t("label.anomaly"),
                        t("label.anomalies")
                    ).then(() => {
                        // Since the uiAnomaly data is a computed value from `anomalies` and will be
                        // re-computed every time the `filterIgnoredAnomalies` is changed, the data
                        // is being re-fetched to avoid having to complicate the local state
                        fetchData();
                    });
            },
            t,
            showDialog,
            t("label.anomalies")
        );
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
                        link: alert ? getAlertsAlertPath(alert.id) : undefined,
                    },
                    {
                        label: t("label.anomalies"),
                    },
                ]}
                subtitle={
                    enumerationItemIdStr && enumerationItem
                        ? generateNameForEnumerationItem(enumerationItem)
                        : undefined
                }
                title={t("label.anomalies")}
            />

            <PageContentsGridV1 fullHeight>
                <Grid item xs={12}>
                    <PageContentsCardV1 disablePadding fullHeight>
                        <AnomalyListV1
                            anomalies={
                                // This prop is set to null every time the API is called again to
                                // trigger a UI loading state, since otherwise the new data just
                                // replaces the old one abruptly
                                anomaliesRequestStatus ===
                                    ActionStatus.Working ||
                                anomaliesRequestStatus === ActionStatus.Initial
                                    ? null
                                    : uiAnomalies
                            }
                            timezone={determineTimezoneFromAlertInEvaluation(
                                alertInsight?.templateWithProperties
                            )}
                            toolbar={
                                <>
                                    <Box width="100%">
                                        <FormControlLabel
                                            control={
                                                <Switch
                                                    checked={
                                                        !filterIgnoredAnomalies
                                                    }
                                                    onChange={
                                                        handleToggleIgnoredAnomalies
                                                    }
                                                />
                                            }
                                            label={t("message.show-ignored")}
                                        />
                                    </Box>
                                    <AnomalyQuickFilters
                                        showTimeSelectorOnly
                                        timezone={determineTimezoneFromAlertInEvaluation(
                                            alertInsight?.templateWithProperties
                                        )}
                                    />
                                </>
                            }
                            onDelete={handleAnomalyDelete}
                        />
                    </PageContentsCardV1>
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
