/*
 * Copyright 2024 StarTree Inc
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
import {
    Box,
    Card,
    List,
    ListItem,
    ListItemText,
    Tooltip,
    Typography,
} from "@material-ui/core";
import { ChevronRight } from "@material-ui/icons";
import { Alert } from "@material-ui/lab";
import { useMutation } from "@tanstack/react-query";
import React, { useState } from "react";
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
    getAlertsAllPath,
    getAlertsCreateCopyPath,
    getAlertsUpdatePath,
    getAnomaliesCreatePath,
} from "../../../utils/routes/routes.util";
import { Modal } from "../../modal/modal.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { SubscriptionGroupsTable } from "../subscription-groups-table/subscription-groups-table.component";
import { TasksTable } from "../tasks-table/tasks-table.component";
import { AlertDrawerProps, AlertOption } from "./alert-drawer.interfaces";
import { alertDrawerStyles } from "./alert-drawer.styles";

const AlertDrawer: React.FC<AlertDrawerProps> = ({
    alert,
    onChange,
    onDetectionRerunSuccess,
}) => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const { notify } = useNotificationProviderV1();
    const [isModalOpen, setIsModalOpen] = useState<boolean | string>("");

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

    const handleAlertStateToggle = (): void => {
        if (!alert || !alert) {
            return;
        }
        const copied = { ...alert };

        copied.active = !copied.active;
        onChange && onChange(copied);
    };

    const handleAlertDuplicate = (): void => {
        if (!alert) {
            return;
        }

        navigate(getAlertsCreateCopyPath(alert.id));
    };

    const handleAlertEdit = (): void => {
        if (!alert) {
            return;
        }

        navigate(getAlertsUpdatePath(alert.id));
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

    const optionGroups = [
        {
            id: "configuration",
            title: t("label.configuration"),
            options: [
                {
                    id: "edit",
                    label: t("label.edit-entity", {
                        entity: t("label.alert"),
                    }),
                    subOptions: [
                        {
                            id: "alertWizard",
                            label: t("label.alert-wizard"),
                            onClick: () => handleAlertEdit(),
                        },
                        {
                            id: "advancedJsonEditor",
                            label: t("label.advanced-json-editor"),
                            onClick: () => handleAlertEdit(),
                        },
                        {
                            id: "advancedOld",
                            label: t("label.advanced-old"),
                            onClick: () => handleAlertEdit(),
                        },
                    ],
                },
                {
                    id: "alertOperations",
                    label: t("label.alert-operations"),
                    subOptions: [
                        {
                            id: "deactivateAlert",
                            label: alert?.active
                                ? t("label.deactivate-entity", {
                                      entity: t("label.alert"),
                                  })
                                : t("label.activate-entity", {
                                      entity: t("label.alert"),
                                  }),
                            onClick: () => handleAlertStateToggle(),
                        },
                        {
                            id: "duplicateAlert",
                            label: t("label.duplicate-entity", {
                                entity: t("label.alert"),
                            }),
                            onClick: () => handleAlertDuplicate(),
                        },
                        {
                            id: "deleteAlert",
                            label: t("label.delete-entity", {
                                entity: t("label.alert"),
                            }),
                            onClick: () => setIsModalOpen("deleteAlert"),
                        },
                        {
                            id: "rerunAnomalyDetection",
                            label: t("label.rerun-anomaly-detection"),
                            onClick: () =>
                                setIsModalOpen("rerunAnomalyDetection"),
                        },
                        {
                            id: "createAnomalyForAlert",
                            label: t("label.create-child-for-parent", {
                                child: t("label.anomaly"),
                                parent: t("label.alert"),
                            }),
                            onClick: () => handleCreateAlertAnomaly(),
                        },
                        {
                            id: "viewTaskStatusesForAlert",
                            label: t("label.view-tasks-for-alert"),
                            onClick: () =>
                                setIsModalOpen("viewTaskStatusesForAlert"),
                        },
                    ],
                },
            ],
        },
        {
            id: "notifications",
            title: t("label.notifications"),
            options: [
                {
                    id: "configureNotifications",
                    label: t("label.configure-notifications"),
                    onClick: () => setIsModalOpen("configureNotifications"),
                },
            ],
        },
    ];
    const classes = alertDrawerStyles();

    const renderOptions = (optionsList: AlertOption[]): JSX.Element => (
        <List disablePadding component="div">
            {optionsList.map((option) => (
                <ListItem button key={option.id} onClick={option.onClick}>
                    <Tooltip
                        interactive
                        classes={{ tooltip: classes.tooltip }}
                        placement="right"
                        title={
                            option.subOptions ? (
                                <List disablePadding component="div">
                                    {option.subOptions.map((subOption) =>
                                        subOption.component ? (
                                            subOption.component
                                        ) : (
                                            <ListItem
                                                button
                                                key={subOption.id}
                                                onClick={subOption.onClick}
                                            >
                                                <ListItemText
                                                    primary={subOption.label}
                                                    primaryTypographyProps={{
                                                        variant: "body2",
                                                        color: "textPrimary",
                                                    }}
                                                />
                                            </ListItem>
                                        )
                                    )}
                                </List>
                            ) : (
                                ""
                            )
                        }
                    >
                        <ListItemText
                            primary={option.label}
                            primaryTypographyProps={{
                                variant: "body2",
                                noWrap: true,
                            }}
                        />
                    </Tooltip>
                    {option.subOptions && <ChevronRight />}
                </ListItem>
            ))}
        </List>
    );

    return (
        <>
            <Modal
                cancelButtonLabel={t("label.close")}
                isOpen={isModalOpen === "configureNotifications"}
                maxWidth="lg"
                setIsOpen={setIsModalOpen}
                title={t("label.view-subscription-groups-for-alert")}
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
                        alertId={alert?.id}
                        headerName={t("label.subscription-groups")}
                    />
                </Box>
            </Modal>
            <Modal
                cancelButtonLabel={t("label.close")}
                isOpen={isModalOpen === "viewTaskStatusesForAlert"}
                maxWidth="lg"
                setIsOpen={setIsModalOpen}
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
                        alertId={alert?.id}
                        headerName={t("label.recent-task-statuses-for-alert")}
                    />
                </Box>
            </Modal>
            <Modal
                disableSubmitButton={isLoading}
                isOpen={isModalOpen === "rerunAnomalyDetection"}
                setIsOpen={setIsModalOpen}
                submitButtonLabel={
                    isLoading ? "Running..." : t("label.confirm")
                }
                title={t("label.rerun-the-anomalies-detection-task")}
                onSubmit={handleRerunAlert}
            >
                {isError && (
                    <Alert severity="error" variant="outlined">
                        {t("message.an-error-was-experienced-while-trying-to")}
                    </Alert>
                )}
                <p>
                    {t(
                        "message.confirming-will-rerun-the-anomalies-task-which"
                    )}
                </p>
            </Modal>
            <Modal
                isOpen={isModalOpen === "deleteAlert"}
                setIsOpen={setIsModalOpen}
                submitButtonLabel={t("label.confirm")}
                onSubmit={handleAlertDeleteOk}
            >
                <p>
                    {t("message.delete-confirmation", {
                        name: alert?.name,
                    })}
                </p>
            </Modal>
            <Card className={classes.drawerCard}>
                {optionGroups.map((group) => (
                    <div className={classes.optionGroup} key={group.id}>
                        {group.title && (
                            <Typography
                                className={classes.groupTitle}
                                variant="body2"
                            >
                                {group.title}
                            </Typography>
                        )}
                        {renderOptions(group.options)}
                    </div>
                ))}
            </Card>
        </>
    );
};

export default AlertDrawer;
