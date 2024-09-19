/*
 * Copyright 2024 StarTree Inc
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
import { ListItemText, ListSubheader, MenuItem } from "@material-ui/core";
import { inRange, isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    formatTimeRange,
    formatTimeRangeDuration,
    getTimeRangeDuration,
} from "../../../utils/time-range/time-range.util";
import { useDateTimeRangeStyles } from "./styles";

interface TimeRangeSelectProps {
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    selectedTimeRange?: TimeRange;
    onChange?: (eventObject: TimeRangeDuration) => void;
    maxDate?: number;
    minDate?: number;
    timezone?: string;
}

export interface TimeRangeDuration {
    timeRange: TimeRange;
    startTime: number;
    endTime: number;
}

export enum TimeRange {
    CUSTOM = "CUSTOM",
    LAST_15_MINUTES = "LAST_15_MINUTES",
    LAST_1_HOUR = "LAST_1_HOUR",
    LAST_12_HOURS = "LAST_12_HOURS",
    LAST_24_HOURS = "LAST_24_HOURS",
    LAST_7_DAYS = "LAST_7_DAYS",
    LAST_30_DAYS = "LAST_30_DAYS",
    TODAY = "TODAY",
    YESTERDAY = "YESTERDAY",
    THIS_WEEK = "THIS_WEEK",
    LAST_WEEK = "LAST_WEEK",
    THIS_MONTH = "THIS_MONTH",
    LAST_MONTH = "LAST_MONTH",
    THIS_YEAR = "THIS_YEAR",
    LAST_YEAR = "LAST_YEAR",
}

export const QuckTimeRangeSelect: FunctionComponent<TimeRangeSelectProps> = ({
    recentCustomTimeRangeDurations,
    selectedTimeRange,
    onChange,
    maxDate,
    minDate,
    timezone,
}) => {
    const { t } = useTranslation();
    const componentStyles = useDateTimeRangeStyles();
    let filteredRecentTimeRanges = recentCustomTimeRangeDurations || [];
    const usedMaxDate = maxDate ?? Number.MAX_VALUE;
    const usedMinDate = minDate ?? 0;

    const filteredQuickSelections = Object.values(TimeRange)
        .filter((timeRange) => typeof timeRange === "string")
        .filter((timeRange) => {
            const { startTime, endTime } = getTimeRangeDuration(timeRange);

            return (
                inRange(startTime, usedMinDate, usedMaxDate) &&
                inRange(endTime, usedMinDate, usedMaxDate)
            );
        });

    filteredRecentTimeRanges = filteredRecentTimeRanges.filter((item) => {
        return (
            inRange(item.startTime, usedMinDate, usedMaxDate) &&
            inRange(item.endTime, usedMinDate, usedMaxDate)
        );
    });

    const handleRangeClick = (range: TimeRange | TimeRangeDuration): void => {
        if (typeof range === "object") {
            onChange && onChange(range);
        } else {
            const dateTimeDuration = getTimeRangeDuration(range);
            onChange && onChange(dateTimeDuration);
        }
    };

    return (
        <div className={componentStyles.quickSelect}>
            {/* Recent custom time range durations label */}
            {!isEmpty(recentCustomTimeRangeDurations) && (
                <ListSubheader disableSticky>
                    <ListItemText
                        primary={t("label.recent-entity", {
                            entity: t("label.custom"),
                        })}
                        primaryTypographyProps={{ variant: "overline" }}
                    />
                </ListSubheader>
            )}

            {/* Recent custom time range durations */}
            {filteredRecentTimeRanges.map(
                (recentCustomTimeRangeDuration, index) => (
                    <MenuItem
                        divider={
                            index ===
                            (recentCustomTimeRangeDurations &&
                                recentCustomTimeRangeDurations.length - 1)
                        }
                        key={index}
                        onClick={() =>
                            handleRangeClick(recentCustomTimeRangeDuration)
                        }
                    >
                        <ListItemText
                            primary={formatTimeRangeDuration(
                                recentCustomTimeRangeDuration,
                                timezone
                            )}
                            primaryTypographyProps={{
                                variant: "button",
                                color: "primary",
                            }}
                        />
                    </MenuItem>
                )
            )}

            {/* Time ranges */}
            {filteredQuickSelections.map((timeRange, index) => (
                <MenuItem
                    divider={
                        timeRange === TimeRange.CUSTOM ||
                        timeRange === TimeRange.LAST_30_DAYS ||
                        timeRange === TimeRange.YESTERDAY ||
                        timeRange === TimeRange.LAST_WEEK ||
                        timeRange === TimeRange.LAST_MONTH
                    }
                    key={index}
                    selected={timeRange === selectedTimeRange}
                    value={timeRange}
                    onClick={() => handleRangeClick(timeRange)}
                >
                    <ListItemText
                        primary={formatTimeRange(timeRange)}
                        primaryTypographyProps={{
                            variant: "button",
                            color: "primary",
                        }}
                    />
                </MenuItem>
            ))}
        </div>
    );
};
