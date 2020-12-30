import LuxonUtils from "@date-io/luxon";
import {
    Box,
    Button,
    Grid,
    List,
    ListItem,
    ListItemText,
    Popover,
    Tooltip,
    Typography,
} from "@material-ui/core";
import { CalendarToday, Refresh } from "@material-ui/icons";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import { MaterialUiPickersDate } from "@material-ui/pickers/typings/date";
import classnames from "classnames";
import { cloneDeep, isEmpty } from "lodash";
import React, {
    Fragment,
    FunctionComponent,
    MouseEvent,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Dimension } from "../../utils/material-ui-util/dimension-util";
import { Palette } from "../../utils/material-ui-util/palette-util";
import {
    formatTimeRange,
    formatTimeRangeDuration,
    getDefaultTimeRangeDuration,
    getTimeRangeDuration,
} from "../../utils/time-range-util/time-range-util";
import { TimeRangeSelectorCalendarToolbar } from "./time-range-selector-calendar-toolbar/time-range-selector-calendar-toolbar.component";
import {
    TimeRange,
    TimeRangeDuration,
    TimeRangeSelectorProps,
} from "./time-range-selector.interfaces";
import { useTimeRangeSelectorStyles } from "./time-range-selector.styles";

export const TimeRangeSelector: FunctionComponent<TimeRangeSelectorProps> = (
    props: TimeRangeSelectorProps
) => {
    const timeRangeSelectorClasses = useTimeRangeSelectorStyles();
    const [
        componentTimeRangeDuration,
        setComponentTimeRangeDuration,
    ] = useState<TimeRangeDuration>(props.timeRangeDurationFn());
    const [
        timeRangeSelectorAnchorElement,
        setTimeRangeSelectorAnchorElement,
    ] = useState<HTMLElement | null>();
    const { t } = useTranslation();

    const onTimeRangeSelectorClick = (event: MouseEvent<HTMLElement>): void => {
        setTimeRangeSelectorAnchorElement(event.currentTarget);
    };

    const onCloseTimeRangeSelector = (): void => {
        setTimeRangeSelectorAnchorElement(null);
    };

    const onRefresh = (): void => {
        // Notify that component time range duration has changed, to trigger refresh
        props.onChange && props.onChange(cloneDeep(componentTimeRangeDuration));
    };

    const onOpenTimeRangeSelector = (): void => {
        // Update component time range duration
        setComponentTimeRangeDuration(props.timeRangeDurationFn());
    };

    const onRecentCustomTimeRangeDurationClick = (
        timeRangeDuration: TimeRangeDuration
    ): void => {
        if (!timeRangeDuration) {
            return;
        }

        // Update component time range duration
        setComponentTimeRangeDuration(timeRangeDuration);
    };

    const onTimeRangeClick = (timeRange: TimeRange): void => {
        if (timeRange === TimeRange.CUSTOM) {
            initCustomTimeRange();

            return;
        }

        // Update component time range duration
        setComponentTimeRangeDuration(getTimeRangeDuration(timeRange));
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
                {/* Time range label*/}
                {props.timeRangeDuration && (
                    <Typography variant="overline">
                        {formatTimeRange(props.timeRangeDuration.timeRange)}
                    </Typography>
                )}

                {/* Time range duration */}
                <Typography variant="body1">
                    {formatTimeRangeDuration(props.timeRangeDuration)}
                </Typography>
            </Grid>

            {/* Time range button */}
            <Grid item>
                <Button
                    className={timeRangeSelectorClasses.timeRangeButton}
                    color="primary"
                    variant="outlined"
                    onClick={onTimeRangeSelectorClick}
                >
                    <CalendarToday />
                </Button>

                {/* Time range selector */}
                <Popover
                    anchorEl={timeRangeSelectorAnchorElement}
                    open={Boolean(timeRangeSelectorAnchorElement)}
                    onClose={onCloseTimeRangeSelector}
                    onEnter={onOpenTimeRangeSelector}
                >
                    <div
                        className={
                            timeRangeSelectorClasses.timeRangeSelectorContainer
                        }
                    >
                        {/* Header */}
                        <Box
                            border={Dimension.WIDTH_BORDER_DEFAULT}
                            borderColor={Palette.COLOR_BORDER_DEFAULT}
                            borderLeft={0}
                            borderRight={0}
                            borderTop={0}
                            padding={2}
                        >
                            <Grid container>
                                <Grid item>
                                    <Typography variant="h6">
                                        {t("label.customize-time-range")}
                                    </Typography>
                                </Grid>
                            </Grid>
                        </Box>

                        {/* Time range list */}
                        <Box
                            border={Dimension.WIDTH_BORDER_DEFAULT}
                            borderBottom={0}
                            borderColor={Palette.COLOR_BORDER_DEFAULT}
                            borderLeft={0}
                            borderTop={0}
                            className={
                                timeRangeSelectorClasses.timeRangeListContainer
                            }
                        >
                            <List dense>
                                {/* Recent custom time range durations label */}
                                {!isEmpty(
                                    props.recentCustomTimeRangeDurations
                                ) && (
                                    <ListItem
                                        className={
                                            timeRangeSelectorClasses.timeRangeListLabel
                                        }
                                    >
                                        <ListItemText
                                            primary={t("label.recent-custom")}
                                            primaryTypographyProps={{
                                                variant: "overline",
                                            }}
                                        />
                                    </ListItem>
                                )}

                                {/* Recent custom time range durations */}
                                {props.recentCustomTimeRangeDurations &&
                                    props.recentCustomTimeRangeDurations.map(
                                        (
                                            recentCustomTimeRangeDuration,
                                            index
                                        ) => (
                                            <Tooltip
                                                arrow
                                                key={index}
                                                placement="right"
                                                title={formatTimeRangeDuration(
                                                    recentCustomTimeRangeDuration
                                                )}
                                            >
                                                <ListItem
                                                    button
                                                    divider={
                                                        (props.recentCustomTimeRangeDurations &&
                                                            props
                                                                .recentCustomTimeRangeDurations
                                                                .length - 1) ===
                                                        index
                                                    }
                                                    onClick={(): void => {
                                                        onRecentCustomTimeRangeDurationClick(
                                                            recentCustomTimeRangeDuration
                                                        );
                                                    }}
                                                >
                                                    <ListItemText
                                                        primary={formatTimeRangeDuration(
                                                            recentCustomTimeRangeDuration
                                                        )}
                                                        primaryTypographyProps={{
                                                            variant: "button",
                                                            color: "primary",
                                                            className:
                                                                timeRangeSelectorClasses.timeRangeListItem,
                                                        }}
                                                    />
                                                </ListItem>
                                            </Tooltip>
                                        )
                                    )}

                                {/* Time ranges */}
                                {Object.values(TimeRange)
                                    .filter(
                                        (timeRange) =>
                                            typeof timeRange === "string"
                                    )
                                    .map((timeRange, index) => (
                                        <Fragment key={index}>
                                            <ListItem
                                                button
                                                divider={
                                                    timeRange ===
                                                        TimeRange.CUSTOM ||
                                                    timeRange ===
                                                        TimeRange.LAST_30_DAYS ||
                                                    timeRange ===
                                                        TimeRange.YESTERDAY ||
                                                    timeRange ===
                                                        TimeRange.LAST_WEEK ||
                                                    timeRange ===
                                                        TimeRange.LAST_MONTH
                                                }
                                                onClick={(): void => {
                                                    onTimeRangeClick(timeRange);
                                                }}
                                            >
                                                <ListItemText
                                                    primary={formatTimeRange(
                                                        timeRange
                                                    )}
                                                    primaryTypographyProps={{
                                                        variant: "button",
                                                        color: "primary",
                                                        className:
                                                            componentTimeRangeDuration.timeRange ===
                                                            timeRange
                                                                ? classnames(
                                                                      timeRangeSelectorClasses.selectedTimeRangeListItem,
                                                                      timeRangeSelectorClasses.timeRangeListItem
                                                                  )
                                                                : timeRangeSelectorClasses.timeRangeListItem,
                                                    }}
                                                />
                                            </ListItem>
                                        </Fragment>
                                    ))}
                            </List>
                        </Box>

                        {/* Calendars and controls */}
                        <Box
                            className={
                                timeRangeSelectorClasses.calendarsAndControlsContainer
                            }
                        >
                            <Grid container>
                                {/* Calendars */}
                                <Grid
                                    item
                                    className={
                                        timeRangeSelectorClasses.calendarsContainer
                                    }
                                    md={12}
                                >
                                    <MuiPickersUtilsProvider utils={LuxonUtils}>
                                        <Grid container>
                                            {/* Start time calendar */}
                                            <Grid
                                                item
                                                className={
                                                    timeRangeSelectorClasses.startTimeCalendar
                                                }
                                                md={6}
                                            >
                                                {/* Calendar label */}
                                                <div
                                                    className={
                                                        timeRangeSelectorClasses.calendarLabel
                                                    }
                                                >
                                                    <Typography variant="overline">
                                                        {t("label.from")}
                                                    </Typography>
                                                </div>

                                                {/* Calendar */}
                                                <DateTimePicker
                                                    disableFuture
                                                    hideTabs
                                                    ToolbarComponent={
                                                        TimeRangeSelectorCalendarToolbar
                                                    }
                                                    value={
                                                        new Date(
                                                            componentTimeRangeDuration.startTime
                                                        )
                                                    }
                                                    variant="static"
                                                    onChange={onStartTimeChange}
                                                />
                                            </Grid>

                                            {/* End time calendar */}
                                            <Grid
                                                item
                                                className={
                                                    timeRangeSelectorClasses.endTimeCalendar
                                                }
                                                md={6}
                                            >
                                                {/* Calendar label */}
                                                <div
                                                    className={
                                                        timeRangeSelectorClasses.calendarLabel
                                                    }
                                                >
                                                    <Typography variant="overline">
                                                        {t("label.to")}
                                                    </Typography>
                                                </div>

                                                {/* Calendar */}
                                                <DateTimePicker
                                                    disableFuture
                                                    hideTabs
                                                    ToolbarComponent={
                                                        TimeRangeSelectorCalendarToolbar
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
                                                    onChange={onEndTimeChange}
                                                />
                                            </Grid>
                                        </Grid>
                                    </MuiPickersUtilsProvider>
                                </Grid>

                                {/* Controls */}
                                <Grid item md={12}>
                                    <Box
                                        border={Dimension.WIDTH_BORDER_DEFAULT}
                                        borderBottom={0}
                                        borderColor={
                                            Palette.COLOR_BORDER_DEFAULT
                                        }
                                        borderLeft={0}
                                        borderRight={0}
                                        margin={2}
                                        paddingTop={2}
                                    >
                                        <Grid container justify="flex-end">
                                            {/* Cancel button */}
                                            <Grid item>
                                                <Button
                                                    color="primary"
                                                    size="large"
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
                                                    size="large"
                                                    variant="contained"
                                                    onClick={onApply}
                                                >
                                                    {t("label.apply")}
                                                </Button>
                                            </Grid>
                                        </Grid>
                                    </Box>
                                </Grid>
                            </Grid>
                        </Box>
                    </div>
                </Popover>
            </Grid>

            {/* Refresh button */}
            <Grid item>
                <Button
                    className={timeRangeSelectorClasses.timeRangeButton}
                    color="primary"
                    variant="outlined"
                    onClick={onRefresh}
                >
                    <Refresh />
                </Button>
            </Grid>
        </Grid>
    );
};
