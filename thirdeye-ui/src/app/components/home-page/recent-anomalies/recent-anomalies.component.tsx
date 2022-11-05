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
                            {anomaliesToDisplay.length} latest anomalies
                            detected in your alerts
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
                        {anomaliesToDisplay.length > 0 &&
                            anomaliesToDisplay.map((anomaly) => {
                                return (
                                    <AnomalyRow
                                        anomaly={anomaly}
                                        key={anomaly.id}
                                    />
                                );
                            })}
                    </Table>
                </LoadingErrorStateSwitch>
            </PageContentsCardV1>
        </>
    );
};
