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
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../../../utils/material-ui/common.styles";
import { getTimeRangeDuration } from "../../../../utils/time-range/time-range.util";
import { TimeRangeList } from "../../time-range-list/time-range-list.component";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelect } from "../../time-range-select/time-range-select.component";
import { DateTimePickerToolbar } from "../../time-range-selector/date-time-picker-toolbar/date-time-picker-toolbar.component";
import { TimeRangeSelectorControls } from "../../time-range-selector/time-range-selector-controls/time-range-selector-controls.component";
import { TimeRangeSelectorPopoverProps } from "./time-range-selector-popover-content.interfaces";
import { useTimeRangeSelectorPopoverStyles } from "./time-range-selector-popover-content.styles";

export const TimeRangeSelectorPopoverContent: FunctionComponent<
    TimeRangeSelectorPopoverProps
> = ({
    start,
    end,
    onClose,
    minDate,
    maxDate,
    ...props
}: TimeRangeSelectorPopoverProps) => {
    const [componentStartTime, setComponentStartTime] = useState<number>(start);
    const [componentEndTime, setComponentEndTime] = useState<number>(end);
    const timeRangeSelectorPopoverClasses = useTimeRangeSelectorPopoverStyles();
    const commonClasses = useCommonStyles();
    const { t } = useTranslation();

    /**
     * Update if start and end externally change
     */
    useEffect(() => {
        if (start !== componentStartTime) {
            setComponentStartTime(start);
        }
        if (end !== componentEndTime) {
            setComponentEndTime(end);
        }
    }, [start, end]);

    const setTimeRangeFromString = (timeRange: TimeRange): void => {
        const generatedTimeRange = getTimeRangeDuration(timeRange);

        setComponentStartTime(generatedTimeRange.startTime);
        setComponentEndTime(generatedTimeRange.endTime);
    };

    const handleTimeRangeChange = (
        eventObject: TimeRangeDuration | TimeRange
    ): void => {
        if (typeof eventObject === "string") {
            setTimeRangeFromString(eventObject);

            return;
        }

        setComponentStartTime(eventObject.startTime);
        setComponentEndTime(eventObject.endTime);
    };

    const handleStartTimeChange = (date: MaterialUiPickersDate): void => {
        if (!date) {
            return;
        }

        // End time to be later than, or equal to start time
        if (componentEndTime - date.toMillis() < 0) {
            setComponentEndTime(date.toMillis());
        }

        setComponentStartTime(date.toMillis());
    };

    const handleEndTimeChange = (date: MaterialUiPickersDate): void => {
        if (!date) {
            return;
        }

        // Update component time range duration
        setComponentEndTime(date.toMillis());
    };

    const handleApplyClick = (): void => {
        if (!componentStartTime || !componentEndTime) {
            handleCancel();

            return;
        }

        // Notify that component time range duration has changed
        props.onChange && props.onChange(componentStartTime, componentEndTime);
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
                                    maxDate={maxDate}
                                    minDate={minDate}
                                    recentCustomTimeRangeDurations={
                                        props.recentCustomTimeRangeDurations
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
                                            maxDate={
                                                maxDate
                                                    ? new Date(maxDate)
                                                    : undefined
                                            }
                                            minDate={
                                                minDate
                                                    ? new Date(minDate)
                                                    : undefined
                                            }
                                            value={new Date(componentStartTime)}
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
                                            maxDate={
                                                maxDate
                                                    ? new Date(maxDate)
                                                    : undefined
                                            }
                                            minDate={
                                                new Date(componentStartTime)
                                            }
                                            value={new Date(componentEndTime)}
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
                                        <Grid
                                            container
                                            justifyContent="flex-end"
                                        >
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
                    <Grid container justifyContent="flex-end">
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
