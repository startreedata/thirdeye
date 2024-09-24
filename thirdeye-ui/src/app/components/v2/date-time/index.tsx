/*
 * Copyright 2024 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import React, { useState } from "react";
import { LocalizationProvider } from "@mui/x-date-pickers";
import { AdapterLuxon } from "@mui/x-date-pickers/AdapterLuxon";
import { StaticDateTimePicker } from "@mui/x-date-pickers/StaticDateTimePicker";
import { DateTime } from "luxon";
import { TimePicker } from "@mui/x-date-pickers/TimePicker";

type DateTimeProps = {
    label: string;
    date: number;
    onDateTimeChange?: (date: DateTime | null) => void;
    disableFuture?: boolean;
    minDate?: number;
};

export const DateTimeUi = ({
    date,
    onDateTimeChange,
    disableFuture = true,
}: DateTimeProps): JSX.Element => {
    const [view, setView] = useState<any>("day");

    const ActionButton = (): JSX.Element => {
        return (
            <TimePicker
                ampm={false}
                label="Time(00:00-23:59)"
                value={DateTime.fromMillis(date)}
                onChange={onDateTimeChange}
            />
        );
    };
    const onViewChange = (v: string): void => {
        if (v === "year") {
            setView(v);
        } else {
            setView("day");
        }
    };

    return (
        <LocalizationProvider dateAdapter={AdapterLuxon}>
            <StaticDateTimePicker
                ampm={false}
                disableFuture={disableFuture}
                displayStaticWrapperAs="desktop"
                slots={{
                    actionBar: ActionButton,
                }}
                sx={{
                    "& .MuiTabs-root": { display: "none" },
                    "& .MuiPickersLayout-contentWrapper": {
                        display: "flex",
                        gridRow: "unset",
                        gridColumn: "unset",
                    },
                    "& .MuiTextField-root": { width: "40%", margin: "auto" },
                    "& .MuiOutlinedInput-root": { height: "40px" },
                }} // Use 24-hour format (digital clock)
                value={DateTime.fromMillis(date)} // Ensures the tabs are hidden
                view={view}
                onChange={onDateTimeChange}
                onViewChange={onViewChange}
            />
        </LocalizationProvider>
    );
};
