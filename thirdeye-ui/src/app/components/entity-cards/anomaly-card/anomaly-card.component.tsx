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
import { Card, CardContent, Grid, Typography } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { DEFAULT_FEEDBACK } from "../../../utils/alerts/alerts.util";
import { getUiAnomaly } from "../../../utils/anomalies/anomalies.util";
import { timezoneStringShort } from "../../../utils/time/time.util";
import { AnomalyFeedback } from "../../anomaly-feedback/anomaly-feedback.component";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AnomalySummaryCardDetail } from "../root-cause-analysis/anomaly-summary-card/anomaly-summary-card-deatil.component";
import { AnomalyCardProps } from "./anomaly-card.interfaces";
import { useAnomalyCardStyles } from "./anomaly-card.styles";

export const AnomalyCard: FunctionComponent<AnomalyCardProps> = ({
    isLoading,
    className,
    anomaly,
    timezone,
    hideFeedback,
}) => {
    const anomalyCardClasses = useAnomalyCardStyles();
    const { t } = useTranslation();

    const uiAnomaly = anomaly && getUiAnomaly(anomaly);

    const resetMargin = { paddingTop: 2, paddingBottom: 2 };

    return (
        <LoadingErrorStateSwitch
            isError={false}
            isLoading={!!isLoading}
            loadingState={
                <PageContentsCardV1 className={className}>
                    <SkeletonV1 height={75} variant="rect" />
                </PageContentsCardV1>
            }
        >
            <Card className={className} variant="outlined">
                <CardContent>
                    {uiAnomaly && (
                        <Grid
                            container
                            alignItems="center"
                            justifyContent="space-between"
                        >
                            <Grid item>
                                <Grid container alignItems="center">
                                    <Grid item style={resetMargin}>
                                        <Typography
                                            color="textSecondary"
                                            component="span"
                                            variant="body2"
                                        >
                                            {t("label.start")}:
                                        </Typography>
                                    </Grid>
                                    <Grid item style={resetMargin}>
                                        <Typography
                                            component="span"
                                            variant="subtitle1"
                                        >
                                            {`${formatDateAndTimeV1(
                                                uiAnomaly.startTimeVal,
                                                timezone
                                            )} (${timezoneStringShort(
                                                timezone
                                            )})`}
                                        </Typography>
                                    </Grid>
                                </Grid>
                                <Grid container alignItems="center">
                                    <Grid item style={resetMargin}>
                                        <Typography
                                            color="textSecondary"
                                            component="span"
                                            variant="body2"
                                        >
                                            {t("label.end")}:
                                        </Typography>
                                    </Grid>
                                    <Grid item style={resetMargin}>
                                        <Typography
                                            component="span"
                                            variant="subtitle1"
                                        >
                                            {`${formatDateAndTimeV1(
                                                uiAnomaly.endTimeVal,
                                                timezone
                                            )} (${timezoneStringShort(
                                                timezone
                                            )})`}
                                        </Typography>
                                    </Grid>
                                </Grid>
                            </Grid>

                            {/* Duration */}
                            <Grid item>
                                <AnomalySummaryCardDetail
                                    label={t("label.duration")}
                                    value={uiAnomaly.duration}
                                />
                            </Grid>

                            {/* Current/Predicted */}
                            <Grid item>
                                <AnomalySummaryCardDetail
                                    label={`${t("label.current")}${t(
                                        "label.pair-separator"
                                    )}${t("label.predicted")}`}
                                    value={`${uiAnomaly.current}${t(
                                        "label.pair-separator"
                                    )}${uiAnomaly.predicted}`}
                                />
                            </Grid>

                            {/* Deviation */}
                            <Grid item>
                                <AnomalySummaryCardDetail
                                    label={t("label.deviation")}
                                    value={uiAnomaly.deviation}
                                    valueClassName={classnames({
                                        [anomalyCardClasses.deviation]:
                                            uiAnomaly.negativeDeviation,
                                    })}
                                />
                            </Grid>

                            {!hideFeedback && anomaly && (
                                <Grid item>
                                    <AnomalyFeedback
                                        hideComment
                                        anomalyFeedback={
                                            anomaly.feedback || {
                                                ...DEFAULT_FEEDBACK,
                                            }
                                        }
                                        anomalyId={anomaly.id}
                                    />
                                </Grid>
                            )}
                        </Grid>
                    )}

                    {/* No data available */}
                    {!uiAnomaly && <NoDataIndicator />}
                </CardContent>
            </Card>
        </LoadingErrorStateSwitch>
    );
};
