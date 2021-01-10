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
import { useDateTimePickerToolbarStyles } from "./date-time-picker-toolbar.styles";

export const DateTimePickerToolbar: FunctionComponent<ToolbarComponentProps> = (
    props: ToolbarComponentProps
) => {
    const dateTimePickerToolbarClasses = useDateTimePickerToolbarStyles();

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
            className={dateTimePickerToolbarClasses.container}
            classes={{ dense: dateTimePickerToolbarClasses.dense }}
            variant="dense"
        >
            {/* Month */}
            <Link
                className={classnames(
                    dateTimePickerToolbarClasses.link,
                    props.openView === "month"
                        ? dateTimePickerToolbarClasses.selectedLink
                        : ""
                )}
                component="button"
                variant="subtitle1"
                onClick={onMonthClick}
            >
                {props.date && formatMonth(props.date.toMillis())}
            </Link>

            {/* Day */}
            <Link
                className={classnames(
                    dateTimePickerToolbarClasses.link,
                    props.openView === "date"
                        ? dateTimePickerToolbarClasses.selectedLink
                        : ""
                )}
                component="button"
                variant="subtitle1"
                onClick={onDayClick}
            >
                {props.date && formatDay(props.date.toMillis())}
            </Link>

            {/* Year */}
            <Link
                className={classnames(
                    dateTimePickerToolbarClasses.link,
                    props.openView === "year"
                        ? dateTimePickerToolbarClasses.selectedLink
                        : ""
                )}
                component="button"
                variant="subtitle1"
                onClick={onYearClick}
            >
                {props.date && formatYear(props.date.toMillis())}
            </Link>

            {/* Hour */}
            <Link
                className={classnames(
                    dateTimePickerToolbarClasses.rightAlign,
                    dateTimePickerToolbarClasses.link,
                    props.openView === "hours"
                        ? dateTimePickerToolbarClasses.selectedLink
                        : ""
                )}
                component="button"
                variant="subtitle1"
                onClick={onHourClick}
            >
                {props.date && formatHour(props.date.toMillis())}
            </Link>

            {/* Minute */}
            <Link
                className={classnames(
                    dateTimePickerToolbarClasses.link,
                    props.openView === "minutes"
                        ? dateTimePickerToolbarClasses.selectedLink
                        : ""
                )}
                component="button"
                variant="subtitle1"
                onClick={onMinuteClick}
            >
                {props.date && formatMinute(props.date.toMillis())}
            </Link>

            {/* Meridiem */}
            <Link
                className={dateTimePickerToolbarClasses.link}
                component="button"
                variant="subtitle1"
                onClick={onMeridiemClick}
            >
                {props.date && formatMeridiem(props.date.toMillis())}
            </Link>
        </Toolbar>
    );
};
