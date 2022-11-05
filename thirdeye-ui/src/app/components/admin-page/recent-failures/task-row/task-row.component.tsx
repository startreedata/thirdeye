import { Link, Typography } from "@material-ui/core";
import TableCell from "@material-ui/core/TableCell";
import TableRow from "@material-ui/core/TableRow";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import {
    LocalThemeProviderV1,
    SkeletonV1,
    useDialogProviderV1,
} from "../../../../platform/components";
import { DialogType } from "../../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { formatDateAndTimeV1, lightV1 } from "../../../../platform/utils";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { useGetAlert } from "../../../../rest/alerts/alerts.actions";
import { TaskType } from "../../../../rest/dto/taks.interface";
import { getAlertsAlertViewPath } from "../../../../utils/routes/routes.util";
import { ActiveIndicator } from "../../../active-indicator/active-indicator.component";
import { LoadingErrorStateSwitch } from "../../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { TaskRowProps } from "./task-row.interfaces";

export const TaskRow: FunctionComponent<TaskRowProps> = ({ task }) => {
    const { t } = useTranslation();
    const { showDialog } = useDialogProviderV1();
    const { alert, getAlert, status } = useGetAlert();
    const [alertId, setAlertId] = useState();
    const isLoading = useMemo(() => {
        if (
            task.taskType === TaskType.DETECTION ||
            task.taskType === TaskType.NOTIFICATION
        ) {
            return status === ActionStatus.Working;
        }

        return false;
    }, [status, task]);

    useEffect(() => {
        let alertId;

        if (task.taskType === TaskType.DETECTION && task.taskInfo) {
            alertId = JSON.parse(task.taskInfo).configId;
            getAlert(alertId);
        } else if (task.taskType === TaskType.NOTIFICATION && task.taskInfo) {
            alertId = JSON.parse(task.taskInfo).detectionAlertConfigId;
            getAlert(alertId);
        }

        setAlertId(alertId);
    }, [task]);

    const handleShowError = (): void => {
        showDialog({
            width: "xl",
            type: DialogType.ALERT,
            contents: task.message,
            okButtonText: t("label.close"),
            hideCancelButton: true,
        });
    };

    return (
        <TableRow>
            <TableCell>{task.id}</TableCell>
            <LoadingErrorStateSwitch
                errorState={
                    <TableCell colSpan={2}>
                        <Typography color="error" variant="body2">
                            {t(
                                "message.experienced-issues-fetching-data-for-alert-id",
                                {
                                    alertId: alertId,
                                }
                            )}
                        </Typography>
                    </TableCell>
                }
                isError={status === ActionStatus.Error}
                isLoading={isLoading}
                loadingState={
                    <>
                        <TableCell>
                            <SkeletonV1 />
                        </TableCell>
                        <TableCell>
                            <SkeletonV1 />
                        </TableCell>
                    </>
                }
            >
                {alert && (
                    <>
                        <TableCell>
                            <Link
                                component={RouterLink}
                                to={getAlertsAlertViewPath(alert.id)}
                            >
                                {alert.name}
                            </Link>
                        </TableCell>
                        <TableCell>
                            <ActiveIndicator active={alert.active === true} />
                        </TableCell>
                    </>
                )}

                {!alert && (
                    <>
                        <TableCell colSpan={2} />
                    </>
                )}
            </LoadingErrorStateSwitch>
            <TableCell>
                {task.message && (
                    <LocalThemeProviderV1 primary={lightV1.palette.error}>
                        <Link underline="always" onClick={handleShowError}>
                            {t("label.show-log")}
                        </Link>
                    </LocalThemeProviderV1>
                )}
            </TableCell>
            <TableCell>{formatDateAndTimeV1(task.startTime)}</TableCell>
        </TableRow>
    );
};
