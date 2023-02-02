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
import { Card, CardContent, Grid } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { timezoneStringShort } from "../../../utils/time/time.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { AnomalySummaryCardDetail } from "../root-cause-analysis/anomaly-summary-card/anomaly-summary-card-deatil.component";
import { AnomalyCardProps } from "./anomaly-card.interfaces";
import { useAnomalyCardStyles } from "./anomaly-card.styles";

export const AnomalyCard: FunctionComponent<AnomalyCardProps> = (
    props: AnomalyCardProps
) => {
    const anomalyCardClasses = useAnomalyCardStyles();
    const { t } = useTranslation();

    if (props.isLoading) {
        return (
            <PageContentsCardV1 className={props.className}>
                <SkeletonV1 height={75} variant="rect" />
            </PageContentsCardV1>
        );
    }

    return (
        <Card className={props.className} variant="outlined">
            <CardContent>
                {props.uiAnomaly && (
                    <Grid container justifyContent="space-between" spacing={4}>
                        {/* Start */}
                        <Grid item lg={2} sm={6} xs={12}>
                            <AnomalySummaryCardDetail
                                label={
                                    t("label.start") +
                                    ` (${timezoneStringShort(props.timezone)})`
                                }
                                value={formatDateAndTimeV1(
                                    props.uiAnomaly.startTimeVal,
                                    props.timezone
                                )}
                            />
                        </Grid>

                        {/* End */}
                        <Grid item lg={2} sm={6} xs={12}>
                            <AnomalySummaryCardDetail
                                label={
                                    t("label.end") +
                                    ` (${timezoneStringShort(props.timezone)})`
                                }
                                value={formatDateAndTimeV1(
                                    props.uiAnomaly.endTimeVal,
                                    props.timezone
                                )}
                            />
                        </Grid>

                        {/* Duration */}
                        <Grid item xs={2}>
                            <AnomalySummaryCardDetail
                                label={t("label.duration")}
                                value={props.uiAnomaly.duration}
                            />
                        </Grid>

                        {/* Current/Predicted */}
                        <Grid item xs={2}>
                            <AnomalySummaryCardDetail
                                label={`${t("label.current")}${t(
                                    "label.pair-separator"
                                )}${t("label.predicted")}`}
                                value={`${props.uiAnomaly.current}${t(
                                    "label.pair-separator"
                                )}${props.uiAnomaly.predicted}`}
                            />
                        </Grid>

                        {/* Deviation */}
                        <Grid item xs={2}>
                            <AnomalySummaryCardDetail
                                label={t("label.deviation")}
                                value={props.uiAnomaly.deviation}
                                valueClassName={classnames({
                                    [anomalyCardClasses.deviation]:
                                        props.uiAnomaly.negativeDeviation,
                                })}
                            />
                        </Grid>
                    </Grid>
                )}

                {/* No data available */}
                {!props.uiAnomaly && <NoDataIndicator />}
            </CardContent>
        </Card>
    );
};
