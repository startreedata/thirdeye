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
import { Button, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useUpdateAnomalyFeedback } from "../../../rest/anomalies/anomaly.actions";
import {
    AnomalyFeedback,
    AnomalyFeedbackType,
} from "../../../rest/dto/anomaly.interfaces";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { AnomalyFeedbackModal } from "../../anomaly-feedback/modal/anomaly-feedback-modal.component";
import { FeedbackCollectorProps } from "./feedback-collector.interfaces";

export const FeedbackCollector: FunctionComponent<FeedbackCollectorProps> = ({
    anomaly,
    onFeedbackUpdate,
}) => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const { updateAnomalyFeedback, status, errorMessages } =
        useUpdateAnomalyFeedback();

    useEffect(() => {
        if (status === ActionStatus.Done) {
            notify(
                NotificationTypeV1.Success,
                t("message.update-success", {
                    entity: t("label.anomaly-feedback"),
                })
            );
        } else if (status === ActionStatus.Error) {
            notifyIfErrors(
                status,
                errorMessages,
                notify,
                t("message.update-error", {
                    entity: t("label.anomaly-feedback"),
                })
            );
        }
    }, [status]);

    const handleNotAnAnomalyClick = (): void => {
        const feedbackObject: AnomalyFeedback = {
            type: AnomalyFeedbackType.NOT_ANOMALY,
            comment: "",
        };

        updateAnomalyFeedback(anomaly.id, feedbackObject).then(() => {
            onFeedbackUpdate(feedbackObject);
        });
    };

    return (
        <>
            <Grid container alignItems="center" justifyContent="space-between">
                <Grid item>
                    <Typography variant="h4">
                        {t("label.confirm-anomaly")}
                    </Typography>
                    <Typography variant="body1">
                        {t(
                            "message.compare-with-previous-time-period-to-confirm-this"
                        )}
                    </Typography>
                </Grid>
                <Grid item>
                    <Grid container>
                        <Grid item>
                            <Button
                                color="primary"
                                disabled={status === ActionStatus.Working}
                                variant="outlined"
                                onClick={handleNotAnAnomalyClick}
                            >
                                {t("message.no-this-is-not-an-anomaly")}
                            </Button>
                        </Grid>
                        <Grid item>
                            <AnomalyFeedbackModal
                                anomalyFeedback={anomaly.feedback}
                                anomalyId={anomaly.id}
                                trigger={(openCallback) => {
                                    return (
                                        <Button
                                            color="primary"
                                            disabled={
                                                status === ActionStatus.Working
                                            }
                                            onClick={openCallback}
                                        >
                                            {t(
                                                "message.yes-this-is-an-anomaly"
                                            )}
                                        </Button>
                                    );
                                }}
                                onFeedbackUpdate={onFeedbackUpdate}
                            />
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </>
    );
};
