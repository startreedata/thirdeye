import LuxonUtils from "@date-io/luxon";
import { Grid, TextField, Typography } from "@material-ui/core";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import React, { FunctionComponent } from "react";
import { Controller } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { DateTimePickerToolbar } from "../../time-range/time-range-selector/date-time-picker-toolbar/date-time-picker-toolbar.component";
import { EventPropertiesFormProps } from "./event-properties-form.interfaces";

export const EventPropertiesForm: FunctionComponent<
    EventPropertiesFormProps
> = ({ formRegister, formErrors, formControl, fullWidth }) => {
    const { t } = useTranslation();

    return (
        <Grid container>
            <Grid item xs={12}>
                <Typography variant="h5">
                    {t("label.event-properties")}
                </Typography>
            </Grid>
            <Grid container item alignItems="center" xs={12}>
                {/* Name input */}
                <InputSection
                    fullWidth={fullWidth}
                    inputComponent={
                        <TextField
                            fullWidth
                            required
                            error={Boolean(formErrors && formErrors.name)}
                            helperText={
                                formErrors &&
                                formErrors.name &&
                                formErrors.name.message
                            }
                            inputRef={formRegister}
                            name="name"
                            placeholder={t("label.enter-name-of-event")}
                            type="string"
                            variant="outlined"
                        />
                    }
                    label={t("label.name")}
                />

                {/* Type label */}
                <InputSection
                    fullWidth={fullWidth}
                    inputComponent={
                        <TextField
                            fullWidth
                            required
                            inputRef={formRegister}
                            name="type"
                            placeholder={t("label.enter-a-type-event")}
                            type="string"
                            variant="outlined"
                        />
                    }
                    label={t("label.type")}
                />

                {/* Start time */}
                <InputSection
                    fullWidth={fullWidth}
                    inputComponent={
                        <Controller
                            control={formControl}
                            name="startTime"
                            render={({ onChange, value }) => (
                                <MuiPickersUtilsProvider utils={LuxonUtils}>
                                    <DateTimePicker
                                        autoOk
                                        fullWidth
                                        ToolbarComponent={DateTimePickerToolbar}
                                        error={Boolean(formErrors.startTime)}
                                        helperText={
                                            formErrors.startTime &&
                                            formErrors.startTime.message
                                        }
                                        value={new Date(value)}
                                        variant="inline"
                                        onChange={(e) => {
                                            onChange(e?.valueOf());
                                        }}
                                    />
                                </MuiPickersUtilsProvider>
                            )}
                        />
                    }
                    label={t("label.start-time")}
                />

                {/* End time */}
                <InputSection
                    fullWidth={fullWidth}
                    inputComponent={
                        <Controller
                            control={formControl}
                            name="endTime"
                            render={({ onChange, value }) => (
                                <MuiPickersUtilsProvider utils={LuxonUtils}>
                                    <DateTimePicker
                                        autoOk
                                        fullWidth
                                        ToolbarComponent={DateTimePickerToolbar}
                                        error={Boolean(formErrors.endTime)}
                                        helperText={
                                            formErrors.endTime &&
                                            formErrors.endTime.message
                                        }
                                        value={new Date(value)}
                                        variant="inline"
                                        onChange={(e) => onChange(e?.valueOf())}
                                    />
                                </MuiPickersUtilsProvider>
                            )}
                        />
                    }
                    label={t("label.end-time")}
                />
            </Grid>
        </Grid>
    );
};
