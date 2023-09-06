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
import { Box, Button, Grid, Table, Typography } from "@material-ui/core";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { useQuery } from "@tanstack/react-query";
import { capitalize, sortBy } from "lodash";
import { DateTime } from "luxon";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import {
    getAnomalies,
    getAnomaliesCount,
} from "../../../rest/anomalies/anomalies.rest";
import { getAnomaliesAllPath } from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { Pluralize } from "../../pluralize/pluralize.component";
import { AnomalyRow } from "./anomaly-row/anomaly-row.component";

export const RecentAnomalies: FunctionComponent = () => {
    const { t } = useTranslation();

    const startTime = DateTime.local()
        .minus({ month: 1 })
        .startOf("day")
        .toMillis();
    const endTime = DateTime.local().endOf("hour").toMillis();

    const getAnomaliesQuery = useQuery({
        queryKey: ["anomalies", startTime, endTime],
        queryFn: () => {
            return getAnomalies({
                startTime,
                endTime,
            });
        },
        refetchOnWindowFocus: false,
    });

    const getAnomaliesCountQuery = useQuery({
        queryKey: ["anomaliesCount"],
        queryFn: () => {
            return getAnomaliesCount();
        },
        refetchOnWindowFocus: false,
    });

    const anomaliesToDisplay = useMemo(() => {
        if (!getAnomaliesQuery.data) {
            return [];
        }

        const sortedAnomalies = sortBy(
            getAnomaliesQuery.data,
            "startTime"
        ).reverse();

        return sortedAnomalies.slice(0, 10);
    }, [getAnomaliesQuery.data]);

    return (
        <>
            <Box paddingTop={2} />
            <Grid container alignItems="center" justifyContent="space-between">
                <Grid item>
                    <Typography variant="h5">
                        <Pluralize
                            count={anomaliesToDisplay.length}
                            plural={t("label.latest-entity", {
                                entity: t("label.anomalies"),
                            })}
                            singular={t("label.latest-entity", {
                                entity: t("label.anomaly"),
                            })}
                        />
                    </Typography>
                    <Typography variant="body1">
                        <LoadingErrorStateSwitch
                            isError={false}
                            isLoading={getAnomaliesQuery.isLoading}
                            loadingState={<SkeletonV1 animation="pulse" />}
                        >
                            {capitalize(
                                `${getAnomaliesCountQuery.data?.count} ${t(
                                    "message.total-entity-from-start",
                                    {
                                        entity: t("label.anomalies"),
                                    }
                                )}`
                            )}
                        </LoadingErrorStateSwitch>
                    </Typography>
                </Grid>
                <Grid item>
                    <Button
                        color="primary"
                        component={RouterLink}
                        to={getAnomaliesAllPath()}
                        variant="contained"
                    >
                        {t("label.view-all-entities", {
                            entity: capitalize(t("label.anomalies")),
                        })}
                    </Button>
                </Grid>
            </Grid>
            <Box paddingTop={2} />
            <PageContentsCardV1>
                <LoadingErrorStateSwitch
                    errorState={
                        <Box
                            alignItems="center"
                            display="flex"
                            height="100%"
                            justifyContent="center"
                        >
                            <Box>
                                <NoDataIndicator
                                    text={t(
                                        "message.experienced-issues-fetching-data"
                                    )}
                                />
                            </Box>
                        </Box>
                    }
                    isError={getAnomaliesQuery.isError}
                    isLoading={getAnomaliesQuery.isLoading}
                    loadingState={
                        <>
                            <SkeletonV1 animation="pulse" />
                            <SkeletonV1 animation="pulse" />
                            <SkeletonV1 animation="pulse" />
                            <SkeletonV1 animation="pulse" />
                            <SkeletonV1 animation="pulse" />
                        </>
                    }
                >
                    <EmptyStateSwitch
                        emptyState={
                            <Box
                                alignItems="center"
                                justifyContent="center"
                                pb={8}
                                pt={8}
                                textAlign="center"
                                width="100%"
                            >
                                <NoDataIndicator>
                                    {capitalize(
                                        t("message.no-recent-anomalies")
                                    )}
                                </NoDataIndicator>
                            </Box>
                        }
                        isEmpty={anomaliesToDisplay.length === 0}
                    >
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell>
                                        {t("label.anomaly-id")}
                                    </TableCell>
                                    <TableCell>
                                        {t("label.alert-name")}
                                    </TableCell>
                                    <TableCell>{t("label.metric")}</TableCell>
                                    <TableCell>{t("label.started")}</TableCell>
                                    <TableCell>{t("label.ended")}</TableCell>
                                    <TableCell>
                                        {t("label.deviation")}
                                    </TableCell>
                                    <TableCell />
                                    <TableCell />
                                </TableRow>
                            </TableHead>
                            {anomaliesToDisplay.map((anomaly) => {
                                return (
                                    <AnomalyRow
                                        anomaly={anomaly}
                                        key={anomaly.id}
                                    />
                                );
                            })}
                        </Table>
                    </EmptyStateSwitch>
                </LoadingErrorStateSwitch>
            </PageContentsCardV1>
        </>
    );
};
