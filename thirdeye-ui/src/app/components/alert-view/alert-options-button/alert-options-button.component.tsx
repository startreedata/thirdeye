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
import { Box, IconButton, Menu, MenuItem } from "@material-ui/core";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import { Alert } from "@material-ui/lab";
import { useMutation } from "@tanstack/react-query";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import {
    deleteAlert,
    getAlertInsight,
    rerunAnomalyDetectionForAlert,
} from "../../../rest/alerts/alerts.rest";
import {
    createPathWithRecognizedQueryString,
    getAlertsAlertPath,
    getAlertsAllPath,
    getAlertsCreateCopyPath,
    getAlertsUpdatePath,
    getAnomaliesCreatePath,
} from "../../../utils/routes/routes.util";
import { Modal } from "../../modal/modal.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { SubscriptionGroupsTable } from "../subscription-groups-table/subscription-groups-table.component";
import { TasksTable } from "../tasks-table/tasks-table.component";
import { AlertOptionsButtonProps } from "./alert-options-button.interfaces";

export const AlertOptionsButton: FunctionComponent<AlertOptionsButtonProps> = ({
    alert,
    onChange,
    showViewDetails,
    openButtonRenderer,
    handleAlertResetClick,
    onDetectionRerunSuccess,
}) => {
    const [alertOptionsAnchorElement, setAlertOptionsAnchorElement] =
        useState<HTMLElement | null>();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const { mutateAsync, isLoading, isError } = useMutation({
        mutationFn: async (alertId: number) => {
            const insights = await getAlertInsight({ alertId });

            return rerunAnomalyDetectionForAlert({
                id: alertId,
                start: insights.datasetStartTime,
                end: insights.datasetEndTime,
            });
        },
    });

    const handleAlertOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setAlertOptionsAnchorElement(event.currentTarget);
    };

    const handleAlertOptionsClose = (): void => {
        setAlertOptionsAnchorElement(null);
    };

    const handleAlertViewDetails = (): void => {
        if (!alert) {
            return;
        }

        navigate(getAlertsAlertPath(alert.id));
        handleAlertOptionsClose();
    };

    const handleAlertStateToggle = (): void => {
        if (!alert || !alert) {
            return;
        }
        const copied = { ...alert };

        copied.active = !copied.active;
        onChange && onChange(copied);
        handleAlertOptionsClose();
    };

    const handleAlertDuplicate = (): void => {
        if (!alert) {
            return;
        }

        navigate(getAlertsCreateCopyPath(alert.id));
        handleAlertOptionsClose();
    };

    const handleAlertEdit = (): void => {
        if (!alert) {
            return;
        }

        navigate(getAlertsUpdatePath(alert.id));
        handleAlertOptionsClose();
    };

    const handleCreateAlertAnomaly = (): void => {
        const start = Number(
            searchParams.get(TimeRangeQueryStringKey.START_TIME)
        );
        const end = Number(searchParams.get(TimeRangeQueryStringKey.END_TIME));

        let path = getAnomaliesCreatePath(alert.id);

        // Use the start and end query params being used by the current alert
        if (start && end) {
            const searchParams = new URLSearchParams([
                [TimeRangeQueryStringKey.START_TIME, `${start}`],
                [TimeRangeQueryStringKey.END_TIME, `${end}`],
            ] as string[][]);

            path = createPathWithRecognizedQueryString(path, searchParams);
        }

        navigate(path);
        handleAlertOptionsClose();
    };

    const handleAlertDeleteOk = (): void => {
        deleteAlert(alert.id).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", { entity: t("label.alert") })
            );

            // Redirect to alerts all path
            navigate(getAlertsAllPath());
        });
    };
    const handleRerunAlert = (closeCallback: () => void): boolean => {
        mutateAsync(alert.id).then(() => {
            closeCallback();
            notify(
                NotificationTypeV1.Success,
                t("message.anomaly-detection-task-ran-successfully")
            );
            onDetectionRerunSuccess();
        });

        return false;
    };

    return (
        <>
            {/* Alert options button */}
            {openButtonRenderer && openButtonRenderer(handleAlertOptionsClick)}
            {!openButtonRenderer && (
                <IconButton color="secondary" onClick={handleAlertOptionsClick}>
                    <MoreVertIcon />
                </IconButton>
            )}

            {/* Alert options */}
            <Menu
                anchorEl={alertOptionsAnchorElement}
                open={Boolean(alertOptionsAnchorElement)}
                onClose={handleAlertOptionsClose}
            >
                {/* View details */}
                {showViewDetails && (
                    <MenuItem onClick={handleAlertViewDetails}>
                        {t("label.view-details")}
                    </MenuItem>
                )}

                {/* Activate/deactivate alert */}
                <MenuItem onClick={handleAlertStateToggle}>
                    {alert.active
                        ? t("label.deactivate-entity", {
                              entity: t("label.alert"),
                          })
                        : t("label.activate-entity", {
                              entity: t("label.alert"),
                          })}
                </MenuItem>

                {/* Duplicate alert */}
                <MenuItem onClick={handleAlertDuplicate}>
                    {t("label.duplicate-entity", {
                        entity: t("label.alert"),
                    })}
                </MenuItem>

                {/* Edit alert */}
                <MenuItem onClick={handleAlertEdit}>
                    {t("label.edit-entity", {
                        entity: t("label.alert"),
                    })}
                </MenuItem>

                {/* Delete alert */}
                <Modal
                    submitButtonLabel={t("label.confirm")}
                    trigger={(onClick) => (
                        <MenuItem onClick={onClick}>
                            {t("label.delete-entity", {
                                entity: t("label.alert"),
                            })}
                        </MenuItem>
                    )}
                    onSubmit={handleAlertDeleteOk}
                >
                    <p>
                        {t("message.delete-confirmation", {
                            name: alert.name,
                        })}
                    </p>
                </Modal>

                {/* Rerun detection task */}
                <Modal
                    disableSubmitButton={isLoading}
                    submitButtonLabel={
                        isLoading ? "Running..." : t("label.confirm")
                    }
                    title={t("label.rerun-the-anomalies-detection-task")}
                    trigger={(onClick) => (
                        <MenuItem onClick={onClick}>
                            {t("label.rerun-anomaly-detection")}
                        </MenuItem>
                    )}
                    onSubmit={handleRerunAlert}
                >
                    {isError && (
                        <Alert severity="error" variant="outlined">
                            {t(
                                "message.an-error-was-experienced-while-trying-to"
                            )}
                        </Alert>
                    )}
                    <p>
                        {t(
                            "message.confirming-will-rerun-the-anomalies-task-which"
                        )}
                    </p>
                </Modal>

                {/* Create anomaly for alert */}
                <MenuItem onClick={handleCreateAlertAnomaly}>
                    {t("label.create-child-for-parent", {
                        child: t("label.anomaly"),
                        parent: t("label.alert"),
                    })}
                </MenuItem>

                {/* Reset alert */}
                <Modal
                    submitButtonLabel={t("label.confirm")}
                    trigger={(onClick) => (
                        <MenuItem onClick={onClick}>
                            {t("label.reset-anomalies-for-alert")}
                        </MenuItem>
                    )}
                    onSubmit={handleAlertResetClick}
                >
                    <p>{t("message.reset-alert-information")}</p>
                    <p>
                        {t("message.reset-alert-confirmation-prompt", {
                            alertName: alert.name,
                        })}
                    </p>
                </Modal>

                {/* View tasks for alert */}
                <Modal
                    cancelButtonLabel={t("label.close")}
                    maxWidth="lg"
                    trigger={(onClick) => (
                        <MenuItem onClick={onClick}>
                            {t("label.view-tasks-for-alert")}
                        </MenuItem>
                    )}
                >
                    <Box
                        width={{
                            xs: "100%",
                            sm: "100%",
                            md: "800px",
                            lg: "1000px",
                            xl: "1000px",
                        }}
                    >
                        <TasksTable
                            alertId={alert.id}
                            headerName={t(
                                "label.recent-task-statuses-for-alert"
                            )}
                        />
                    </Box>
                </Modal>

                {/* View subscription group for alert */}
                <Modal
                    cancelButtonLabel={t("label.close")}
                    maxWidth="lg"
                    trigger={(onClick) => (
                        <MenuItem onClick={onClick}>
                            {t("label.view-subscription-groups-for-alert")}
                        </MenuItem>
                    )}
                >
                    <Box
                        width={{
                            xs: "100%",
                            sm: "100%",
                            md: "800px",
                            lg: "1000px",
                            xl: "1000px",
                        }}
                    >
                        <SubscriptionGroupsTable
                            alertId={alert.id}
                            headerName={t("label.subscription-groups")}
                        />
                    </Box>
                </Modal>
            </Menu>
        </>
    );
};
