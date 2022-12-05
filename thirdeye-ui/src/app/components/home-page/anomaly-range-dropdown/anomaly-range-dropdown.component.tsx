/*
 * Copyright 2022 StarTree Inc
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

import { MenuItem, Paper, Select } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import {
    generateDateRangeDaysFromNow,
    generateDateRangeMonthsFromNow,
} from "../../../utils/routes/routes.util";
import type { AnomalyRangeDropdownProps } from "./anomaly-range-dropdown.interface";

export const AnomalyRangeDropdown: FunctionComponent<AnomalyRangeDropdownProps> =
    ({ anomalyStartTime, setAnomalyStartTime }) => {
        const handleChange = (
            e: React.ChangeEvent<{ name?: string; value: number | unknown }>
        ): void => {
            setAnomalyStartTime(e.target.value as number);
        };

        const options = [
            { label: "Last 7 days", value: generateDateRangeDaysFromNow(7)[0] },
            {
                label: "Last 6 months",
                value: generateDateRangeMonthsFromNow(6)[0],
            },
            { label: "All", value: generateDateRangeMonthsFromNow(60)[0] },
        ];

        return (
            <Paper elevation={0}>
                <Select
                    fullWidth
                    value={anomalyStartTime}
                    variant="outlined"
                    onChange={handleChange}
                >
                    {options.map(({ label, value }) => (
                        <MenuItem key={label} value={value}>
                            {label}
                        </MenuItem>
                    ))}
                </Select>
            </Paper>
        );
    };
