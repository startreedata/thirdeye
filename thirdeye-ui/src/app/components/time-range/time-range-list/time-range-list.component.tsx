import { List, ListItem, ListItemText, ListSubheader } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    formatTimeRange,
    formatTimeRangeDuration,
} from "../../../utils/time-range/time-range.util";
import { TooltipV1 } from "../../platform-ui/components";
import { TimeRange } from "../time-range-provider/time-range-provider.interfaces";
import { TimeRangeListProps } from "./time-range-list.interfaces";

export const TimeRangeList: FunctionComponent<TimeRangeListProps> = (
    props: TimeRangeListProps
) => {
    const { t } = useTranslation();

    return (
        <List dense>
            {/* Recent custom time range durations label */}
            {!isEmpty(props.recentCustomTimeRangeDurations) && (
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
            {props.recentCustomTimeRangeDurations &&
                props.recentCustomTimeRangeDurations.map(
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
                                    (props.recentCustomTimeRangeDurations &&
                                        props.recentCustomTimeRangeDurations
                                            .length - 1)
                                }
                                onClick={() =>
                                    props.onClick &&
                                    props.onClick(recentCustomTimeRangeDuration)
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
            {Object.values(TimeRange)
                .filter((timeRange) => typeof timeRange === "string")
                .map((timeRange, index) => (
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
                        selected={timeRange === props.selectedTimeRange}
                        onClick={() =>
                            props.onClick && props.onClick(timeRange)
                        }
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
