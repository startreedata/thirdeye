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
import { Box, Grid, Link, Typography } from "@material-ui/core";
import { sortBy } from "lodash";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    DataGridColumnV1,
    DataGridV1,
    LocalThemeProviderV1,
    PageContentsCardV1,
    SkeletonV1,
    useDialogProviderV1,
} from "../../../platform/components";
import { Task, TaskStatus, TaskType } from "../../../rest/dto/taks.interface";
import { WEEK_IN_MILLISECONDS } from "../../../utils/time/time.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelectorButton } from "../../time-range/v2/time-range-selector-button/time-range-selector-button.component";
import { getTasks } from "../../../rest/tasks/tasks.rest";
import { useFetchQuery } from "../../../rest/hooks/useFetchQuery";
import {
    formatDateAndTimeV1,
    formatDurationV1,
    lightV1,
} from "../../../platform/utils";
import { DialogType } from "../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { RenderAlertName, RenderAlertStatus } from "./render-alert-columns";
import { TEST_IDS } from "../../alert-list-v1/alert-list-v1.interfaces";

export const RecentFailures: FunctionComponent = () => {
    const { t } = useTranslation();
    const [searchParams, setSearchParams] = useSearchParams();
    const { showDialog } = useDialogProviderV1();

    const [startTime, endTime] = useMemo(() => {
        // Apply the default filter of the last 7 days if no
        // prior filter is specified or if the filters are invalid
        let returnStartTime = Number(
            searchParams.get(TimeRangeQueryStringKey.START_TIME)
        );

        let returnEndTime = Number(
            searchParams.get(TimeRangeQueryStringKey.END_TIME)
        );

        if (
            !returnStartTime ||
            !returnEndTime ||
            returnStartTime > returnEndTime
        ) {
            returnEndTime = Date.now();
            returnStartTime = returnEndTime - WEEK_IN_MILLISECONDS;
        }

        return [returnStartTime, returnEndTime];
    }, [searchParams]);

    const {
        data: tasks,
        isLoading,
        isError,
    } = useFetchQuery({
        queryKey: ["tasks", startTime, endTime],
        queryFn: () => {
            return getTasks({
                status: [TaskStatus.TIMEOUT, TaskStatus.FAILED],
                type: [TaskType.DETECTION],
                startTime: Number(startTime),
                endTime: Number(endTime),
            });
        },
    });

    const onHandleTimeRangeChange = (
        startProp: number,
        endProp: number
    ): void => {
        searchParams.set(
            TimeRangeQueryStringKey.START_TIME,
            startProp.toString()
        );
        searchParams.set(TimeRangeQueryStringKey.END_TIME, endProp.toString());
        setSearchParams(searchParams);
    };

    const tasksToDisplay = useMemo(() => {
        if (!tasks) {
            return [];
        }
        const sortedTasks = sortBy(tasks, "startTime").reverse();

        return sortedTasks;
    }, [tasks]);

    const handleShowError = (message: string): void => {
        showDialog({
            width: "xl",
            type: DialogType.ALERT,
            contents: message,
            okButtonText: t("label.close"),
            hideCancelButton: true,
        });
    };

    const getAlertId = (task: Task): number | null => {
        let id: number | null = null;
        if (task.taskType === TaskType.DETECTION && task.taskInfo) {
            id = JSON.parse(task.taskInfo).configId;
        } else if (task.taskType === TaskType.NOTIFICATION && task.taskInfo) {
            id = JSON.parse(task.taskInfo).detectionAlertConfigId;
        }

        return id;
    };

    const renderAlert = (
        _: Record<string, unknown>,
        data: Task
    ): JSX.Element => {
        const id = getAlertId(data);

        return id ? <RenderAlertName id={id} /> : <></>;
    };

    const renderAlertStatus = (
        _: Record<string, unknown>,
        data: Task
    ): JSX.Element => {
        const id = getAlertId(data);

        return id ? <RenderAlertStatus id={id} /> : <></>;
    };
    const renderMoreInfo = (
        _: Record<string, unknown>,
        data: Task
    ): JSX.Element => {
        return (
            <LocalThemeProviderV1 primary={lightV1.palette.error}>
                <Link
                    underline="always"
                    onClick={() => handleShowError(data.message)}
                >
                    {t("label.show-log")}
                </Link>
            </LocalThemeProviderV1>
        );
    };

    const renderCreated = (_: Record<string, unknown>, data: Task): string => {
        return formatDateAndTimeV1(data.created, undefined, true);
    };
    const renderStartTime = (
        _: Record<string, unknown>,
        data: Task
    ): string => {
        return formatDateAndTimeV1(data.startTime, undefined, true);
    };
    const renderDuration = (_: Record<string, unknown>, data: Task): string => {
        return formatDurationV1(data.startTime, data.endTime);
    };
    const taskColumns: DataGridColumnV1<any>[] = [
        {
            key: "id",
            dataKey: "id",
            header: t("label.task-id"),
            minWidth: 0,
            flex: 1.5,
        },
        {
            key: "alert-name",
            dataKey: "alertname",
            header: t("label.alert-name"),
            minWidth: 0,
            flex: 1,
            customCellRenderer: renderAlert,
        },
        {
            key: "active",
            dataKey: "active",
            header: t("label.active"),
            minWidth: 0,
            flex: 1,
            customCellRenderer: renderAlertStatus,
            cellTooltip: false,
        },
        {
            key: "moreInfo",
            dataKey: "task.moreInfo",
            header: t("label.more-info"),
            minWidth: 0,
            flex: 1,
            customCellRenderer: renderMoreInfo,
        },
        {
            key: "created",
            dataKey: "task.created",
            header: t("label.created-at"),
            minWidth: 0,
            flex: 1,
            customCellRenderer: renderCreated,
        },
        {
            key: "startTime",
            dataKey: "task.startTime",
            header: t("label.start-time"),
            minWidth: 0,
            flex: 1,
            customCellRenderer: renderStartTime,
        },
        {
            key: "duration",
            dataKey: "alert.duration",
            header: t("label.duration"),
            minWidth: 0,
            flex: 1,
            customCellRenderer: renderDuration,
        },
    ];

    return (
        <>
            <Grid container alignItems="center" justifyContent="space-between">
                <Grid item />
                <Grid item>
                    <TimeRangeSelectorButton
                        end={endTime}
                        placeholder={t("message.click-to-select-date-range")}
                        start={startTime}
                        onChange={(start, end) => {
                            onHandleTimeRangeChange(start, end);
                        }}
                    />
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
                    isError={isError}
                    isLoading={isLoading}
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
                    <Box height="100vh">
                        <DataGridV1<any>
                            disableSearch
                            disableSelection
                            hideBorder
                            hideToolbar
                            columns={taskColumns}
                            data={tasksToDisplay as Task[]}
                            data-testId={TEST_IDS.TABLE}
                            rowKey="id"
                        />
                    </Box>
                </LoadingErrorStateSwitch>
            </PageContentsCardV1>
        </>
    );
};
