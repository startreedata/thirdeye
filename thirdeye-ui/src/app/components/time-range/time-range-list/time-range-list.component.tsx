/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { List, ListItem, ListItemText, ListSubheader } from "@material-ui/core";
import { inRange, isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { TooltipV1 } from "../../../platform/components";
import {
    formatTimeRange,
    formatTimeRangeDuration,
    getTimeRangeDuration,
} from "../../../utils/time-range/time-range.util";
import { TimeRange } from "../time-range-provider/time-range-provider.interfaces";
import { TimeRangeListProps } from "./time-range-list.interfaces";

export const TimeRangeList: FunctionComponent<TimeRangeListProps> = ({
    recentCustomTimeRangeDurations,
    selectedTimeRange,
    onClick,
    maxDate,
    minDate,
}) => {
    const { t } = useTranslation();
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

    return (
        <List dense>
            {/* Recent custom time range durations label */}
            {!isEmpty(recentCustomTimeRangeDurations) && (
                <ListSubheader disableSticky>
                    <ListItemText
                        primary={t("label.recent-custom")}
                        primaryTypographyProps={{
                            variant: "overline",
                            noWrap: true,
                        }}
                    />
                </ListSubheader>
            )}

            {/* Recent custom time range durations */}
            {filteredRecentTimeRanges.map(
                (recentCustomTimeRangeDuration, index) => (
                    <TooltipV1
                        visible
                        key={index}
                        placement="right"
                        title={formatTimeRangeDuration(
                            recentCustomTimeRangeDuration
                        )}
                    >
                        <ListItem
                            button
                            divider={
                                index ===
                                (recentCustomTimeRangeDurations &&
                                    recentCustomTimeRangeDurations.length - 1)
                            }
                            onClick={() =>
                                onClick &&
                                onClick(recentCustomTimeRangeDuration)
                            }
                        >
                            <ListItemText
                                primary={formatTimeRangeDuration(
                                    recentCustomTimeRangeDuration
                                )}
                                primaryTypographyProps={{
                                    variant: "button",
                                    color: "primary",
                                    noWrap: true,
                                }}
                            />
                        </ListItem>
                    </TooltipV1>
                )
            )}

            {/* Time ranges */}
            {filteredQuickSelections.map((timeRange, index) => (
                <ListItem
                    button
                    divider={
                        timeRange === TimeRange.CUSTOM ||
                        timeRange === TimeRange.LAST_30_DAYS ||
                        timeRange === TimeRange.YESTERDAY ||
                        timeRange === TimeRange.LAST_WEEK ||
                        timeRange === TimeRange.LAST_MONTH
                    }
                    key={index}
                    selected={timeRange === selectedTimeRange}
                    onClick={() => onClick && onClick(timeRange)}
                >
                    <ListItemText
                        primary={formatTimeRange(timeRange)}
                        primaryTypographyProps={{
                            variant: "button",
                            color: "primary",
                            noWrap: true,
                        }}
                    />
                </ListItem>
            ))}
        </List>
    );
};
