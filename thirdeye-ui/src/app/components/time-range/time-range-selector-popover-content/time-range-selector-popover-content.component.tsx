import LuxonUtils from "@date-io/luxon";
import {
    Card,
    CardActions,
    CardContent,
    CardHeader,
    Grid,
    Hidden,
    Typography,
} from "@material-ui/core";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import { MaterialUiPickersDate } from "@material-ui/pickers/typings/date";
import classnames from "classnames";
import { cloneDeep } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../../utils/material-ui/common.styles";
import {
    getDefaultTimeRangeDuration,
    getTimeRangeDuration,
} from "../../../utils/time-range/time-range.util";
import { TimeRangeList } from "../time-range-list/time-range-list.component";
import {
    TimeRange,
    TimeRangeDuration,
} from "../time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelect } from "../time-range-select/time-range-select.component";
import { DateTimePickerToolbar } from "../time-range-selector/date-time-picker-toolbar/date-time-picker-toolbar.component";
import { TimeRangeSelectorControls } from "../time-range-selector/time-range-selector-controls/time-range-selector-controls.component";
import { TimeRangeSelectorPopoverProps } from "./time-range-selector-popover-content.interfaces";
import { useTimeRangeSelectorPopoverStyles } from "./time-range-selector-popover-content.styles";

