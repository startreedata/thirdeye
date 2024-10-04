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
import RefreshIcon from "@material-ui/icons/Refresh";
import { Alert } from "@material-ui/lab";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { ActionStatus } from "../../../../rest/actions.interfaces";
import { determineTimezoneFromAlertInEvaluation } from "../../../../utils/alerts/alerts.util";
// import {
//     TimeRangeButtonWithContext
// } from "../../../time-range/time-range-button-with-context/time-range-button.component";
import { PreviewChartHeaderProps } from "./preview-chart-header.interfaces";
import { DateTimeRangePopover } from "../../../v2/date-time-range-popover";
import { useTimeRange } from "../../../time-range/time-range-provider/time-range-provider.component";
import {
    TimeRange,
    TimeRangeQueryStringKey,
} from "../../../time-range/time-range-provider/time-range-provider.interfaces";

import { useSearchParams } from "react-router-dom";
import { TimeRangeDuration } from "../../../v2/date-time-range/quick-select";

export const PreviewChartHeader: FunctionComponent<PreviewChartHeaderProps> = ({
    alertInsight,
    getEvaluationStatus,
    disableReload,
    onReloadClick,
    onStartEndChange,
    showConfigurationNotReflective,
}) => {
    const { t } = useTranslation();
    const { recentCustomTimeRangeDurations, setTimeRangeDuration } =
        useTimeRange();
    const [searchParams, setSearchParams] = useSearchParams();
    const timeRangeDuration = useMemo(() => {
        return {
            [TimeRangeQueryStringKey.TIME_RANGE]: TimeRange.CUSTOM,
            [TimeRangeQueryStringKey.START_TIME]: Number(
                searchParams.get(TimeRangeQueryStringKey.START_TIME)
            ),
            [TimeRangeQueryStringKey.END_TIME]: Number(
                searchParams.get(TimeRangeQueryStringKey.END_TIME)
            ),
        };
    }, [searchParams]);

    const onHandleTimeRangeChange = (
        timeRangeDuration: TimeRangeDuration
    ): void => {
        setTimeRangeDuration(timeRangeDuration);
        searchParams.set(
            TimeRangeQueryStringKey.TIME_RANGE,
            timeRangeDuration.timeRange
        );
        searchParams.set(
            TimeRangeQueryStringKey.START_TIME,
            timeRangeDuration.startTime.toString()
        );
        searchParams.set(
            TimeRangeQueryStringKey.END_TIME,
            timeRangeDuration.endTime.toString()
        );
        setSearchParams(searchParams);
        if (onStartEndChange) {
            onStartEndChange(
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            );
        }
    };

    return (
        <>
            {getEvaluationStatus !== ActionStatus.Initial && (
                <Grid
                    container
                    alignItems="center"
                    justifyContent="space-between"
                >
                    <Grid item>
                        <Button
                            color="primary"
                            disabled={disableReload}
                            variant="outlined"
                            onClick={onReloadClick}
                        >
                            <RefreshIcon fontSize="small" />
                            {t("label.reload-preview")}
                        </Button>
                    </Grid>
                    {showConfigurationNotReflective &&
                        getEvaluationStatus !== ActionStatus.Working && (
                            <Grid item>
                                <Alert severity="warning" variant="outlined">
                                    {t("message.chart-data-not-reflective")}
                                </Alert>
                            </Grid>
                        )}
                    <Grid item>
                        {/* <TimeRangeButtonWithContext
                            hideQuickExtend
                            btnGroupColor="primary"
                            maxDate={alertInsight?.datasetEndTime}
                            minDate={alertInsight?.datasetStartTime}
                            timezone={determineTimezoneFromAlertInEvaluation(
                                alertInsight?.templateWithProperties
                            )}
                            onTimeRangeChange={onStartEndChange}
                        /> */}
                        <DateTimeRangePopover
                            hideRefresh
                            hideTimeRangeSelectorButton
                            maxDate={alertInsight?.datasetEndTime}
                            minDate={alertInsight?.datasetStartTime}
                            recentCustomTimeRangeDurations={
                                recentCustomTimeRangeDurations
                            }
                            showQuickExtend={false}
                            timeRangeDuration={timeRangeDuration}
                            timezone={determineTimezoneFromAlertInEvaluation(
                                alertInsight?.templateWithProperties
                            )}
                            onChange={onHandleTimeRangeChange}
                        />
                    </Grid>
                </Grid>
            )}
        </>
    );
};
