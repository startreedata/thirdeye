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
import {
    Box,
    Checkbox,
    FormControlLabel,
    FormHelperText,
    Grid,
    MenuItem,
    TextField,
} from "@material-ui/core";
import { capitalize } from "lodash";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { InputSection } from "../../../../form-basics/input-section/input-section.component";
import { AlertDateTimeCronSimpleProps } from "./alert-date-time-cron-simple.interfaces";
import {
    buildCronString,
    DAY_STRING_TO_IDX,
    generateDayOfWeekArray,
    parseCronString,
    TimeOfDay,
} from "./alert-date-time-cron-simple.utils";

export const AlertDateTimeCronSimple: FunctionComponent<
    AlertDateTimeCronSimpleProps
> = ({ value, onChange }: AlertDateTimeCronSimpleProps) => {
    const { t } = useTranslation();

    const [minute, setMinute] = useState<number>(
        parseCronString(value).minute || 0
    );

    // Hour will always be 0 - 23
    const [hour, setHour] = useState<number>(parseCronString(value).hour || 5);

    // Display hour will be from 1 - 12
    const [displayHour, setDisplayHour] = useState<number>(() => {
        const parsedHour = parseCronString(value).hour;

        if (parsedHour) {
            return parsedHour % 12 === 0 ? 12 : parsedHour % 12;
        }

        // Default to 6am
        return 5;
    });

    const [dayOfWeek, setDayOfWeek] = useState<Array<boolean>>(
        () =>
            parseCronString(value).dayOfWeek ||
            generateDayOfWeekArray(true, { SUN: false, SAT: false })
    );

    const [timeOfDay, setTimeOfDay] = useState<TimeOfDay>(() => {
        const parsedHour = parseCronString(value).hour;

        if (parsedHour && parsedHour > 11) {
            return TimeOfDay.PM;
        }

        return TimeOfDay.AM;
    });

    const handleHourChange = (newHour: number): void => {
        let normalizedHour = newHour % 12;

        if (timeOfDay === TimeOfDay.PM) {
            normalizedHour = newHour + 12;
        }

        setHour(normalizedHour);
        setDisplayHour(normalizedHour % 12 === 0 ? 12 : normalizedHour % 12);
        onChange(buildCronString({ minute, hour: normalizedHour, dayOfWeek }));
    };
    const handleMinuteChange = (newMinute: number): void => {
        const normalized = newMinute % 60;
        setMinute(normalized);
        onChange(buildCronString({ minute: normalized, hour, dayOfWeek }));
    };
    const handleTimeOfDayChange = (newTimeOfDay: TimeOfDay): void => {
        let normalizedHour = hour;

        if (newTimeOfDay !== timeOfDay) {
            if (newTimeOfDay === TimeOfDay.AM) {
                normalizedHour = normalizedHour - 12;
            } else {
                normalizedHour = normalizedHour + 12;
            }
        }

        setTimeOfDay(newTimeOfDay);
        setHour(normalizedHour);
        onChange(buildCronString({ minute, hour: normalizedHour, dayOfWeek }));
    };
    const handleDayOfWeekChange = (idx: number): void => {
        setDayOfWeek((current) => {
            const cloned = [...current];

            cloned[idx] = !current[idx];

            onChange(buildCronString({ minute, hour, dayOfWeek: cloned }));

            return cloned;
        });
    };

    return (
        <>
            <InputSection
                inputComponent={
                    <>
                        {DAY_STRING_TO_IDX.map((dayString, idx) => {
                            return (
                                <FormControlLabel
                                    control={
                                        <Checkbox
                                            checked={dayOfWeek[idx]}
                                            color="primary"
                                            name={capitalize(dayString)}
                                            onChange={() => {
                                                handleDayOfWeekChange(idx);
                                            }}
                                        />
                                    }
                                    key={dayString}
                                    label={capitalize(dayString)}
                                />
                            );
                        })}
                    </>
                }
                label={t("message.select-days-of-week")}
            />

            <InputSection
                inputComponent={
                    <Grid container>
                        <Grid item xs={4}>
                            <TextField
                                fullWidth
                                type="number"
                                value={displayHour}
                                variant="outlined"
                                onChange={(e) => {
                                    handleHourChange(
                                        Number(e.currentTarget.value)
                                    );
                                }}
                            />
                            <FormHelperText>{t("label.hour")}</FormHelperText>
                        </Grid>
                        <Grid item xs={4}>
                            <TextField
                                fullWidth
                                type="number"
                                value={minute}
                                variant="outlined"
                                onChange={(e) => {
                                    handleMinuteChange(
                                        Number(e.currentTarget.value)
                                    );
                                }}
                            />
                            <FormHelperText>{t("label.minute")}</FormHelperText>
                        </Grid>
                        <Grid item xs={4}>
                            <TextField
                                fullWidth
                                select
                                id="interval-select"
                                value={timeOfDay}
                                variant="outlined"
                                onChange={(e) => {
                                    handleTimeOfDayChange(
                                        e.target.value as TimeOfDay
                                    );
                                }}
                            >
                                <MenuItem value={TimeOfDay.AM}>
                                    {TimeOfDay.AM}
                                </MenuItem>
                                <MenuItem value={TimeOfDay.PM}>
                                    {TimeOfDay.PM}
                                </MenuItem>
                            </TextField>
                        </Grid>
                    </Grid>
                }
                labelComponent={
                    <Box paddingY={1}>
                        <label>{t("label.time")}</label>
                    </Box>
                }
            />
        </>
    );
};
