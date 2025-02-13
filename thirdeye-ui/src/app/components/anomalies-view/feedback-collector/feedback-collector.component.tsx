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
import { Link as RouterLink } from "react-router-dom";
import {
    AnomalyFeedback,
    AnomalyFeedbackType,
} from "../../../rest/dto/anomaly.interfaces";
import { getRootCauseAnalysisForAnomalyInvestigateV2Path } from "../../../utils/routes/routes.util";
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
            <Grid
                container
                alignItems="center"
                data-testId="feedback-collector-container"
                justifyContent="space-between"
            >
                <Grid
                    item
                    lg
                    data-testId="feedback-collector-text"
                    md={5}
                    sm={12}
                >
                    <Typography variant="h4">
                        {t("label.confirm-anomaly")}
                    </Typography>
                </Grid>
                <Grid
                    item
                    lg
                    data-testId="feedback-collector-buttons"
                    md={7}
                    sm={12}
                >
                    <Grid container justifyContent="flex-end">
                        <Grid item>
                            <AnomalyFeedbackModal
                                noOnly
                                anomalyFeedback={localFeedback}
                                anomalyId={anomaly.id}
                                trigger={(openCallback) => {
                                    return (
                                        <Button
                                            color="primary"
                                            data-testId="not-an-anomaly"
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
                                            data-testId="is-an-anomaly"
                                            variant="outlined"
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
                        <Grid item>
                            <Button
                                color="primary"
                                component={RouterLink}
                                data-testId="investigate-anomaly"
                                size="medium"
                                to={`${getRootCauseAnalysisForAnomalyInvestigateV2Path(
                                    anomaly?.id
                                )}`}
                                variant="contained"
                            >
                                {t("label.investigate-entity", {
                                    entity: t("label.anomaly"),
                                })}
                            </Button>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </>
    );
};
