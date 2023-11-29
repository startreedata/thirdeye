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
import { Button, Grid, Typography, useTheme } from "@material-ui/core";
import EditIcon from "@material-ui/icons/Edit";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { AnomalyFeedbackType } from "../../../rest/dto/anomaly.interfaces";
import { getRootCauseAnalysisForAnomalyInvestigateV2Path } from "../../../utils/routes/routes.util";
import { AnomalyFeedbackModal } from "../../anomaly-feedback/modal/anomaly-feedback-modal.component";
import { FeedbackCardProps } from "./feedback-card.interfaces";

export const FeedbackCard: FunctionComponent<FeedbackCardProps> = ({
    anomalyId,
    feedback,
    onFeedbackUpdate,
}) => {
    const { t } = useTranslation();
    const theme = useTheme();

    const lastUpdatedStr = useMemo(() => {
        if (feedback?.updated) {
            return formatDateAndTimeV1(feedback?.updated);
        } else if (feedback?.created) {
            return formatDateAndTimeV1(feedback?.created);
        }

        return null;
    }, [feedback]);

    return (
        <Grid container alignItems="center" justifyContent="space-between">
            <Grid item md sm={6} xs={12}>
                {t("message.anomaly-confirmed-to-be-a")}
                {feedback?.type === AnomalyFeedbackType.NOT_ANOMALY ? (
                    <Typography color="error" component="span" variant="body2">
                        {t("message.false-positive")}
                    </Typography>
                ) : (
                    <Typography
                        component="span"
                        style={{ color: theme.palette.success.main }}
                        variant="body2"
                    >
                        {t("message.true-anomaly")}
                    </Typography>
                )}
                {feedback?.updatedBy &&
                    t("message.by", {
                        name: feedback.updatedBy.principal,
                    })}
                {lastUpdatedStr &&
                    t("message.on", { lastUpdatedStr: lastUpdatedStr })}
            </Grid>
            <Grid item md sm={6} xs={12}>
                <Grid container alignItems="center" justifyContent="flex-end">
                    <Grid item>
                        <AnomalyFeedbackModal
                            showNo
                            anomalyFeedback={feedback}
                            anomalyId={anomalyId}
                            trigger={(openCallback) => {
                                return (
                                    <Button
                                        color="primary"
                                        endIcon={<EditIcon />}
                                        size="medium"
                                        variant="outlined"
                                        onClick={openCallback}
                                    >
                                        {t("message.review-anomaly-decision")}
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
                            size="medium"
                            to={`${getRootCauseAnalysisForAnomalyInvestigateV2Path(
                                anomalyId
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
    );
};
