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
import { Box, Chip, Link, Typography } from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    LocalThemeProviderV1,
    SkeletonV1,
    useDialogProviderV1,
} from "../../../platform/components";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { useQuery } from "@tanstack/react-query";
import { getTasks } from "../../../rest/tasks/tasks.rest";
import { generateDateRangeDaysFromNow } from "../../../utils/routes/routes.util";
import { TaskTableProps } from "./task-table.interfaces";
import { DataGrid, GridColumns, GridSortModel } from "@mui/x-data-grid";
import { formatDateAndTimeV1, lightV1 } from "../../../platform/utils";
import { Task, TaskStatus } from "../../../rest/dto/taks.interface";
import { DialogType } from "../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";

export const TasksTable: FunctionComponent<TaskTableProps> = ({
    alertId,
    headerName,
}) => {
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const [start, end] = generateDateRangeDaysFromNow(7);
    const [sortModel, setSortModel] = useState<GridSortModel>([
        {
            field: "startTime",
            sort: "desc",
        },
    ]);

    const {
        data: tasks,
        isLoading,
        isError,
    } = useQuery({
        queryKey: ["tasks", "alert", alertId, start, end],
        queryFn: () => {
            return getTasks({
                startTime: start,
                endTime: end,
                alertOrSubGroupId: alertId,
            });
        },
    });

    const handleShowError = (task: Task): void => {
        showDialog({
            width: "lg",
            type: DialogType.ALERT,
            contents: task.message,
            okButtonText: t("label.close"),
            hideCancelButton: true,
        });
    };

    const columns: GridColumns = [
        {
            field: "id",
            headerName: t("label.id"),
            flex: 1,
        },
        {
            field: "taskType",
            headerName: t("label.task-type"),
            flex: 2,
        },
        {
            field: "status",
            headerName: t("label.status"),
            flex: 3,
            renderCell: (params) => {
                return (
                    <LocalThemeProviderV1
                        primary={lightV1.palette.error}
                        secondary={lightV1.palette.success}
                    >
                        <Chip
                            color={
                                params.row.status === TaskStatus.FAILED
                                    ? "primary"
                                    : params.row.status === TaskStatus.COMPLETED
                                    ? "secondary"
                                    : "default"
                            }
                            label={params.row.status}
                        />
                    </LocalThemeProviderV1>
                );
            },
        },
        {
            field: "startTime",
            headerName: t("label.timestamp"),
            flex: 2,
            sortable: true,
            renderCell: (params) => formatDateAndTimeV1(params.row.startTime),
        },
        {
            field: "message",
            headerName: t("label.options"),
            flex: 1,
            sortable: false,
            renderCell: (params) => {
                return (
                    <>
                        {params.row.message && (
                            <LocalThemeProviderV1
                                primary={lightV1.palette.error}
                            >
                                <Link
                                    underline="always"
                                    onClick={() =>
                                        handleShowError(params.row as Task)
                                    }
                                >
                                    {t("label.show-log")}
                                </Link>
                            </LocalThemeProviderV1>
                        )}
                    </>
                );
            },
        },
    ];

    /**
     * if statement to prevent the infinite loop by confirming model is
     * different than the current sortModel state
     */
    const handleSortChange = (model: GridSortModel): void => {
        if (JSON.stringify(model) !== JSON.stringify(sortModel)) {
            setSortModel(model);
        }
    };

    return (
        <>
            <Typography variant="h5">{headerName}</Typography>
            <Typography variant="caption">In the last 7 days</Typography>
            <Box paddingTop={2} />
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
                <Box width="100%">
                    <DataGrid
                        autoHeight
                        disableColumnFilter
                        disableColumnSelector
                        disableSelectionOnClick
                        columns={columns}
                        pageSize={10}
                        rows={tasks as Task[]}
                        sortModel={sortModel}
                        onSortModelChange={handleSortChange}
                    />
                </Box>
            </LoadingErrorStateSwitch>
        </>
    );
};
