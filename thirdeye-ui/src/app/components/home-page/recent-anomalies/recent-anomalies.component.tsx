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
import { Box, Button, Grid, Table, Typography } from "@material-ui/core";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { sortBy } from "lodash";
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAnomalies } from "../../../rest/anomalies/anomaly.actions";
import {
    generateDateRangeMonthsFromNow,
    getAnomaliesAllPath,
} from "../../../utils/routes/routes.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AnomalyRow } from "./anomaly-row/anomaly-row.component";

export const RecentAnomalies: FunctionComponent = () => {
    const { t } = useTranslation();
    const { anomalies, getAnomalies, status } = useGetAnomalies();

    useEffect(() => {
        getAnomalies({ startTime: generateDateRangeMonthsFromNow(6)[0] });
    }, []);

    const anomaliesToDisplay = useMemo(() => {
        if (!anomalies) {
            return [];
        }

        const sortedAnomalies = sortBy(anomalies, "startTime").reverse();

        return sortedAnomalies.slice(0, 10);
    }, [anomalies]);

    return (
        <>
            <Box paddingTop={2} />
            <Grid container alignItems="center" justifyContent="space-between">
                <Grid item>
                    <Typography variant="h5">Recent Anomalies</Typography>
                    <Typography variant="body1">
                        <LoadingErrorStateSwitch
                            isError={false}
                            isLoading={status === ActionStatus.Working}
                            loadingState={<SkeletonV1 animation="pulse" />}
                        >
                            {anomaliesToDisplay.length
                                ? `${anomaliesToDisplay.length} latest `
                                : "No recent "}
                            anomalies detected in your alerts
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
                        View all anomalies
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
                    isError={status === ActionStatus.Error}
                    isLoading={status === ActionStatus.Working}
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
                    {anomaliesToDisplay.length > 0 ? (
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell>Anomaly ID</TableCell>
                                    <TableCell>Alert Name</TableCell>
                                    <TableCell>Metric</TableCell>
                                    <TableCell>Started</TableCell>
                                    <TableCell>Ended</TableCell>
                                    <TableCell>Deviation</TableCell>
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
                    ) : (
                        <Box
                            alignItems="center"
                            justifyContent="center"
                            mb={8}
                            mt={8}
                            textAlign="center"
                            width="100%"
                        >
                            <NoDataIndicator>
                                No recent anomalies found in the last 6 months.
                            </NoDataIndicator>
                        </Box>
                    )}
                </LoadingErrorStateSwitch>
            </PageContentsCardV1>
        </>
    );
};
