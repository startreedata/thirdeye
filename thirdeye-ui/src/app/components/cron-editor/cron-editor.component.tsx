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
    FormHelperText,
    Grid,
    MenuItem,
    TextField,
} from "@material-ui/core";
import React, {
    ChangeEvent,
    FunctionComponent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import {
    ClockTimeOptions,
    convertTime12to24,
    CronRepeatOptions,
    getCronHour,
    getCronValue,
    getHourFormat,
} from "../../utils/cron-expression/cron-expression.util";
import { InputSection } from "../form-basics/input-section/input-section.component";
import { CronEditorProps } from "./cron-editor.interfaces";

export const CronEditor: FunctionComponent<CronEditorProps> = ({
    value,
    onChange,
}: CronEditorProps) => {
    const { t } = useTranslation();

    const [minute, setMinute] = useState<number>(() =>
        getCronValue(value, "minute")
    );
    const [hour, setHour] = useState<number>(() => getCronHour(value));
    const [day, setDay] = useState<number>(() => getCronValue(value, "day"));
    const [month, setMonth] = useState<number>(() =>
        getCronValue(value, "month")
    );
    const [year, setYear] = useState<number>(() => getCronValue(value, "year"));

    const [intervalValue, setIntervalValue] = useState<number>(0);
    const [intervalType, setIntervalType] = useState<string>("day");
    const [clockValue, setClockValue] = useState<string>(() =>
        getHourFormat(value)
    );

    useEffect(() => {
        setValues(intervalValue);
    }, [intervalValue]);

    useEffect(() => {
        setIntervalValue(getIntervalValue());
    }, [intervalType]);

    useEffect(() => {
        const hour24Format = convertTime12to24(hour, clockValue);
        onChange &&
            onChange(
                `0 ${minute || 0} ${hour24Format || 0} ${
                    day ? `1/${day}` : "1/1"
                } ${month ? `1/${month}` : "*"} ? ${
                    year ? `${new Date().getFullYear()}/${year}` : "*"
                }`
            );
    }, [minute, hour, day, month, year, clockValue]);

    const handleMinuteUpdate = (event: ChangeEvent<HTMLInputElement>): void => {
        let inputMinute = +event.target.value;
        if (inputMinute >= 59) {
            inputMinute = 59;
        }
        setMinute(inputMinute);
    };

    const handleHourUpdate = (event: ChangeEvent<HTMLInputElement>): void => {
        let inputHours = +event.target.value;

        if (inputHours >= 11 && clockValue === "am") {
            inputHours = 11;
        }
        if ((inputHours >= 12 || inputHours <= 1) && clockValue === "pm") {
            inputHours = inputHours >= 12 ? 12 : 1;
        }

        setHour(inputHours);
    };

    const handleIntervalChange = (
        event: ChangeEvent<HTMLInputElement>
    ): void => {
        setIntervalValue(+event.target.value);
    };

    const handleIntervalTypeChange = (
        event: ChangeEvent<HTMLInputElement>
    ): void => {
        setIntervalType(event.target.value);
    };

    const handleClockTypeChange = (
        event: ChangeEvent<HTMLInputElement>
    ): void => {
        setClockValue(event.target.value);

        if (event.target.value === "am") {
            setHour(0);
        } else {
            setHour(1);
        }
    };

    const handleDayUpdate = (value: number): void => {
        setDay(value);
    };

    const handleMonthUpdate = (value: number): void => {
        setMonth(value);
    };

    const handleYearUpdate = (value: number): void => {
        setYear(value);
    };

    const getIntervalValue = (): number => {
        switch (intervalType) {
            case "day":
                return day;
            case "month":
                return month;
            case "year":
                return year;

            default:
                return 0;
        }
    };

    const setValues = (value: number): void => {
        switch (intervalType) {
            case "day":
                handleDayUpdate(value);

                break;
            case "month":
                handleMonthUpdate(value);

                break;
            case "year":
                handleYearUpdate(value);

                break;
        }
    };

    const generateSelectOptions = (min: number, max: number): JSX.Element[] => {
        const options: JSX.Element[] = [];
        for (let i = min; i <= max; i++) {
            options.push(
                <MenuItem key={i} value={i}>
                    {i}
                </MenuItem>
            );
        }

        return options;
    };

    const generateRepeatOption = (
        type: string
    ): JSX.Element[] | JSX.Element => {
        switch (type) {
            case "day":
                return generateSelectOptions(1, 31);
            case "month":
                return generateSelectOptions(1, 12);
            case "year":
                return generateSelectOptions(1, 129);
            default:
                return <></>;
        }
    };

    return (
        <>
            <InputSection
                inputComponent={
                    <Grid container>
                        <Grid item xs={6}>
                            <TextField
                                fullWidth
                                select
                                value={intervalValue}
                                variant="outlined"
                                onChange={handleIntervalChange}
                            >
                                {generateRepeatOption(intervalType)}
                            </TextField>
                        </Grid>
                        <Grid item xs={6}>
                            <TextField
                                fullWidth
                                select
                                id="interval-select"
                                value={intervalType}
                                variant="outlined"
                                onChange={handleIntervalTypeChange}
                            >
                                {CronRepeatOptions.map((option) => (
                                    <MenuItem key={option} value={option}>
                                        {t(`label.${option}`)}
                                    </MenuItem>
                                ))}
                            </TextField>
                        </Grid>
                    </Grid>
                }
                labelComponent={
                    <Box paddingY={1}>
                        <label>{t("label.repeat-every")}</label>
                    </Box>
                }
            />
            <InputSection
                inputComponent={
                    <Grid container>
                        <Grid item xs={4}>
                            <TextField
                                fullWidth
                                select
                                value={hour}
                                variant="outlined"
                                onChange={handleHourUpdate}
                            >
                                {generateSelectOptions(
                                    clockValue === "am" ? 0 : 1,
                                    clockValue === "am" ? 11 : 12
                                )}
                            </TextField>
                            <FormHelperText>{t("label.hour")}</FormHelperText>
                        </Grid>
                        <Grid item xs={4}>
                            <TextField
                                fullWidth
                                select
                                value={minute}
                                variant="outlined"
                                onChange={handleMinuteUpdate}
                            >
                                {generateSelectOptions(0, 59)}
                            </TextField>
                            <FormHelperText>{t("label.minute")}</FormHelperText>
                        </Grid>
                        <Grid item xs={4}>
                            <TextField
                                fullWidth
                                select
                                id="interval-select"
                                value={clockValue}
                                variant="outlined"
                                onChange={handleClockTypeChange}
                            >
                                {ClockTimeOptions.map((option) => (
                                    <MenuItem key={option} value={option}>
                                        {t(`label.${option}`)}
                                    </MenuItem>
                                ))}
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
