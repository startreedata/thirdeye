// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { Box, Grid, Table, Typography } from "@material-ui/core";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import { sortBy } from "lodash";
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { TaskStatus, TaskType } from "../../../rest/dto/taks.interface";
import { useGetTasks } from "../../../rest/tasks/tasks.actions";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TaskRow } from "./task-row/task-row.component";

export const RecentFailures: FunctionComponent = () => {
    const { t } = useTranslation();
    const { tasks, getTasks, status } = useGetTasks();

    useEffect(() => {
        getTasks({
            status: [TaskStatus.TIMEOUT, TaskStatus.FAILED],
            type: [TaskType.DETECTION],
        });
    }, []);

    const tasksToDisplay = useMemo(() => {
        if (!tasks) {
            return [];
        }

        const sortedTasks = sortBy(tasks, "startTime").reverse();

        return sortedTasks.slice(0, 10);
    }, [tasks]);

    return (
        <>
            <Grid container alignItems="center" justifyContent="space-between">
                <Grid item>
                    <Typography variant="h5">
                        {t("label.recent-anomaly-detection-failures")}
                    </Typography>
                    <Typography variant="body1">
                        <LoadingErrorStateSwitch
                            isError={false}
                            isLoading={status === ActionStatus.Working}
                            loadingState={<SkeletonV1 animation="pulse" />}
                        >
                            {t("label.latest-errors-in-your-alerts")}
                        </LoadingErrorStateSwitch>
                    </Typography>
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
                    <Table>
                        <TableHead>
                            <TableRow>
                                <TableCell>{t("label.task-id")}</TableCell>
                                <TableCell>{t("label.alert-name")}</TableCell>
                                <TableCell>{t("label.active")}</TableCell>
                                <TableCell>{t("label.more-info")}</TableCell>
                                <TableCell>{t("label.timestamp")}</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {tasksToDisplay.length > 0 &&
                                tasksToDisplay.map((task) => {
                                    return (
                                        <TaskRow key={task.id} task={task} />
                                    );
                                })}
                            {tasksToDisplay.length === 0 && (
                                <TableRow>
                                    <TableCell align="center" colSpan={10}>
                                        {t("message.no-recent-failures")}
                                    </TableCell>
                                </TableRow>
                            )}
                        </TableBody>
                    </Table>
                </LoadingErrorStateSwitch>
            </PageContentsCardV1>
        </>
    );
};
