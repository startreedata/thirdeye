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
import { Box, Button, Grid, Link, Typography } from "@material-ui/core";
import TableCell from "@material-ui/core/TableCell";
import TableRow from "@material-ui/core/TableRow";
import { useQuery } from "@tanstack/react-query";
import { capitalize, sortBy } from "lodash";
import { DateTime } from "luxon";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { SkeletonV1 } from "../../../platform/components";
import { getAnomalies } from "../../../rest/anomalies/anomalies.rest";
import { getAnomaliesAllPath } from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    TitleCardTable,
    TitleCardTableHead,
} from "../title-card-table/title-card-table.component";
import { AnomalyRowV2 } from "./anomaly-row-v2/anomaly-row-v2.component";

export const RecentAnomaliesV2: FunctionComponent = () => {
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
            <Grid container direction="row">
                <Grid item>
                    <Typography style={{ marginBottom: 8 }} variant="h5">
                        {t("label.recent-entity", {
                            entity: t("label.anomalies"),
                        })}
                    </Typography>
                </Grid>
                <Grid item>
                    <Link component={RouterLink} to={getAnomaliesAllPath()}>
                        <Button color="primary" size="large" variant="outlined">
                            {t("label.view-all-entities", {
                                entity: t("label.anomalies"),
                            })}
                        </Button>
                    </Link>
                </Grid>
            </Grid>
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
                                {capitalize(t("message.no-recent-anomalies"))}
                            </NoDataIndicator>
                        </Box>
                    }
                    isEmpty={anomaliesToDisplay.length === 0}
                >
                    <TitleCardTable>
                        <TitleCardTableHead>
                            <TableRow>
                                <TableCell>{t("label.anomaly-id")}</TableCell>
                                <TableCell>{t("label.alert-name")}</TableCell>
                                <TableCell>{t("label.metric")}</TableCell>
                                <TableCell>{t("label.started")}</TableCell>
                                <TableCell>{t("label.ended")}</TableCell>
                                <TableCell>{t("label.deviation")}</TableCell>
                                <TableCell>{t("label.deviation")}</TableCell>
                                <TableCell />
                            </TableRow>
                        </TitleCardTableHead>
                        {anomaliesToDisplay.map((anomaly) => {
                            return (
                                <AnomalyRowV2
                                    anomaly={anomaly}
                                    key={anomaly.id}
                                />
                            );
                        })}
                    </TitleCardTable>
                </EmptyStateSwitch>
            </LoadingErrorStateSwitch>
        </>
    );
};
