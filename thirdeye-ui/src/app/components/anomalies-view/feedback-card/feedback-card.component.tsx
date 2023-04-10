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
import {
    Button,
    Card,
    CardContent,
    Grid,
    Typography,
    useTheme,
} from "@material-ui/core";
import EditIcon from "@material-ui/icons/Edit";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { Link as RouterLink } from "react-router-dom";
import { AnomalyFeedbackType } from "../../../rest/dto/anomaly.interfaces";
import { getRootCauseAnalysisForAnomalyInvestigatePath } from "../../../utils/routes/routes.util";
import { AnomalyFeedbackModal } from "../../anomaly-feedback/modal/anomaly-feedback-modal.component";
import { FeedbackCardProps } from "./feedback-card.interfaces";

export const FeedbackCard: FunctionComponent<FeedbackCardProps> = ({
    anomaly,
    onFeedbackUpdate,
}) => {
    const { t } = useTranslation();
    const theme = useTheme();

    return (
        <Card>
            <CardContent>
                <Grid
                    container
                    alignItems="center"
                    justifyContent="space-between"
                >
                    <Grid item>
                        {t("message.anomaly-confirmed-to-be-a")}
                        {anomaly.feedback?.type ===
                        AnomalyFeedbackType.NOT_ANOMALY ? (
                            <Typography
                                color="error"
                                component="span"
                                variant="body2"
                            >
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
                    </Grid>
                    <Grid item>
                        <Grid container justifyContent="space-between">
                            <Grid item>
                                <AnomalyFeedbackModal
                                    showNo
                                    anomalyFeedback={anomaly.feedback}
                                    anomalyId={anomaly.id}
                                    trigger={(openCallback) => {
                                        return (
                                            <Button
                                                color="primary"
                                                endIcon={<EditIcon />}
                                                variant="outlined"
                                                onClick={openCallback}
                                            >
                                                {t(
                                                    "message.review-anomaly-decision"
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
                                    to={`${getRootCauseAnalysisForAnomalyInvestigatePath(
                                        anomaly.id
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
            </CardContent>
        </Card>
    );
};
