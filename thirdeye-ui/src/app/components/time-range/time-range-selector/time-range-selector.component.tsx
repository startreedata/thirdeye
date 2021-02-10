import LuxonUtils from "@date-io/luxon";
import {
    Box,
    Button,
    Card,
    CardContent,
    CardHeader,
    Grid,
    Popover,
    Typography,
} from "@material-ui/core";
import CalendarTodayIcon from "@material-ui/icons/CalendarToday";
import RefreshIcon from "@material-ui/icons/Refresh";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import { MaterialUiPickersDate } from "@material-ui/pickers/typings/date";
import classnames from "classnames";
import { cloneDeep } from "lodash";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useCommonStyles } from "../../../utils/material-ui/common.styles";
import { Dimension } from "../../../utils/material-ui/dimension.util";
import { Palette } from "../../../utils/material-ui/palette.util";
import {
    formatTimeRange,
    formatTimeRangeDuration,
    getDefaultTimeRangeDuration,
    getTimeRangeDuration,
} from "../../../utils/time-range/time-range.util";
import { TimeRangeList } from "../time-range-list/time-range-list.component";
import {
    TimeRange,
    TimeRangeDuration,
} from "../time-range-provider/time-range-provider.interfaces";
import { DateTimePickerToolbar } from "./date-time-picker-toolbar/date-time-picker-toolbar.component";
import { TimeRangeSelectorProps } from "./time-range-selector.interfaces";
import { useTimeRangeSelectorStyles } from "./time-range-selector.styles";

