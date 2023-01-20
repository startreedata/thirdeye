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
import { Box, Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Outlet, useSearchParams } from "react-router-dom";
import { AnomaliesPageHeader } from "../../components/anomalies-page-header/anomalies-page-header.component";
import { AnomalyFilterQueryStringKey } from "../../components/anomaly-quick-filters/anomaly-quick-filter.interface";
import { AnomalyQuickFilters } from "../../components/anomaly-quick-filters/anomaly-quick-filters.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../components/page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { useGetAnomalies } from "../../rest/anomalies/anomaly.actions";
import { GetAnomaliesProps } from "../../rest/anomalies/anomaly.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    makeDeleteRequest,
    promptDeleteConfirmation,
} from "../../utils/bulk-delete/bulk-delete.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";

export const AnomaliesAllPage: FunctionComponent = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    // Use state so we can remove anomalies locally without having to fetch again
    const [anomalies, setAnomalies] = useState<Anomaly[]>([]);
    const {
        anomalies: anomaliesRequestDataResponse,
        getAnomalies,
        status: getAnomaliesRequestStatus,
        errorMessages: anomaliesRequestErrors,
    } = useGetAnomalies();
    const { showDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );
    const anomalyFilters = useMemo(() => {
        const params: GetAnomaliesProps = {};
        if (searchParams.has(AnomalyFilterQueryStringKey.ALERT)) {
            params.alertId = parseInt(
                searchParams.get(AnomalyFilterQueryStringKey.ALERT) || ""
            );
        }

        if (searchParams.has(AnomalyFilterQueryStringKey.DATASET)) {
            params.dataset = searchParams.get(
                AnomalyFilterQueryStringKey.DATASET
            ) as string;
        }

        if (searchParams.has(AnomalyFilterQueryStringKey.METRIC)) {
            params.metric = searchParams.get(
                AnomalyFilterQueryStringKey.METRIC
            ) as string;
        }

        return params;
    }, [searchParams]);

    useEffect(() => {
        // isMounted ensures that the state updates are not run if the component has been unmounted
        let isMounted = true;

        // Time range refreshed, fetch anomalies
        setAnomalies([]);

        const params: GetAnomaliesProps = {
            startTime: Number(startTime),
            endTime: Number(endTime),
            ...anomalyFilters,
        };

        getAnomalies(params).then((anomalies) => {
            if (isMounted) {
                if (anomalies && anomalies.length > 0) {
                    setAnomalies(anomalies);
                } else {
                    setAnomalies([]);
                }
            }
        });

        return () => {
            isMounted = false;
        };
    }, [startTime, endTime, anomalyFilters]);

    const handleAnomalyDelete = (uiAnomaliesToDelete: UiAnomaly[]): void => {
        promptDeleteConfirmation(
            uiAnomaliesToDelete,
            () => {
                anomalies &&
                    makeDeleteRequest(
                        uiAnomaliesToDelete,
                        deleteAnomaly,
                        t,
                        notify,
                        t("label.anomaly"),
                        t("label.anomalies")
                    ).then((deletedAnomalies) => {
                        setAnomalies(() => {
                            return [...anomalies].filter((candidate) => {
                                return (
                                    deletedAnomalies.findIndex(
                                        (d) => d.id === candidate.id
                                    ) === -1
                                );
                            });
                        });
                    });
            },
            t,
            showDialog,
            t("label.anomalies")
        );
    };

    const handleResetFiltersClick = (): void => {
        searchParams.delete(AnomalyFilterQueryStringKey.ALERT);
        searchParams.delete(AnomalyFilterQueryStringKey.DATASET);
        searchParams.delete(AnomalyFilterQueryStringKey.METRIC);
        setSearchParams(searchParams, { replace: true });
    };

    useEffect(() => {
        notifyIfErrors(
            getAnomaliesRequestStatus,
            anomaliesRequestErrors,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.anomalies"),
            })
        );
    }, [getAnomaliesRequestStatus, anomaliesRequestErrors]);

    return (
        <PageV1>
            <AnomaliesPageHeader />

            <PageContentsGridV1 fullHeight>
                <LoadingErrorStateSwitch
                    wrapInCard
                    wrapInGrid
                    isError={getAnomaliesRequestStatus === ActionStatus.Error}
                    isLoading={
                        getAnomaliesRequestStatus === ActionStatus.Working ||
                        getAnomaliesRequestStatus === ActionStatus.Initial
                    }
                >
                    <EmptyStateSwitch
                        emptyState={
                            <Grid item xs={12}>
                                <PageContentsCardV1>
                                    <AnomalyQuickFilters />
                                    <Box pb={20} pt={20}>
                                        <NoDataIndicator>
                                            <Box>
                                                {t(
                                                    "message.no-data-for-entity-for-date-range",
                                                    {
                                                        entity: t(
                                                            "label.anomalies"
                                                        ),
                                                    }
                                                )}
                                            </Box>
                                            {Object.keys(anomalyFilters)
                                                .length > 0 && (
                                                <>
                                                    <Box
                                                        marginTop={3}
                                                        textAlign="center"
                                                    >
                                                        or
                                                    </Box>
                                                    <Box
                                                        marginTop={3}
                                                        textAlign="center"
                                                    >
                                                        <Button
                                                            onClick={
                                                                handleResetFiltersClick
                                                            }
                                                        >
                                                            {t(
                                                                "label.clear-filters"
                                                            )}
                                                        </Button>
                                                    </Box>
                                                </>
                                            )}
                                        </NoDataIndicator>
                                    </Box>
                                </PageContentsCardV1>
                            </Grid>
                        }
                        isEmpty={anomaliesRequestDataResponse?.length === 0}
                    >
                        <Outlet
                            context={{
                                anomalies,
                                handleAnomalyDelete,
                            }}
                        />
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsGridV1>
        </PageV1>
    );
};
