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
    FormControl,
    FormControlLabel,
    FormHelperText,
    Grid,
    MenuItem,
    Radio,
    RadioGroup,
    TextField,
    Typography,
} from "@material-ui/core";
import HelpOutlineIcon from "@material-ui/icons/HelpOutline";
import CronValidator from "cron-expression-validator";
import React, {
    ChangeEvent,
    FunctionComponent,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { TooltipV1 } from "../../platform/components";
import {
    ClockTimeOptions,
    convertTime12to24,
    CronRepeatOptions,
} from "../../utils/cron-expression/cron-expression.util";
import CronHelper from "../cron-helper/cron-helper.component";
import { CronEditorProps } from "./cron-editor.interfaces";
import { useCronEditorStyle } from "./cron-editor.style";

export const CronEditor: FunctionComponent<CronEditorProps> = ({
    value,
    onChange,
}: CronEditorProps) => {
    const { t } = useTranslation();
    const [cronInternal, setCronInternal] = useState(value ?? "");

    const [cronConfigTab, setCronConfigTab] = useState<string>(
        t("label.day-time")
    );

    const [dayTimeCron, setDayTimeCron] = useState<string>("");

    const [minute, setMinute] = useState<number>(0);
    const [hour, setHour] = useState<number>(0);
    const [day, setDay] = useState<string>("1");
    const [month, setMonth] = useState<string>("1");
    const [year, setYear] = useState<string>("");

    const [intervalValue, setIntervalValue] = useState<string>("");
    const [intervalType, setIntervalType] = useState<string>("day");
    const [clockValue, setClockValue] = useState<string>("am");

    const cronEditorStyle = useCronEditorStyle();

    // Avoid validation for predefined cron expressions
    const isCronValid = CronValidator.isValidCronExpression(
        cronInternal.trim()
    );

    const isDayTimeCronValid = CronValidator.isValidCronExpression(
        dayTimeCron.trim()
    );

    useEffect(() => {
        setValues(intervalValue);
    }, [intervalValue]);

    useEffect(() => {
        setIntervalValue(getIntervalValue());
    }, [intervalType]);

    useEffect(() => {
        const hour24Format = convertTime12to24(hour, clockValue);
        setDayTimeCron(
            `0 ${minute || 0} ${hour24Format || 0} ${
                day ? `1/${day}` : "1/1"
            } ${month ? `1/${month}` : "*"} ? ${
                year ? `${new Date().getFullYear()}/${year}` : "*"
            }`
        );
    }, [minute, hour, day, month, year, clockValue]);

    useEffect(() => {
        if (cronConfigTab === t("label.day-time")) {
            onChange && onChange(dayTimeCron);
        } else {
            onChange && onChange(cronInternal);
        }
    }, [cronConfigTab, cronInternal, dayTimeCron]);

    const handleMinuteUpdate = (event: ChangeEvent<HTMLInputElement>): void => {
        let inputMinute = +event.target.value;
        if (isNaN(inputMinute)) {
            return;
        }
        if (inputMinute >= 59) {
            inputMinute = 59;
        }
        setMinute(inputMinute);
    };

    const handleHourUpdate = (event: ChangeEvent<HTMLInputElement>): void => {
        let inputHours = +event.target.value;
        if (isNaN(inputHours)) {
            return;
        }

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
        if (!isNaN(+event.target.value)) {
            setIntervalValue(event.target.value);
        }
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

    const handleCronInputChange = (
        event: ChangeEvent<HTMLInputElement>
    ): void => {
        setCronInternal(event.target.value);
    };

    const handleDateTypeChange = (
        event: ChangeEvent<HTMLInputElement>
    ): void => {
        setCronConfigTab(event.target.value);
    };

    const handleDayUpdate = (value: string): void => {
        setDay(value);
    };

    const handleMonthUpdate = (value: string): void => {
        setMonth(value);
    };

    const handleYearUpdate = (value: string): void => {
        setYear(value);
    };

    const getIntervalValue = (): string => {
        switch (intervalType) {
            case "day":
                return day;
            case "month":
                return month;
            case "year":
                return year;

            default:
                return "";
        }
    };

    const setValues = (value: string): void => {
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

    return (
        <Grid container item alignItems="center" spacing={2}>
            <Grid item lg={2} md={4} sm={12} xs={12}>
                <Box alignItems="center" display="flex">
                    <Typography variant="body2">
                        {t("label.date-type")}&nbsp;
                    </Typography>
                    <TooltipV1 title={t("message.data-type-helper") as string}>
                        <HelpOutlineIcon color="secondary" fontSize="small" />
                    </TooltipV1>
                </Box>
            </Grid>
            <Grid item lg={10} md={8} sm={12} xs={12}>
                <FormControl component="fieldset">
                    <RadioGroup
                        row
                        aria-label="cron-radio-buttons"
                        name="cron-radio-buttons"
                        value={cronConfigTab}
                        onChange={handleDateTypeChange}
                    >
                        <FormControlLabel
                            control={<Radio />}
                            label={t("label.day-time")}
                            value={t("label.day-time")}
                        />
                        <FormControlLabel
                            control={<Radio />}
                            label={t("label.cron")}
                            value={t("label.cron")}
                        />
                    </RadioGroup>
                </FormControl>
            </Grid>

            {cronConfigTab === t("label.day-time") ? (
                <>
                    <Grid item lg={2} md={4} sm={12} xs={12}>
                        <Typography variant="body2">
                            {t("label.repeat-every")}
                        </Typography>
                    </Grid>
                    <Grid item lg={10} md={8} sm={12} xs={12}>
                        <Grid container>
                            <Grid item>
                                <TextField
                                    InputProps={{
                                        className:
                                            cronEditorStyle.cronInputField,
                                    }}
                                    type="text"
                                    value={intervalValue}
                                    variant="outlined"
                                    onChange={handleIntervalChange}
                                />
                            </Grid>
                            <Grid item>
                                <TextField
                                    select
                                    InputProps={{
                                        className:
                                            cronEditorStyle.repeatTypeField,
                                    }}
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
                    </Grid>
                    <Grid item lg={2} md={4} sm={12} xs={12}>
                        <Typography variant="body2">
                            {t("label.time")}
                        </Typography>
                    </Grid>
                    <Grid item lg={10} md={8} sm={12} xs={12}>
                        <Grid container>
                            <Grid item>
                                <TextField
                                    InputProps={{
                                        className:
                                            cronEditorStyle.cronInputField,
                                    }}
                                    type="text"
                                    value={hour}
                                    variant="outlined"
                                    onChange={handleHourUpdate}
                                />
                                <FormHelperText>
                                    {t("label.hour")}:
                                    {clockValue === "pm" ? " 1" : " 0"} -
                                    {clockValue === "am" ? " 11" : " 12"}
                                </FormHelperText>
                            </Grid>
                            <Grid item>
                                <TextField
                                    InputProps={{
                                        className:
                                            cronEditorStyle.cronInputField,
                                    }}
                                    type="text"
                                    value={minute}
                                    variant="outlined"
                                    onChange={handleMinuteUpdate}
                                />
                                <FormHelperText>
                                    {t("label.minute")}: 0 - 59
                                </FormHelperText>
                            </Grid>
                            <Grid item>
                                <TextField
                                    select
                                    InputProps={{
                                        className:
                                            cronEditorStyle.cronInputField,
                                    }}
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
                    </Grid>
                    <CronHelper
                        cron={dayTimeCron}
                        isCronValid={isDayTimeCronValid}
                    />
                </>
            ) : (
                <>
                    <Grid container>
                        <Grid item lg={2} md={4} sm={12} xs={12} />
                        <Grid item lg={3} md={8} sm={12} xs={12}>
                            <TextField
                                fullWidth
                                error={!isCronValid}
                                value={cronInternal}
                                variant="outlined"
                                onChange={handleCronInputChange}
                            />
                        </Grid>
                    </Grid>
                    <CronHelper cron={cronInternal} isCronValid={isCronValid} />
                </>
            )}
        </Grid>
    );
};
