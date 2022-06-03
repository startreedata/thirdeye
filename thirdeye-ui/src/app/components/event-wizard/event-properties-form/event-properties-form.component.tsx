import LuxonUtils from "@date-io/luxon";
import { yupResolver } from "@hookform/resolvers/yup";
import { Grid, TextField, Typography } from "@material-ui/core";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import React, { FunctionComponent } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { Event } from "../../../rest/dto/event.interfaces";
import { createEmptyEvent } from "../../../utils/events/events.util";
import { DateTimePickerToolbar } from "../../time-range/time-range-selector/date-time-picker-toolbar/date-time-picker-toolbar.component";
import { EventPropertiesFormProps } from "./event-properties-form.interfaces";
import { useEventPropertiesFormStyles } from "./event-properties-form.styles";

export const EventPropertiesForm: FunctionComponent<
    EventPropertiesFormProps
> = ({ event, id, onSubmit }: EventPropertiesFormProps) => {
    const eventPropertiesFormStyles = useEventPropertiesFormStyles();

    const { t } = useTranslation();
    const defaultValues = event || createEmptyEvent();

    const { register, handleSubmit, errors, control } = useForm<Event>({
        defaultValues,
        resolver: yupResolver(
            yup.object().shape({
                name: yup.string().required(t("message.event-name-required")),
                type: yup.string(),
                startTime: yup
                    .number()
                    .required(t("message.event-start-time-required")),
                endTime: yup
                    .number()
                    .required(t("message.event-end-time-required")),
            })
        ),
    });

    const onSubmitEventPropertiesForm = (event: Event): void => {
        onSubmit && onSubmit(event);
    };

    return (
        <form
            noValidate
            id={id}
            onSubmit={handleSubmit(onSubmitEventPropertiesForm)}
        >
            <Grid container alignItems="center">
                {/* Name label */}
                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.name")}
                    </Typography>
                </Grid>

                {/* Name input */}
                <Grid item lg={4} md={5} sm={6} xs={12}>
                    <TextField
                        fullWidth
                        required
                        error={Boolean(errors && errors.name)}
                        helperText={
                            errors && errors.name && errors.name.message
                        }
                        inputRef={register}
                        name="name"
                        placeholder={t("label.enter-name-of-event")}
                        type="string"
                        variant="outlined"
                    />
                </Grid>

                {/* Spacer */}
                <Grid item sm={12} />

                {/* Type label */}
                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.type")}
                    </Typography>
                </Grid>

                {/* Type input */}
                <Grid item lg={4} md={5} sm={6} xs={12}>
                    <TextField
                        fullWidth
                        required
                        inputRef={register}
                        name="type"
                        placeholder={t("label.enter-a-type-event")}
                        type="string"
                        variant="outlined"
                    />
                </Grid>

                {/* Spacer */}
                <Grid item sm={12} />

                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.start-time")}
                    </Typography>
                </Grid>

                <Grid
                    item
                    className={eventPropertiesFormStyles.datePickerContainer}
                    lg={4}
                    md={5}
                    sm={6}
                    xs={12}
                >
                    <Controller
                        control={control}
                        name="startTime"
                        render={({ onChange, value }) => (
                            <MuiPickersUtilsProvider utils={LuxonUtils}>
                                <DateTimePicker
                                    autoOk
                                    fullWidth
                                    ToolbarComponent={DateTimePickerToolbar}
                                    error={Boolean(errors.startTime)}
                                    helperText={
                                        errors.startTime &&
                                        errors.startTime.message
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
                </Grid>

                {/* Spacer */}
                <Grid item sm={12} />

                <Grid item lg={2} md={3} sm={5} xs={12}>
                    <Typography variant="subtitle2">
                        {t("label.end-time")}
                    </Typography>
                </Grid>

                <Grid
                    item
                    className={eventPropertiesFormStyles.datePickerContainer}
                    lg={4}
                    md={5}
                    sm={6}
                    xs={12}
                >
                    <Controller
                        control={control}
                        name="endTime"
                        render={({ onChange, value }) => (
                            <MuiPickersUtilsProvider utils={LuxonUtils}>
                                <DateTimePicker
                                    autoOk
                                    fullWidth
                                    ToolbarComponent={DateTimePickerToolbar}
                                    error={Boolean(errors.endTime)}
                                    helperText={
                                        errors.endTime && errors.endTime.message
                                    }
                                    value={new Date(value)}
                                    variant="inline"
                                    onChange={(e) => onChange(e?.valueOf())}
                                />
                            </MuiPickersUtilsProvider>
                        )}
                    />
                </Grid>
            </Grid>
        </form>
    );
};
