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
import { Button, Grid, Typography } from "@material-ui/core";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    AnomalyFeedback,
    AnomalyFeedbackType,
} from "../../../rest/dto/anomaly.interfaces";
import { AnomalyFeedbackModal } from "../../anomaly-feedback/modal/anomaly-feedback-modal.component";
import { FeedbackCollectorProps } from "./feedback-collector.interfaces";

export const FeedbackCollector: FunctionComponent<FeedbackCollectorProps> = ({
    anomaly,
    onFeedbackUpdate,
}) => {
    const { t } = useTranslation();

    const [localFeedback, setLocalFeedback] = useState<
        AnomalyFeedback | undefined
    >(anomaly.feedback);

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
                            <AnomalyFeedbackModal
                                noOnly
                                anomalyFeedback={localFeedback}
                                anomalyId={anomaly.id}
                                trigger={(openCallback) => {
                                    return (
                                        <Button
                                            color="primary"
                                            variant="outlined"
                                            onClick={() => {
                                                setLocalFeedback({
                                                    type: AnomalyFeedbackType.NOT_ANOMALY,
                                                    comment: "",
                                                });
                                                openCallback();
                                            }}
                                        >
                                            {t(
                                                "message.no-this-is-not-an-anomaly"
                                            )}
                                        </Button>
                                    );
                                }}
                                onFeedbackUpdate={onFeedbackUpdate}
                            />
                        </Grid>
                        <Grid item>
                            <AnomalyFeedbackModal
                                anomalyFeedback={localFeedback}
                                anomalyId={anomaly.id}
                                trigger={(openCallback) => {
                                    return (
                                        <Button
                                            color="primary"
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
