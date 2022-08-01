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
    Button,
    FormHelperText,
    Grid,
    InputLabel,
    Link,
    TextField,
    Typography,
} from "@material-ui/core";
import CronValidator from "cron-expression-validator";
import cronstrue from "cronstrue";
import React, { ChangeEvent, FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { QuickScheduleOptions } from "../../utils/cron-expression/cron-expression.util";
import {
    CronEditorProps,
    QuickScheduleOption,
    QuickScheduleOptionKeys,
} from "./cron-editor.interfaces";

export const CronEditor: FunctionComponent<CronEditorProps> = ({
    value,
    onChange,
    hideQuickOptions,
    label,
}: CronEditorProps) => {
    const { t } = useTranslation();
    const [cronInternal, setCronInternal] = useState(value ?? "");
    const [quickSchedule, setQuickSchedule] =
        useState<QuickScheduleOptionKeys>();

    const atoms = cronInternal.trim().split(" ");
    const [_seconds, _minute, _hour, _day, _month, _week, _year] = atoms;
    const [seconds, setSeconds] = useState<string>(_seconds ?? "");
    const [minute, setMinute] = useState<string>(_minute ?? "");
    const [hour, setHour] = useState<string>(_hour ?? "");
    const [day, setDay] = useState<string>(_day ?? "");
    const [month, setMonth] = useState<string>(_month ?? "");
    const [week, setWeek] = useState<string>(_week ?? "");
    // optional
    const [year, setYear] = useState<string>(_year ?? "");

    // Avoid validation for predefined cron expressions
    const isCronValid =
        Boolean(quickSchedule) ||
        CronValidator.isValidCronExpression(cronInternal.trim());

    const handleMinuteUpdate = (event: ChangeEvent<HTMLInputElement>): void => {
        setMinute(event.target.value);
        setCronInternal(
            `${seconds} ${
                event.target.value ?? ""
            } ${hour} ${day} ${month} ${week} ${year ?? ""}`
        );
    };

    const handleHourUpdate = (event: ChangeEvent<HTMLInputElement>): void => {
        setHour(event.target.value);
        setCronInternal(
            `${seconds} ${minute} ${
                event.target.value ?? ""
            } ${day} ${month} ${week} ${year ?? ""}`
        );
    };

    const handleDayUpdate = (event: ChangeEvent<HTMLInputElement>): void => {
        setDay(event.target.value);
        setCronInternal(
            `${seconds} ${minute} ${hour} ${
                event.target.value ?? ""
            } ${month} ${week} ${year ?? ""}`
        );
    };

    const handleMonthUpdate = (event: ChangeEvent<HTMLInputElement>): void => {
        setMonth(event.target.value);
        setCronInternal(
            `${seconds} ${minute} ${hour} ${day} ${
                event.target.value ?? ""
            } ${week} ${year ?? ""}`
        );
    };

    const handleWeekUpdate = (event: ChangeEvent<HTMLInputElement>): void => {
        setWeek(event.target.value);
        setCronInternal(
            `${seconds} ${minute} ${hour} ${day} ${month} ${
                event.target.value ?? ""
            } ${year ?? ""}`
        );
    };

    const handleSecondsUpdate = (
        event: ChangeEvent<HTMLInputElement>
    ): void => {
        setSeconds(event.target.value);
        setCronInternal(
            `${
                event.target.value ?? ""
            } ${minute} ${hour} ${day} ${month} ${week} ${year ?? ""}`
        );
    };

    const handleYearUpdate = (event: ChangeEvent<HTMLInputElement>): void => {
        setYear(event.target.value);
        setCronInternal(
            `${seconds} ${minute} ${hour} ${day} ${month} ${week} ${
                event.target.value ?? ""
            }`
        );
    };

    const handleQuickScheduleChange = (cron: QuickScheduleOption): void => {
        onChange && onChange(cron.value);
        setQuickSchedule(cron.key);
        setCronInternal(cron.value);
    };

    return (
        <Grid container item spacing={2}>
            <Grid item lg={2} md={4} sm={12} xs={12}>
                <InputLabel
                    shrink
                    data-testid="cron-input-label"
                    error={!isCronValid}
                >
                    {label ?? t("label.cron")}
                </InputLabel>
                <Typography variant="caption">
                    <Link
                        href="http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html"
                        target="_blank"
                    >
                        {t("label.documentation")}
                    </Link>
                </Typography>
            </Grid>
            {!hideQuickOptions && (
                <Grid item lg={10} md={8} sm={12} xs={12}>
                    <Grid container>
                        {QuickScheduleOptions.map(({ key, label, value }) => (
                            <Grid item key={key}>
                                <Button
                                    color="primary"
                                    variant={
                                        quickSchedule === key
                                            ? "contained"
                                            : "outlined"
                                    }
                                    onClick={() => {
                                        handleQuickScheduleChange({
                                            label,
                                            key,
                                            value,
                                        });
                                    }}
                                >
                                    {label}
                                </Button>
                            </Grid>
                        ))}
                    </Grid>
                </Grid>
            )}
            <Grid item lg={2} md={4} sm={12} xs={12} />
            <Grid item lg={10} md={8} sm={12} xs={12}>
                <Grid container>
                    <Grid item>
                        <TextField
                            name="seconds"
                            placeholder={t("label.seconds")}
                            value={seconds}
                            onChange={handleSecondsUpdate}
                        />
                    </Grid>
                    <Grid item>
                        <TextField
                            name="minute"
                            placeholder={t("label.minute")}
                            value={minute}
                            onChange={handleMinuteUpdate}
                        />
                    </Grid>
                    <Grid item>
                        <TextField
                            name="hour"
                            placeholder={t("label.hour")}
                            value={hour}
                            onChange={handleHourUpdate}
                        />
                    </Grid>
                    <Grid item>
                        <TextField
                            name="day"
                            placeholder={t("label.day")}
                            value={day}
                            onChange={handleDayUpdate}
                        />
                    </Grid>
                    <Grid item>
                        <TextField
                            name="month"
                            placeholder={t("label.month")}
                            value={month}
                            onChange={handleMonthUpdate}
                        />
                    </Grid>
                    <Grid item>
                        <TextField
                            name="week"
                            placeholder={t("label.week")}
                            value={week}
                            variant="outlined"
                            onChange={handleWeekUpdate}
                        />
                    </Grid>
                    <Grid item>
                        <TextField
                            name="year"
                            placeholder={t("label.year")}
                            value={year}
                            variant="outlined"
                            onChange={handleYearUpdate}
                        />
                    </Grid>
                </Grid>
            </Grid>
            <Grid item lg={2} md={4} sm={12} xs={12} />
            <Grid item lg={10} md={8} sm={12} xs={12}>
                {isCronValid && (
                    <FormHelperText>
                        {cronstrue.toString(cronInternal, {
                            verbose: true,
                        })}
                    </FormHelperText>
                )}

                {/* If there are errors, render them */}
                {!isCronValid && (
                    <FormHelperText error data-testid="error-message-container">
                        {t("message.invalid-cron-input-1")}
                        <Link
                            href="http://www.quartz-scheduler.org/documentation/quartz-2.3.0/tutorials/crontrigger.html"
                            target="_blank"
                        >
                            {t("label.cron-documentation")}
                        </Link>
                        {t("message.invalid-cron-input-2")}
                    </FormHelperText>
                )}
            </Grid>
        </Grid>
    );
};
