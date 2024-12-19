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
import { Card, CardContent, Grid } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { formatDateAndTimeV1, formatDateV1 } from "../../../platform/utils";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetAlert } from "../../../rest/alerts/alerts.actions";
import { getUiAnomaly } from "../../../utils/anomalies/anomalies.util";
import {
    iso8601ToHuman,
    timezoneStringShort,
} from "../../../utils/time/time.util";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AnomalySummaryCardDetail } from "../root-cause-analysis/anomaly-summary-card-detail/anomaly-summary-card-detail.component";
import { AnomalyCardProps } from "./anomaly-card.interfaces";
import { useAnomalyCardStyles } from "./anomaly-card.styles";

export const AnomalyCard: FunctionComponent<AnomalyCardProps> = ({
    isLoading,
    className,
    anomaly,
    timezone,
    hideTime,
}) => {
    const anomalyCardClasses = useAnomalyCardStyles();
    const { t } = useTranslation();
    const { alert, getAlert, status } = useGetAlert();

    const uiAnomaly = anomaly && getUiAnomaly(anomaly);

    useEffect(() => {
        if (anomaly) {
            getAlert(anomaly.alert.id);
        }
    }, [anomaly]);

    const seasonalityPeriodHumanStr = useMemo(() => {
        if (alert?.templateProperties.seasonalityPeriod) {
            // seasonalityPeriod can be in the enumeration item
            // #TODO find it in the enumeration item
            try {
                return iso8601ToHuman(
                    alert?.templateProperties.seasonalityPeriod as string
                );
            } catch {
                return "";
            }
        }

        return "";
    }, [alert]);

    const dateFormatter = useMemo(() => {
        let formatter = formatDateAndTimeV1;

        if (hideTime) {
            formatter = formatDateV1;
        }

        return formatter;
    }, [hideTime]);

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
                            data-testId="anomaly-summary"
                            justifyContent="space-between"
                        >
                            {/* Start */}
                            <Grid item data-testId="anomaly-start">
                                <AnomalySummaryCardDetail
                                    label={t("label.anomaly-start")}
                                    value={`${dateFormatter(
                                        uiAnomaly.startTimeVal,
                                        timezone
                                    )} (${timezoneStringShort(timezone)})`}
                                />
                            </Grid>

                            {/* End */}
                            <Grid item data-testId="anomaly-end">
                                <AnomalySummaryCardDetail
                                    label={t("label.anomaly-end")}
                                    value={`${dateFormatter(
                                        uiAnomaly.endTimeVal,
                                        timezone
                                    )} (${timezoneStringShort(timezone)})`}
                                />
                            </Grid>

                            {/* Duration */}
                            <Grid item data-testId="anomaly-duration">
                                <AnomalySummaryCardDetail
                                    label="Anomaly duration"
                                    value={uiAnomaly.duration}
                                />
                            </Grid>

                            {/* Seasonality */}
                            <Grid item data-testId="anomaly-seasonality">
                                <LoadingErrorStateSwitch
                                    isError={status === ActionStatus.Error}
                                    isLoading={
                                        status === ActionStatus.Working ||
                                        status === ActionStatus.Initial
                                    }
                                    loadingState={
                                        <AnomalySummaryCardDetail
                                            label="Seasonality"
                                            value={<SkeletonV1 />}
                                        />
                                    }
                                >
                                    <EmptyStateSwitch
                                        emptyState={
                                            <>{/** Purposely empty **/}</>
                                        }
                                        isEmpty={
                                            !alert?.templateProperties
                                                .seasonalityPeriod
                                        }
                                    >
                                        <AnomalySummaryCardDetail
                                            label="Seasonality"
                                            value={seasonalityPeriodHumanStr}
                                        />
                                    </EmptyStateSwitch>
                                </LoadingErrorStateSwitch>
                            </Grid>

                            {/* Deviation (Current/Predicted) */}
                            <Grid item data-testId="anomaly-deviation">
                                <AnomalySummaryCardDetail
                                    label={`${t("label.deviation")} (${t(
                                        "label.current"
                                    )}${t("label.pair-separator")}${t(
                                        "label.predicted"
                                    )})`}
                                    value={`${uiAnomaly.deviation} (${
                                        uiAnomaly.current
                                    }${t("label.pair-separator")}${
                                        uiAnomaly.predicted
                                    })`}
                                    valueClassName={classnames({
                                        [anomalyCardClasses.deviation]:
                                            uiAnomaly.negativeDeviation,
                                    })}
                                />
                            </Grid>
                        </Grid>
                    )}

                    {/* No data available */}
                    {!uiAnomaly && <NoDataIndicator />}
                </CardContent>
            </Card>
        </LoadingErrorStateSwitch>
    );
};
