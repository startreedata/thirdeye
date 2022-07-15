/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Link, Toolbar } from "@material-ui/core";
import { ToolbarComponentProps } from "@material-ui/pickers/Picker/Picker";
import {
    formatDayV1,
    formatHourV1,
    formatMeridiemV1,
    formatMinuteV1,
    formatMonthV1,
    formatYearV1,
    switchMeridiemV1,
} from "@startree-ui/platform-ui";
import classnames from "classnames";
import { DateTime } from "luxon";
import React, { FunctionComponent } from "react";
import { useDateTimePickerToolbarStyles } from "./date-time-picker-toolbar.styles";

export const DateTimePickerToolbar: FunctionComponent<ToolbarComponentProps> = (
    props: ToolbarComponentProps
) => {
    const dateTimePickerToolbarClasses = useDateTimePickerToolbarStyles();

    const handleMonthClick = (): void => {
        props.setOpenView && props.setOpenView("month");
    };

    const handleDayClick = (): void => {
        props.setOpenView && props.setOpenView("date");
    };

    const handleYearClick = (): void => {
        props.setOpenView && props.setOpenView("year");
    };

    const handleHourClick = (): void => {
        props.setOpenView && props.setOpenView("hours");
    };

    const handleMinuteClick = (): void => {
        props.setOpenView && props.setOpenView("minutes");
    };

    const handleMeridiemClick = (): void => {
        if (!props.date) {
            return;
        }

        props.onChange &&
            props.onChange(
                DateTime.fromMillis(switchMeridiemV1(props.date.toMillis()))
            );
    };

    return (
        <Toolbar
            className={dateTimePickerToolbarClasses.dateTimePickerToolbar}
            classes={{
                dense: dateTimePickerToolbarClasses.toolbarDense,
            }}
            variant="dense"
        >
            {/* Month */}
            <Link
                className={classnames(dateTimePickerToolbarClasses.link, {
                    [dateTimePickerToolbarClasses.linkSelected]:
                        props.openView === "month",
                })}
                variant="subtitle1"
                onClick={handleMonthClick}
            >
                {props.date && formatMonthV1(props.date.toMillis())}
            </Link>

            {/* Day */}
            <Link
                className={classnames(dateTimePickerToolbarClasses.link, {
                    [dateTimePickerToolbarClasses.linkSelected]:
                        props.openView === "date",
                })}
                variant="subtitle1"
                onClick={handleDayClick}
            >
                {props.date && formatDayV1(props.date.toMillis())}
            </Link>

            {/* Year */}
            <Link
                className={classnames(dateTimePickerToolbarClasses.link, {
                    [dateTimePickerToolbarClasses.linkSelected]:
                        props.openView === "year",
                })}
                variant="subtitle1"
                onClick={handleYearClick}
            >
                {props.date && formatYearV1(props.date.toMillis())}
            </Link>

            {/* Hour */}
            <Link
                className={classnames(
                    dateTimePickerToolbarClasses.link,
                    dateTimePickerToolbarClasses.linkRightAligned,
                    {
                        [dateTimePickerToolbarClasses.linkSelected]:
                            props.openView === "hours",
                    }
                )}
                variant="subtitle1"
                onClick={handleHourClick}
            >
                {props.date && formatHourV1(props.date.toMillis())}
            </Link>

            {/* Minute */}
            <Link
                className={classnames(dateTimePickerToolbarClasses.link, {
                    [dateTimePickerToolbarClasses.linkSelected]:
                        props.openView === "minutes",
                })}
                variant="subtitle1"
                onClick={handleMinuteClick}
            >
                {props.date && formatMinuteV1(props.date.toMillis())}
            </Link>

            {/* Meridiem */}
            <Link
                className={dateTimePickerToolbarClasses.link}
                variant="subtitle1"
                onClick={handleMeridiemClick}
            >
                {props.date && formatMeridiemV1(props.date.toMillis())}
            </Link>
        </Toolbar>
    );
};
