import LuxonUtils from "@date-io/luxon";
import { Button, ButtonGroup, Grid, Popover } from "@material-ui/core";
import { CalendarToday } from "@material-ui/icons";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import { MaterialUiPickersDate } from "@material-ui/pickers/typings/date";
import { cloneDeep, kebabCase } from "lodash";
import React, {
    FunctionComponent,
    MouseEvent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { formatLongDateAndTime } from "../../utils/date-time-util/date-time-util";
import { getTimeRangeDuration } from "../../utils/time-range-util/time-range-util";
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

    useEffect(() => {
        setTimeRangeDuration(props.getTimeRangeDuration());
    }, [props.timeRange]);

    const onTimeRangeButtonClick = (event: MouseEvent<HTMLElement>): void => {
        setTimeRangeSelectorAnchorElement(event.currentTarget);
    };

    const onTimeRangeSelectorOpen = (): void => {
        // Update component state time range duration
        setTimeRangeDuration(props.getTimeRangeDuration());
    };

    const onPredefinedTimeRangeClick = (timeRange: TimeRange): void => {
        if (timeRange === TimeRange.CUSTOM) {
            // Custom time range duration to be set
            initCustomTimeRange();

            return;
        }

        // Update component state time range duration to selected predefined time range
        setTimeRangeDuration(getTimeRangeDuration(timeRange));
    };

    const onStartDateChange = (date: MaterialUiPickersDate): void => {
        if (!date) {
            return;
        }

        // Custom time range duration
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

        // Custom time range duration
        const customTimeRangeDuration = cloneDeep(timeRangeDuration);
        customTimeRangeDuration.timeRange = TimeRange.CUSTOM;
        customTimeRangeDuration.endTime = date.toMillis();

        // Update component state time range duration
        setTimeRangeDuration(customTimeRangeDuration);
    };

    const onApply = (): void => {
        // Notify that component state time range duration has changed
        props.onChange &&
            props.onChange(
                timeRangeDuration.timeRange,
                timeRangeDuration.startTime,
                timeRangeDuration.endTime
            );

        closeTimeRangeSelector();
    };

    const onCancel = (): void => {
        // Discard any changes to component state time range
        setTimeRangeDuration(props.getTimeRangeDuration());

        closeTimeRangeSelector();
    };

    const initCustomTimeRange = (): void => {
        if (timeRangeDuration.timeRange === TimeRange.CUSTOM) {
            // Component state time range duration is already of custom type, do nothing
            return;
        }

        // Start with setting TimeRangeType.TODAY as custom time range duration
        const customTimeRangeDuration = getTimeRangeDuration(TimeRange.TODAY);
        customTimeRangeDuration.timeRange = TimeRange.CUSTOM;

        // Update component state time range duration
        setTimeRangeDuration(customTimeRangeDuration);
    };

    const closeTimeRangeSelector = (): void => {
        setTimeRangeSelectorAnchorElement(null);
    };

    return (
        <Grid container alignItems="center">
            {/* Time range */}
            <Grid item>
                {(timeRangeDuration.timeRange === TimeRange.CUSTOM &&
                    // Custom time range, render duration
                    t("label.start-time-end-time", {
                        startTime: formatLongDateAndTime(
                            timeRangeDuration.startTime
                        ),
                        endTime: formatLongDateAndTime(
                            timeRangeDuration.endTime
                        ),
                    })) ||
                    (timeRangeDuration.timeRange &&
                        // Predefined time range, render name
                        t(
                            `label.${kebabCase(
                                TimeRange[
                                    timeRangeDuration.timeRange as TimeRange
                                ]
                            )}`
                        ))}
            </Grid>

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
                    <Grid
                        container
                        className={
                            timeRangeSelectorClasses.timeRangeSelectorContainer
                        }
                    >
                        <Grid item>
                            {/* Render predefined time range names */}
                            <ButtonGroup
                                color="primary"
                                orientation="vertical"
                                size="large"
                                variant="text"
                            >
                                {Object.values(TimeRange)
                                    // Iterate through available TimeRange values
                                    .filter(
                                        // Filter string values
                                        (timeRange) =>
                                            typeof timeRange === "string"
                                    )
                                    .map((timeRange) => (
                                        <Button
                                            className={
                                                TimeRange[
                                                    timeRange as keyof typeof TimeRange
                                                ] ===
                                                timeRangeDuration.timeRange
                                                    ? timeRangeSelectorClasses.selectedTimeRange
                                                    : ""
                                            }
                                            key={timeRange}
                                            onClick={(): void => {
                                                onPredefinedTimeRangeClick(
                                                    TimeRange[
                                                        timeRange as keyof typeof TimeRange
                                                    ]
                                                );
                                            }}
                                        >
                                            {t(
                                                `label.${kebabCase(
                                                    timeRange as string
                                                )}`
                                            )}
                                        </Button>
                                    ))}
                            </ButtonGroup>
                        </Grid>

                        <Grid item>
                            <Grid container item direction="column">
                                {/* Calendars */}
                                <Grid container item>
                                    <MuiPickersUtilsProvider utils={LuxonUtils}>
                                        <Grid item>
                                            <DateTimePicker
                                                disableFuture
                                                hideTabs
                                                value={
                                                    new Date(
                                                        timeRangeDuration.startTime as number
                                                    )
                                                }
                                                variant="static"
                                                onChange={onStartDateChange}
                                            />
                                        </Grid>

                                        <Grid item>
                                            <DateTimePicker
                                                disableFuture
                                                hideTabs
                                                minDate={
                                                    new Date(
                                                        timeRangeDuration.startTime as number
                                                    )
                                                }
                                                value={
                                                    new Date(
                                                        timeRangeDuration.endTime as number
                                                    )
                                                }
                                                variant="static"
                                                onChange={onEndDateChange}
                                            />
                                        </Grid>
                                    </MuiPickersUtilsProvider>
                                </Grid>

                                <Grid container item>
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
                                    <Grid item>
                                        <Button
                                            color="primary"
                                            size="large"
                                            variant="outlined"
                                            onClick={onCancel}
                                        >
                                            {t("label.cancel")}
                                        </Button>
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Popover>
            </Grid>
        </Grid>
    );
};
