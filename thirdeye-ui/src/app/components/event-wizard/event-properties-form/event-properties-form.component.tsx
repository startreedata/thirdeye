/*
 * Copyright 2023 StarTree Inc
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
import LuxonUtils from "@date-io/luxon";
import { Grid, TextField, Typography } from "@material-ui/core";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import React, { FunctionComponent } from "react";
import { Controller } from "react-hook-form";
import { useTranslation } from "react-i18next";
import { InputSection } from "../../form-basics/input-section/input-section.component";
import { DateTimePickerToolbar } from "../../time-range/time-range-selector/date-time-picker-toolbar/date-time-picker-toolbar.component";
import { EventPropertiesFormProps } from "./event-properties-form.interfaces";

export const EventPropertiesForm: FunctionComponent<EventPropertiesFormProps> =
    ({ formErrors, formControl, fullWidth }) => {
        const { t } = useTranslation();

        return (
            <Grid container>
                <Grid item data-testId="event-properties-title" xs={12}>
                    <Typography variant="h5">
                        {t("label.event-properties")}
                    </Typography>
                </Grid>
                <Grid
                    container
                    item
                    alignItems="center"
                    data-testId="event-properties-form"
                    xs={12}
                >
                    {/* Name input */}
                    <InputSection
                        fullWidth={fullWidth}
                        inputComponent={
                            <Controller
                                control={formControl}
                                name="name"
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        fullWidth
                                        required
                                        error={Boolean(
                                            formErrors && formErrors.name
                                        )}
                                        helperText={
                                            formErrors &&
                                            formErrors.name &&
                                            formErrors.name.message
                                        }
                                        name="name"
                                        placeholder={t(
                                            "label.enter-name-of-event"
                                        )}
                                        type="string"
                                        variant="outlined"
                                    />
                                )}
                            />
                        }
                        label={t("label.name")}
                    />

                    {/* Type label */}
                    <InputSection
                        fullWidth={fullWidth}
                        inputComponent={
                            <Controller
                                control={formControl}
                                name="type"
                                render={({ field }) => (
                                    <TextField
                                        {...field}
                                        fullWidth
                                        required
                                        name="type"
                                        placeholder={t(
                                            "label.enter-a-type-event"
                                        )}
                                        type="string"
                                        variant="outlined"
                                    />
                                )}
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
                                render={({ field }) => (
                                    <MuiPickersUtilsProvider utils={LuxonUtils}>
                                        <DateTimePicker
                                            autoOk
                                            fullWidth
                                            ToolbarComponent={
                                                DateTimePickerToolbar
                                            }
                                            error={Boolean(
                                                formErrors.startTime
                                            )}
                                            helperText={
                                                formErrors.startTime &&
                                                formErrors.startTime.message
                                            }
                                            value={new Date(field.value)}
                                            variant="inline"
                                            onChange={(e) => {
                                                field.onChange(e?.valueOf());
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
                                render={({ field }) => (
                                    <MuiPickersUtilsProvider utils={LuxonUtils}>
                                        <DateTimePicker
                                            autoOk
                                            fullWidth
                                            ToolbarComponent={
                                                DateTimePickerToolbar
                                            }
                                            error={Boolean(formErrors.endTime)}
                                            helperText={
                                                formErrors.endTime &&
                                                formErrors.endTime.message
                                            }
                                            value={new Date(field.value)}
                                            variant="inline"
                                            onChange={(e) =>
                                                field.onChange(e?.valueOf())
                                            }
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
