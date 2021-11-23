import LuxonUtils from "@date-io/luxon";
import {
    Button,
    Card,
    CardActions,
    CardContent,
    Grid,
    Hidden,
    Popover,
    Typography,
} from "@material-ui/core";
import { CalendarToday } from "@material-ui/icons";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import { MaterialUiPickersDate } from "@material-ui/pickers/typings/date";
import classnames from "classnames";
import { cloneDeep } from "lodash";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../../../utils/material-ui/common.styles";
import {
    formatTimeRangeDuration,
    getDefaultTimeRangeDuration,
    getTimeRangeDuration,
} from "../../../../utils/time-range/time-range.util";
import { SafariMuiGridFix } from "../../../safari-mui-grid-fix/safari-mui-grid-fix.component";
import { TimeRangeList } from "../../time-range-list/time-range-list.component";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../time-range-provider/time-range-provider.interfaces";
import { TimeRangeSelect } from "../../time-range-select/time-range-select.component";
import { DateTimePickerToolbar } from "../date-time-picker-toolbar/date-time-picker-toolbar.component";
import { TimeRangeSelectorControls } from "../time-range-selector-controls/time-range-selector-controls.component";
import { TimeRangeSelectorProps } from "./time-range-selector-v1.interfaces";
import { useTimeRangeSelectorStyles } from "./time-range-selector-v1.styles";

export const TimeRangeSelectorV1: FunctionComponent<TimeRangeSelectorProps> = (
    props: TimeRangeSelectorProps
) => {
    const timeRangeSelectorClasses = useTimeRangeSelectorStyles();
    const commonClasses = useCommonStyles();
    const [
        componentTimeRangeDuration,
        setComponentTimeRangeDuration,
    ] = useState<TimeRangeDuration>(
        props.timeRangeDuration || getDefaultTimeRangeDuration()
    );
    const [
        timeRangeSelectorAnchorElement,
        setTimeRangeSelectorAnchorElement,
    ] = useState<HTMLElement | null>();
    const { t } = useTranslation();

    const handleTimeRangeSelectorClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setTimeRangeSelectorAnchorElement(event.currentTarget);
    };

    const handleTimeRangeSelectorOpen = (): void => {
        // Update component time range duration
        setComponentTimeRangeDuration(
            props.timeRangeDuration || getDefaultTimeRangeDuration()
        );
    };

    const handleTimeRangeSelectorClose = (): void => {
        setTimeRangeSelectorAnchorElement(null);
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

    const setTimeRange = (timeRange: TimeRange): void => {
        if (timeRange === TimeRange.CUSTOM) {
            initCustomTimeRange();

            return;
        }

        // Update component time range duration
        setComponentTimeRangeDuration(getTimeRangeDuration(timeRange));
    };

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
            handleTimeRangeSelectorClose();

            return;
        }

        // Notify that component time range duration has changed
        props.onChange && props.onChange(componentTimeRangeDuration);
        handleTimeRangeSelectorClose();
    };

    return (
        <Grid container alignItems="center">
            {!props.hideTimeRangeSelectorButton && (
                <Grid item>
                    {/* Time range selector button */}
                    <Button
                        className={
                            timeRangeSelectorClasses.timeRangeSelectorButton
                        }
                        color="primary"
                        variant="outlined"
                        onClick={handleTimeRangeSelectorClick}
                    >
                        <CalendarToday />
                    </Button>

                    {/* Time range selector */}
                    <Popover
                        anchorEl={timeRangeSelectorAnchorElement}
                        open={Boolean(timeRangeSelectorAnchorElement)}
                        onClose={handleTimeRangeSelectorClose}
                        onEnter={handleTimeRangeSelectorOpen}
                    >
                        <Card elevation={0}>
                            {/* Header */}
                            <CardContent
                                className={classnames(
                                    timeRangeSelectorClasses.timeRangeSelectorContents,
                                    commonClasses.cardContentBottomPaddingRemoved
                                )}
                            >
                                <Grid container spacing={0}>
                                    {/* Time range list */}
                                    <Hidden xsDown>
                                        <Grid item md={3} sm={4}>
                                            <div
                                                className={
                                                    timeRangeSelectorClasses.timeRangeList
                                                }
                                            >
                                                <TimeRangeList
                                                    recentCustomTimeRangeDurations={
                                                        props.recentCustomTimeRangeDurations
                                                    }
                                                    selectedTimeRange={
                                                        componentTimeRangeDuration.timeRange
                                                    }
                                                    onClick={
                                                        handleTimeRangeChange
                                                    }
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
                                                        onChange={
                                                            handleTimeRangeChange
                                                        }
                                                    />
                                                </Grid>
                                            </Hidden>

                                            <MuiPickersUtilsProvider
                                                utils={LuxonUtils}
                                            >
                                                {/* Start time calendar */}
                                                <Grid
                                                    item
                                                    className={
                                                        timeRangeSelectorClasses.startTimeCalendarContainer
                                                    }
                                                    md={6}
                                                    xs={12}
                                                >
                                                    <div
                                                        className={
                                                            timeRangeSelectorClasses.calendar
                                                        }
                                                    >
                                                        {/* Start time label */}
                                                        <Typography
                                                            className={
                                                                timeRangeSelectorClasses.startTimeCalendarLabel
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
                                                            onChange={
                                                                handleStartTimeChange
                                                            }
                                                        />
                                                    </div>
                                                </Grid>

                                                {/* End time calendar */}
                                                <Grid
                                                    item
                                                    className={
                                                        timeRangeSelectorClasses.endTimeCalendarContainer
                                                    }
                                                    md={6}
                                                    xs={12}
                                                >
                                                    <div
                                                        className={
                                                            timeRangeSelectorClasses.calendar
                                                        }
                                                    >
                                                        {/* End time label */}
                                                        <Typography
                                                            className={
                                                                timeRangeSelectorClasses.endTimeCalendarLabel
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
                                                            onChange={
                                                                handleEndTimeChange
                                                            }
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
                                                            justify="flex-end"
                                                        >
                                                            <Grid item>
                                                                <TimeRangeSelectorControls
                                                                    onApply={
                                                                        handleApplyClick
                                                                    }
                                                                    onCancel={
                                                                        handleTimeRangeSelectorClose
                                                                    }
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
                                                onCancel={
                                                    handleTimeRangeSelectorClose
                                                }
                                            />
                                        </Grid>
                                    </Grid>
                                </CardActions>
                            </Hidden>
                        </Card>
                    </Popover>
                </Grid>
            )}
            {/* Time range */}
            {!props.hideTimeRange && props.timeRangeDuration && (
                <Grid item>
                    {/* Time range duration */}
                    <Typography variant="body2">
                        {formatTimeRangeDuration(props.timeRangeDuration)}
                    </Typography>
                </Grid>
            )}

            {/* Fixes layout in Safari */}
            <SafariMuiGridFix />
        </Grid>
    );
};
