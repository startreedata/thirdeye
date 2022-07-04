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
import {
    FormControl,
    InputLabel,
    ListItemText,
    ListSubheader,
    MenuItem,
    Select,
} from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    formatTimeRange,
    formatTimeRangeDuration,
} from "../../../utils/time-range/time-range.util";
import { TimeRange } from "../time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelectProps } from "./time-range-select.interfaces";

export const TimeRangeSelect: FunctionComponent<TimeRangeSelectProps> = (
    props: TimeRangeSelectProps
) => {
    const { t } = useTranslation();

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
                value={props.selectedTimeRange}
            >
                {/* Recent custom time range durations label */}
                {!isEmpty(props.recentCustomTimeRangeDurations) && (
                    <ListSubheader disableSticky>
                        <ListItemText
                            primary={t("label.recent-custom")}
                            primaryTypographyProps={{ variant: "overline" }}
                        />
                    </ListSubheader>
                )}

                {/* Recent custom time range durations */}
                {props.recentCustomTimeRangeDurations &&
                    props.recentCustomTimeRangeDurations.map(
                        (recentCustomTimeRangeDuration, index) => (
                            <MenuItem
                                divider={
                                    index ===
                                    (props.recentCustomTimeRangeDurations &&
                                        props.recentCustomTimeRangeDurations
                                            .length - 1)
                                }
                                key={index}
                                onClick={() =>
                                    props.onChange &&
                                    props.onChange(
                                        recentCustomTimeRangeDuration
                                    )
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
                {Object.values(TimeRange)
                    .filter((timeRange) => typeof timeRange === "string")
                    .map((timeRange, index) => (
                        <MenuItem
                            divider={
                                timeRange === TimeRange.CUSTOM ||
                                timeRange === TimeRange.LAST_30_DAYS ||
                                timeRange === TimeRange.YESTERDAY ||
                                timeRange === TimeRange.LAST_WEEK ||
                                timeRange === TimeRange.LAST_MONTH
                            }
                            key={index}
                            selected={timeRange === props.selectedTimeRange}
                            value={timeRange}
                            onClick={() =>
                                props.onChange && props.onChange(timeRange)
                            }
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
