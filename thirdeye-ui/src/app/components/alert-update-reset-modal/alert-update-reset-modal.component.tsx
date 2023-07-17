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
import { Button, Grid } from "@material-ui/core";
import { Alert } from "@material-ui/lab";
import { useMutation } from "@tanstack/react-query";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useSearchParams } from "react-router-dom";
import { QUERY_PARAM_KEY_ANOMALIES_RETRY } from "../../pages/alerts-view-page/alerts-view-page.utils";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useResetAlert } from "../../rest/alerts/alerts.actions";
import {
    getAlertInsight,
    rerunAnomalyDetectionForAlert,
} from "../../rest/alerts/alerts.rest";
import { getAlertsAlertPath } from "../../utils/routes/routes.util";
import { Modal } from "../modal/modal.component";
import { AlertUpdateResetModalProps } from "./alert-update-reset-modal.interfaces";

enum Modes {
    INITIAL,
    RESET,
    RERUN,
}

export const AlertUpdateResetModal: FunctionComponent<AlertUpdateResetModalProps> =
    ({ alert }) => {
        const { t } = useTranslation();
        const [searchParams, setSearchParams] = useSearchParams();
        const navigate = useNavigate();
        const { notify } = useNotificationProviderV1();

        const { resetAlert, status: resetAlertRequestStatus } = useResetAlert();

        const [mode, setMode] = useState(Modes.INITIAL);

        const { mutateAsync, isLoading, isError } = useMutation({
            mutationFn: async (alertId: number) => {
                const insights = await getAlertInsight({ alertId });

                return rerunAnomalyDetectionForAlert({
                    id: alertId,
                    start: insights.datasetStartTime,
                    end: insights.defaultStartTime,
                });
            },
        });

        const handleAlertResetClick = (): boolean => {
            alert &&
                resetAlert(alert.id)
                    .then(() => {
                        searchParams.set(
                            QUERY_PARAM_KEY_ANOMALIES_RETRY,
                            "true"
                        );
                        setSearchParams(searchParams, {
                            replace: true,
                        });
                        // Navigate to alerts detail path
                        navigate(
                            `${getAlertsAlertPath(
                                alert.id
                            )}?${searchParams.toString()}`
                        );
                    })
                    .catch(() => {
                        notify(
                            NotificationTypeV1.Error,
                            t(
                                "message.failed-to-reset-alert-due-to-server-error-however"
                            )
                        );
                    });

            return false;
        };

        const handleRerunAlert = (closeCallback: () => void): boolean => {
            mutateAsync(alert.id).then(() => {
                closeCallback();
                notify(
                    NotificationTypeV1.Success,
                    t("message.anomaly-detection-task-ran-successfully")
                );
                // Navigate to alerts detail path
                navigate(
                    `${getAlertsAlertPath(alert.id)}?${searchParams.toString()}`
                );
            });

            return false;
        };

        return (
            <>
                {mode === Modes.INITIAL && (
                    <Modal
                        initiallyOpen
                        cancelButtonLabel={t("label.no")}
                        title={t(
                            "message.do-you-want-to-reset-the-alert-or-rerun-the"
                        )}
                        trigger={() => <></>}
                        onCancel={() =>
                            navigate(getAlertsAlertPath(alert?.id as number))
                        }
                    >
                        <Grid container justifyContent="space-evenly">
                            <Grid item>
                                <Button
                                    color="primary"
                                    variant="outlined"
                                    onClick={() => setMode(Modes.RESET)}
                                >
                                    {t("label.reset-alert")}
                                </Button>
                            </Grid>
                            <Grid item>
                                <Button
                                    color="primary"
                                    variant="outlined"
                                    onClick={() => setMode(Modes.RERUN)}
                                >
                                    {t("label.rerun-anomaly-detection")}
                                </Button>
                            </Grid>
                        </Grid>
                    </Modal>
                )}
                {mode === Modes.RERUN && (
                    <Modal
                        initiallyOpen
                        disableSubmitButton={isLoading}
                        submitButtonLabel={
                            isLoading ? "Running..." : t("label.confirm")
                        }
                        title={t("label.rerun-the-anomalies-detection-task")}
                        trigger={() => <></>}
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
                )}
                {mode === Modes.RESET && (
                    <Modal
                        initiallyOpen
                        cancelButtonLabel={t("label.no")}
                        disableSubmitButton={
                            resetAlertRequestStatus === ActionStatus.Working
                        }
                        submitButtonLabel={t("label.yes")}
                        trigger={() => <></>}
                        onCancel={() =>
                            navigate(getAlertsAlertPath(alert?.id as number))
                        }
                        onSubmit={handleAlertResetClick}
                    >
                        <p>
                            {t(
                                "message.do-you-want-to-reset-the-alert-which-will-delete"
                            )}
                        </p>
                    </Modal>
                )}
            </>
        );
    };