export const TimeRangeSelector: FunctionComponent<TimeRangeSelectorProps> = (
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

    const onTimeRangeSelectorClick = (event: MouseEvent<HTMLElement>): void => {
        setTimeRangeSelectorAnchorElement(event.currentTarget);
    };

    const onOpenTimeRangeSelector = (): void => {
        // Update component time range duration
        setComponentTimeRangeDuration(
            props.timeRangeDuration || getDefaultTimeRangeDuration()
        );
    };

    const onCloseTimeRangeSelector = (): void => {
        setTimeRangeSelectorAnchorElement(null);
    };

    const onTimeRangeListClick = (
        eventObject: TimeRangeDuration | TimeRange
    ): void => {
        if (typeof eventObject === "string") {
            onTimeRangeClick(eventObject);

            return;
        }

        onRecentCustomTimeRangeDurationClick(eventObject);
    };

    const onTimeRangeClick = (timeRange: TimeRange): void => {
        if (timeRange === TimeRange.CUSTOM) {
            initCustomTimeRange();

            return;
        }

        // Update component time range duration
        setComponentTimeRangeDuration(getTimeRangeDuration(timeRange));
    };

    const onRecentCustomTimeRangeDurationClick = (
        customTimeRangeDuration: TimeRangeDuration
    ): void => {
        if (!customTimeRangeDuration) {
            return;
        }

        // Update component time range duration
        setComponentTimeRangeDuration(customTimeRangeDuration);
    };

    const onStartTimeChange = (date: MaterialUiPickersDate): void => {
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

    const onEndTimeChange = (date: MaterialUiPickersDate): void => {
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

    const onApply = (): void => {
        // Notify that component time range duration has changed
        props.onChange && props.onChange(componentTimeRangeDuration);

        onCloseTimeRangeSelector();
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

    return (
        <Grid container alignItems="center">
            {/* Time range */}
            <Grid item>
                {props.timeRangeDuration && (
                    <>
                        {/* Time range label*/}
                        <Typography variant="overline">
                            {formatTimeRange(props.timeRangeDuration.timeRange)}
                        </Typography>

                        {/* Time range duration */}
                        <Typography variant="body2">
                            {formatTimeRangeDuration(props.timeRangeDuration)}
                        </Typography>
                    </>
                )}
            </Grid>

            {/* Time range selector button */}
            <Grid item>
                <Button
                    className={timeRangeSelectorClasses.button}
                    color="primary"
                    variant="outlined"
                    onClick={onTimeRangeSelectorClick}
                >
                    <CalendarTodayIcon />
                </Button>

                {/* Time range selector */}
                <Popover
                    anchorEl={timeRangeSelectorAnchorElement}
                    classes={{ paper: timeRangeSelectorClasses.popoverPaper }}
                    open={Boolean(timeRangeSelectorAnchorElement)}
                    onClose={onCloseTimeRangeSelector}
                    onEnter={onOpenTimeRangeSelector}
                >
                    <Card
                        className={timeRangeSelectorClasses.timeRangeSelector}
                        elevation={0}
                    >
                        {/* Header */}
                        <Box
                            border={Dimension.WIDTH_BORDER_DEFAULT}
                            borderColor={Palette.COLOR_BORDER_DEFAULT}
                            borderLeft={0}
                            borderRight={0}
                            borderTop={0}
                        >
                            <CardHeader
                                title={t("label.customize-time-range")}
                                titleTypographyProps={{ variant: "h6" }}
                            />
                        </Box>

                        <CardContent
                            className={classnames(
                                timeRangeSelectorClasses.timeRangeSelectorContents,
                                commonClasses.cardContentBottomPaddingDisable,
                                commonClasses.gridLimitation
                            )}
                        >
                            <Grid container>
                                {/* Time range list */}
                                <Box
                                    border={Dimension.WIDTH_BORDER_DEFAULT}
                                    borderBottom={0}
                                    borderColor={Palette.COLOR_BORDER_DEFAULT}
                                    borderLeft={0}
                                    borderTop={0}
                                    className={
                                        timeRangeSelectorClasses.timeRangeList
                                    }
                                >
                                    <Grid item>
                                        <TimeRangeList
                                            recentCustomTimeRangeDurations={
                                                props.recentCustomTimeRangeDurations
                                            }
                                            selectedTimeRange={
                                                componentTimeRangeDuration.timeRange
                                            }
                                            onClick={onTimeRangeListClick}
                                        />
                                    </Grid>
                                </Box>

                                {/* Calendars and controls */}
                                <Grid item>
                                    <Grid container direction="column">
                                        {/* Calendars */}
                                        <Grid item sm={12}>
                                            <MuiPickersUtilsProvider
                                                utils={LuxonUtils}
                                            >
                                                <Grid container>
                                                    {/* Start time calendar */}
                                                    <Box
                                                        className={
                                                            timeRangeSelectorClasses.calendar
                                                        }
                                                    >
                                                        <Grid item sm={6}>
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
                                                                    onStartTimeChange
                                                                }
                                                            />
                                                        </Grid>
                                                    </Box>

                                                    {/* End time calendar */}
                                                    <Box
                                                        className={
                                                            timeRangeSelectorClasses.calendar
                                                        }
                                                    >
                                                        <Grid item sm={6}>
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
                                                                    onEndTimeChange
                                                                }
                                                            />
                                                        </Grid>
                                                    </Box>
                                                </Grid>
                                            </MuiPickersUtilsProvider>
                                        </Grid>

                                        {/* Controls */}
                                        <Grid item sm={12}>
                                            <Grid container justify="flex-end">
                                                {/* Cancel button */}
                                                <Grid item>
                                                    <Button
                                                        color="primary"
                                                        variant="outlined"
                                                        onClick={
                                                            onCloseTimeRangeSelector
                                                        }
                                                    >
                                                        {t("label.cancel")}
                                                    </Button>
                                                </Grid>

                                                {/* Apply button */}
                                                <Grid item>
                                                    <Button
                                                        color="primary"
                                                        variant="contained"
                                                        onClick={onApply}
                                                    >
                                                        {t("label.apply")}
                                                    </Button>
                                                </Grid>
                                            </Grid>
                                        </Grid>
                                    </Grid>
                                </Grid>
                            </Grid>
                        </CardContent>
                    </Card>
                </Popover>
            </Grid>

            {/* Refresh button */}
            <Grid item>
                <Button
                    className={timeRangeSelectorClasses.button}
                    color="primary"
                    variant="outlined"
                    onClick={props.onRefresh}
                >
                    <RefreshIcon />
                </Button>
            </Grid>
        </Grid>
    );
};