export const TimeRangeSelectorPopoverContent: FunctionComponent<TimeRangeSelectorPopoverProps> = ({
    timeRangeDuration,
    onClose,
    ...props
}: TimeRangeSelectorPopoverProps) => {
    const timeRangeSelectorPopoverClasses = useTimeRangeSelectorPopoverStyles();
    const commonClasses = useCommonStyles();
    const { t } = useTranslation();

    const [
        componentTimeRangeDuration,
        setComponentTimeRangeDuration,
    ] = useState<TimeRangeDuration>(
        timeRangeDuration || getDefaultTimeRangeDuration()
    );

    /**
     * Update the timerange if it externally changes
     */
    useEffect(() => {
        if (!timeRangeDuration) {
            return;
        }

        setComponentTimeRangeDuration(timeRangeDuration);
    }, [timeRangeDuration]);

    const initCustomTimeRange = (): void => {
        if (componentTimeRangeDuration.timeRange === TimeRange.CUSTOM) {
            // Component time range duration is already a custom time range
            return;
        }

        // Start with setting default time range as custom time range duration
        const customTimeRangeDuration = getDefaultTimeRangeDuration();
        customTimeRangeDuration.timeRange = TimeRange.CUSTOM;

        // Update component time range duration
        setComponentTimeRangeDuration(customTimeRangeDuration);
    };

    const setTimeRange = (timeRange: TimeRange): void => {
        if (timeRange === TimeRange.CUSTOM) {
            initCustomTimeRange();

            return;
        }

        // Update component time range duration
        setComponentTimeRangeDuration(getTimeRangeDuration(timeRange));
    };

    const handleTimeRangeChange = (
        eventObject: TimeRangeDuration | TimeRange
    ): void => {
        if (typeof eventObject === "string") {
            setTimeRange(eventObject);

            return;
        }

        // Update component time range duration
        setComponentTimeRangeDuration(eventObject);
    };

    const handleStartTimeChange = (date: MaterialUiPickersDate): void => {
        if (!date) {
            return;
        }

        // Custom time range duration to be set
        const customTimeRangeDuration = cloneDeep(componentTimeRangeDuration);
        customTimeRangeDuration.timeRange = TimeRange.CUSTOM;
        customTimeRangeDuration.startTime = date.toMillis();

        // End time to be later than, or equal to start time
        if (
            customTimeRangeDuration.endTime -
                customTimeRangeDuration.startTime <
            0
        ) {
            customTimeRangeDuration.endTime = customTimeRangeDuration.startTime;
        }

        // Update component time range duration
        setComponentTimeRangeDuration(customTimeRangeDuration);
    };

    const handleEndTimeChange = (date: MaterialUiPickersDate): void => {
        if (!date) {
            return;
        }

        // Custom time range duration to be set
        const customTimeRangeDuration = cloneDeep(componentTimeRangeDuration);
        customTimeRangeDuration.timeRange = TimeRange.CUSTOM;
        customTimeRangeDuration.endTime = date.toMillis();

        // Update component time range duration
        setComponentTimeRangeDuration(customTimeRangeDuration);
    };

    const handleApplyClick = (): void => {
        if (!componentTimeRangeDuration) {
            handleCancel();

            return;
        }

        // Notify that component time range duration has changed
        props.onChange && props.onChange(componentTimeRangeDuration);
        handleCancel();
    };

    const handleCancel = (): void => {
        onClose && onClose();
    };

    return (
        <Card elevation={0}>
            {/* Header */}
            <CardHeader
                className={
                    timeRangeSelectorPopoverClasses.timeRangeSelectorHeader
                }
                title={t("label.customize-time-range")}
                titleTypographyProps={{ variant: "h6" }}
            />

            <CardContent
                className={classnames(
                    timeRangeSelectorPopoverClasses.timeRangeSelectorContents,
                    commonClasses.cardContentBottomPaddingRemoved
                )}
            >
                <Grid container spacing={0}>
                    {/* Time range list */}
                    <Hidden xsDown>
                        <Grid item md={3} sm={4}>
                            <div
                                className={
                                    timeRangeSelectorPopoverClasses.timeRangeList
                                }
                            >
                                <TimeRangeList
                                    recentCustomTimeRangeDurations={
                                        props.recentCustomTimeRangeDurations
                                    }
                                    selectedTimeRange={
                                        componentTimeRangeDuration.timeRange
                                    }
                                    onClick={handleTimeRangeChange}
                                />
                            </div>
                        </Grid>
                    </Hidden>

                    <Grid item md={9} sm={8} xs={12}>
                        <Grid container>
                            {/* Time range select */}
                            <Hidden smUp>
                                <Grid item xs={12}>
                                    <TimeRangeSelect
                                        recentCustomTimeRangeDurations={
                                            props.recentCustomTimeRangeDurations
                                        }
                                        selectedTimeRange={
                                            componentTimeRangeDuration.timeRange
                                        }
                                        onChange={handleTimeRangeChange}
                                    />
                                </Grid>
                            </Hidden>

                            <MuiPickersUtilsProvider utils={LuxonUtils}>
                                {/* Start time calendar */}
                                <Grid
                                    item
                                    className={
                                        timeRangeSelectorPopoverClasses.startTimeCalendarContainer
                                    }
                                    md={6}
                                    xs={12}
                                >
                                    <div
                                        className={
                                            timeRangeSelectorPopoverClasses.calendar
                                        }
                                    >
                                        {/* Start time label */}
                                        <Typography
                                            className={
                                                timeRangeSelectorPopoverClasses.startTimeCalendarLabel
                                            }
                                            color="textSecondary"
                                            display="block"
                                            variant="overline"
                                        >
                                            {t("label.from")}
                                        </Typography>

                                        {/* Calendar */}
                                        <DateTimePicker
                                            disableFuture
                                            hideTabs
                                            ToolbarComponent={
                                                DateTimePickerToolbar
                                            }
                                            value={
                                                new Date(
                                                    componentTimeRangeDuration.startTime
                                                )
                                            }
                                            variant="static"
                                            onChange={handleStartTimeChange}
                                        />
                                    </div>
                                </Grid>

                                {/* End time calendar */}
                                <Grid
                                    item
                                    className={
                                        timeRangeSelectorPopoverClasses.endTimeCalendarContainer
                                    }
                                    md={6}
                                    xs={12}
                                >
                                    <div
                                        className={
                                            timeRangeSelectorPopoverClasses.calendar
                                        }
                                    >
                                        {/* End time label */}
                                        <Typography
                                            className={
                                                timeRangeSelectorPopoverClasses.endTimeCalendarLabel
                                            }
                                            color="textSecondary"
                                            display="block"
                                            variant="overline"
                                        >
                                            {t("label.to")}
                                        </Typography>

                                        {/* Calendar */}
                                        <DateTimePicker
                                            disableFuture
                                            hideTabs
                                            ToolbarComponent={
                                                DateTimePickerToolbar
                                            }
                                            minDate={
                                                new Date(
                                                    componentTimeRangeDuration.startTime
                                                )
                                            }
                                            value={
                                                new Date(
                                                    componentTimeRangeDuration.endTime
                                                )
                                            }
                                            variant="static"
                                            onChange={handleEndTimeChange}
                                        />
                                    </div>
                                </Grid>
                            </MuiPickersUtilsProvider>

                            {/* Controls when screen width is sm and up */}
                            <Hidden xsDown>
                                <Grid item xs={12}>
                                    <CardActions>
                                        <Grid container justify="flex-end">
                                            <Grid item>
                                                <TimeRangeSelectorControls
                                                    onApply={handleApplyClick}
                                                    onCancel={handleCancel}
                                                />
                                            </Grid>
                                        </Grid>
                                    </CardActions>
                                </Grid>
                            </Hidden>
                        </Grid>
                    </Grid>
                </Grid>
            </CardContent>

            {/* Controls when screen width is xs */}
            <Hidden smUp>
                <CardActions>
                    <Grid container justify="flex-end">
                        <Grid item>
                            <TimeRangeSelectorControls
                                onApply={handleApplyClick}
                                onCancel={handleCancel}
                            />
                        </Grid>
                    </Grid>
                </CardActions>
            </Hidden>
        </Card>
    );
};
