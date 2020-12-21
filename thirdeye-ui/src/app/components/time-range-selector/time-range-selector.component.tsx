import LuxonUtils from "@date-io/luxon";
import {
    Box,
    Button,
    Divider,
    Grid,
    List,
    ListItem,
    ListItemText,
    Popover,
    Tooltip,
    Typography,
} from "@material-ui/core";
import { CalendarToday } from "@material-ui/icons";
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
    const [timeRangeDuration, setTimeRangeDuration] = useState<
        TimeRangeDuration
    >(props.getTimeRangeDuration());
    const [
        timeRangeSelectorAnchorElement,
        setTimeRangeSelectorAnchorElement,
    ] = useState<HTMLElement | null>();
    const { t } = useTranslation();

    const onTimeRangeButtonClick = (event: MouseEvent<HTMLElement>): void => {
        setTimeRangeSelectorAnchorElement(event.currentTarget);
    };

    const onTimeRangeSelectorOpen = (): void => {
        // Update component state time range duration
        setTimeRangeDuration(props.getTimeRangeDuration());
    };

    const onRecentCustomTimeRangeDurationClick = (
        timeRangeDuration: TimeRangeDuration
    ): void => {
        if (isEmpty(timeRangeDuration)) {
            return;
        }

        // Update component state time range duration to selected custom time range
        setTimeRangeDuration(timeRangeDuration);
    };

    const onTimeRangeClick = (timeRange: TimeRange): void => {
        if (timeRange === TimeRange.CUSTOM) {
            // Custom time range duration to be set
            initCustomTimeRange();

            return;
        }

        // Update component state time range duration to selected time range
        setTimeRangeDuration(getTimeRangeDuration(timeRange));
    };

    const onStartDateChange = (date: MaterialUiPickersDate): void => {
        if (!date) {
            return;
        }

        // Custom time range duration to be set
        const customTimeRangeDuration = cloneDeep(timeRangeDuration);
        customTimeRangeDuration.timeRange = TimeRange.CUSTOM;
        customTimeRangeDuration.startTime = date.toMillis();

        // Make sure endTime is later or at least equal to the startTime
        if (
            customTimeRangeDuration.endTime -
                customTimeRangeDuration.startTime <
            0
        ) {
            customTimeRangeDuration.endTime = customTimeRangeDuration.startTime;
        }

        // Update component state time range duration
        setTimeRangeDuration(customTimeRangeDuration);
    };

    const onEndDateChange = (date: MaterialUiPickersDate): void => {
        if (!date) {
            return;
        }

        // Custom time range duration to be set
        const customTimeRangeDuration = cloneDeep(timeRangeDuration);
        customTimeRangeDuration.timeRange = TimeRange.CUSTOM;
        customTimeRangeDuration.endTime = date.toMillis();

        // Update component state time range duration
        setTimeRangeDuration(customTimeRangeDuration);
    };

    const onApply = (): void => {
        // Notify that component state time range duration has changed
        props.onChange && props.onChange(timeRangeDuration);

        closeTimeRangeSelector();
    };

    const closeTimeRangeSelector = (): void => {
        setTimeRangeSelectorAnchorElement(null);
    };

    const initCustomTimeRange = (): void => {
        if (timeRangeDuration.timeRange === TimeRange.CUSTOM) {
            // Component state time range duration is already a custom time range, do nothing
            return;
        }

        // Start with setting default time range as custom time range duration
        const customTimeRangeDuration = getDefaultTimeRangeDuration();
        customTimeRangeDuration.timeRange = TimeRange.CUSTOM;

        // Update component state time range duration
        setTimeRangeDuration(customTimeRangeDuration);
    };

    return (
        <Grid container alignItems="center">
            {/* Time range */}
            <Grid item>{formatTimeRangeDuration(props.timeRangeDuration)}</Grid>

            {/* Time range button */}
            <Grid item>
                <Button
                    className={timeRangeSelectorClasses.timeRangeButton}
                    color="primary"
                    variant="outlined"
                    onClick={onTimeRangeButtonClick}
                >
                    <CalendarToday />
                </Button>

                {/* Time range selector */}
                <Popover
                    anchorEl={timeRangeSelectorAnchorElement}
                    open={Boolean(timeRangeSelectorAnchorElement)}
                    onClose={closeTimeRangeSelector}
                    onEnter={onTimeRangeSelectorOpen}
                >
                    <div
                        className={
                            timeRangeSelectorClasses.timeRangeSelectorContainer
                        }
                    >
                        <Grid container direction="column" spacing={0}>
                            {/* Header */}
                            <Grid item>
                                <Box
                                    border={Dimension.WIDTH_BORDER_DEFAULT}
                                    borderColor={Palette.COLOR_BORDER_DEFAULT}
                                    borderLeft={0}
                                    borderRight={0}
                                    borderTop={0}
                                    className={
                                        timeRangeSelectorClasses.timeRangeSelectorChildContainer
                                    }
                                >
                                    <Typography variant="h6">
                                        {t("label.customize-time-range")}
                                    </Typography>
                                </Box>
                            </Grid>

                            <Grid container item spacing={0}>
                                {/* Time range selection */}
                                <Grid item>
                                    <Box
                                        border={Dimension.WIDTH_BORDER_DEFAULT}
                                        borderBottom={0}
                                        borderColor={
                                            Palette.COLOR_BORDER_DEFAULT
                                        }
                                        borderLeft={0}
                                        borderTop={0}
                                        className={
                                            timeRangeSelectorClasses.timeRangeList
                                        }
                                    >
                                        <List dense>
                                            {/* Recent custom time ranges label */}
                                            {props.recentCustomTimeRangeDurations &&
                                                props
                                                    .recentCustomTimeRangeDurations
                                                    .length > 0 && (
                                                    <ListItem>
                                                        <ListItemText
                                                            primary={t(
                                                                "label.recent-custom"
                                                            )}
                                                            primaryTypographyProps={{
                                                                variant:
                                                                    "overline",
                                                            }}
                                                        />
                                                    </ListItem>
                                                )}

                                            {/* Recent custom time ranges */}
                                            {props.recentCustomTimeRangeDurations &&
                                                props.recentCustomTimeRangeDurations.map(
                                                    (
                                                        recentTimeRangeDuration,
                                                        index
                                                    ) => (
                                                        <ListItem
                                                            button
                                                            key={index}
                                                            onClick={(): void => {
                                                                onRecentCustomTimeRangeDurationClick(
                                                                    recentTimeRangeDuration
                                                                );
                                                            }}
                                                        >
                                                            <Tooltip
                                                                arrow
                                                                placement="right"
                                                                title={formatTimeRangeDuration(
                                                                    recentTimeRangeDuration
                                                                )}
                                                            >
                                                                <ListItemText
                                                                    primary={formatTimeRangeDuration(
                                                                        recentTimeRangeDuration
                                                                    )}
                                                                    primaryTypographyProps={{
                                                                        variant:
                                                                            "button",
                                                                        color:
                                                                            "primary",
                                                                        className:
                                                                            timeRangeSelectorClasses.timeRangeListItem,
                                                                    }}
                                                                />
                                                            </Tooltip>
                                                        </ListItem>
                                                    )
                                                )}

                                            {/* Divider */}
                                            {props.recentCustomTimeRangeDurations &&
                                                props
                                                    .recentCustomTimeRangeDurations
                                                    .length > 0 && <Divider />}

                                            {/* Time ranges */}
                                            {Object.values(TimeRange)
                                                // Iterate through available TimeRange values
                                                .filter(
                                                    // Filter string values
                                                    (timeRange) =>
                                                        typeof timeRange ===
                                                        "string"
                                                )
                                                .map((timeRange, index) => (
                                                    <Fragment key={index}>
                                                        <ListItem
                                                            button
                                                            onClick={(): void => {
                                                                onTimeRangeClick(
                                                                    timeRange
                                                                );
                                                            }}
                                                        >
                                                            <ListItemText
                                                                primary={formatTimeRange(
                                                                    timeRange
                                                                )}
                                                                primaryTypographyProps={{
                                                                    variant:
                                                                        "button",
                                                                    color:
                                                                        "primary",
                                                                    className:
                                                                        timeRangeDuration.timeRange ===
                                                                        timeRange
                                                                            ? classnames(
                                                                                  timeRangeSelectorClasses.selectedTimeRange,
                                                                                  timeRangeSelectorClasses.timeRangeListItem
                                                                              )
                                                                            : timeRangeSelectorClasses.timeRangeListItem,
                                                                }}
                                                            />
                                                        </ListItem>

                                                        {/* Place divider after certain options */}
                                                        {(timeRange ===
                                                            TimeRange.CUSTOM ||
                                                            timeRange ===
                                                                TimeRange.LAST_30_DAYS ||
                                                            timeRange ===
                                                                TimeRange.YESTERDAY ||
                                                            timeRange ===
                                                                TimeRange.LAST_WEEK ||
                                                            timeRange ===
                                                                TimeRange.LAST_MONTH) && (
                                                            <Divider />
                                                        )}
                                                    </Fragment>
                                                ))}
                                        </List>
                                    </Box>
                                </Grid>

                                <Grid item>
                                    <Grid
                                        container
                                        direction="column"
                                        spacing={0}
                                    >
                                        {/* Calendars */}
                                        <Grid container item spacing={0}>
                                            <MuiPickersUtilsProvider
                                                utils={LuxonUtils}
                                            >
                                                {/* Start time */}
                                                <Grid item>
                                                    {/* Label */}
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
                                                                timeRangeDuration.startTime
                                                            )
                                                        }
                                                        variant="static"
                                                        onChange={
                                                            onStartDateChange
                                                        }
                                                    />
                                                </Grid>

                                                {/* End time */}
                                                <Grid item>
                                                    {/* Label */}
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
                                                                timeRangeDuration.startTime
                                                            )
                                                        }
                                                        value={
                                                            new Date(
                                                                timeRangeDuration.endTime
                                                            )
                                                        }
                                                        variant="static"
                                                        onChange={
                                                            onEndDateChange
                                                        }
                                                    />
                                                </Grid>
                                            </MuiPickersUtilsProvider>
                                        </Grid>

                                        {/* Controls */}
                                        <Grid
                                            container
                                            item
                                            className={
                                                timeRangeSelectorClasses.timeRangeSelectorChildContainer
                                            }
                                        >
                                            {/* Apply */}
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

                                            {/* Cancel */}
                                            <Grid item>
                                                <Button
                                                    color="primary"
                                                    size="large"
                                                    variant="outlined"
                                                    onClick={
                                                        closeTimeRangeSelector
                                                    }
                                                >
                                                    {t("label.cancel")}
                                                </Button>
                                            </Grid>
                                        </Grid>
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                    </div>
                </Popover>
            </Grid>
        </Grid>
    );
};
