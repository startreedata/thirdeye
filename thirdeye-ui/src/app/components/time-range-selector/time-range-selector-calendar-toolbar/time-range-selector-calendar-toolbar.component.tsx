import { Link, Toolbar } from "@material-ui/core";
import { ToolbarComponentProps } from "@material-ui/pickers/Picker/Picker";
import classnames from "classnames";
import { DateTime } from "luxon";
import React, { FunctionComponent } from "react";
import {
    formatDay,
    formatHour,
    formatMeridiem,
    formatMinute,
    formatMonth,
    formatYear,
    switchMeridiem,
} from "../../../utils/date-time-util/date-time-util";
import { useTimeRangeSelectorCalendarToolbarStyles } from "./time-range-selector-calendar-toolbar.styles";

export const TimeRangeSelectorCalendarToolbar: FunctionComponent<ToolbarComponentProps> = (
    props: ToolbarComponentProps
) => {
    const timeRangeSelectorCalendarToolbarClasses = useTimeRangeSelectorCalendarToolbarStyles();

    const onMonthClick = (): void => {
        props.setOpenView && props.setOpenView("month");
    };

    const onDayClick = (): void => {
        props.setOpenView && props.setOpenView("date");
    };

    const onYearClick = (): void => {
        props.setOpenView && props.setOpenView("year");
    };

    const onHourClick = (): void => {
        props.setOpenView && props.setOpenView("hours");
    };

    const onMinuteClick = (): void => {
        props.setOpenView && props.setOpenView("minutes");
    };

    const onMeridiemClick = (): void => {
        props.date &&
            props.onChange &&
            props.onChange(
                DateTime.fromMillis(switchMeridiem(props.date.toMillis()))
            );
    };

    return (
        <Toolbar
            classes={{ dense: timeRangeSelectorCalendarToolbarClasses.dense }}
            variant="dense"
        >
            {/* Month */}
            <Link
                className={timeRangeSelectorCalendarToolbarClasses.month}
                component="button"
                variant="subtitle1"
                onClick={onMonthClick}
            >
                {props.date && formatMonth(props.date.toMillis())}
            </Link>

            {/* Day */}
            <Link
                className={timeRangeSelectorCalendarToolbarClasses.link}
                component="button"
                variant="subtitle1"
                onClick={onDayClick}
            >
                {props.date && formatDay(props.date.toMillis())}
            </Link>

            {/* Year */}
            <Link
                className={timeRangeSelectorCalendarToolbarClasses.link}
                component="button"
                variant="subtitle1"
                onClick={onYearClick}
            >
                {props.date && formatYear(props.date.toMillis())}
            </Link>

            {/* Hour */}
            <Link
                className={classnames(
                    timeRangeSelectorCalendarToolbarClasses.rightAlign,
                    timeRangeSelectorCalendarToolbarClasses.link
                )}
                component="button"
                variant="subtitle1"
                onClick={onHourClick}
            >
                {props.date && formatHour(props.date.toMillis())}
            </Link>

            {/* Minute */}
            <Link
                className={timeRangeSelectorCalendarToolbarClasses.link}
                component="button"
                variant="subtitle1"
                onClick={onMinuteClick}
            >
                {props.date && formatMinute(props.date.toMillis())}
            </Link>

            {/* Meridiem */}
            <Link
                className={timeRangeSelectorCalendarToolbarClasses.meridiem}
                component="button"
                variant="subtitle1"
                onClick={onMeridiemClick}
            >
                {props.date && formatMeridiem(props.date.toMillis())}
            </Link>
        </Toolbar>
    );
};
