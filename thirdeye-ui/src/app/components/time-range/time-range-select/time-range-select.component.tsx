import {
    FormControl,
    InputLabel,
    ListItemText,
    ListSubheader,
    MenuItem,
    Select,
} from "@material-ui/core";
import { inRange, isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    formatTimeRange,
    formatTimeRangeDuration,
    getTimeRangeDuration,
} from "../../../utils/time-range/time-range.util";
import { TimeRange } from "../time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelectProps } from "./time-range-select.interfaces";

export const TimeRangeSelect: FunctionComponent<TimeRangeSelectProps> = ({
    recentCustomTimeRangeDurations,
    selectedTimeRange,
    onChange,
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
        <FormControl fullWidth variant="outlined">
            {/* Label */}
            <InputLabel id="time-range-select-label">
                {t("label.time-range")}
            </InputLabel>

            {/* Select */}
            <Select
                label={t("label.time-range")}
                labelId="time-range-select-label"
                value={selectedTimeRange}
            >
                {/* Recent custom time range durations label */}
                {!isEmpty(recentCustomTimeRangeDurations) && (
                    <ListSubheader disableSticky>
                        <ListItemText
                            primary={t("label.recent-custom")}
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
                                onChange &&
                                onChange(recentCustomTimeRangeDuration)
                            }
                        >
                            <ListItemText
                                primary={formatTimeRangeDuration(
                                    recentCustomTimeRangeDuration
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
                        onClick={() => onChange && onChange(timeRange)}
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
            </Select>
        </FormControl>
    );
};
