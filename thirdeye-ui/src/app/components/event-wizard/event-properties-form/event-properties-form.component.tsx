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
import LuxonUtils from "@date-io/luxon";
import { yupResolver } from "@hookform/resolvers/yup";
import {
    Box,
    Button,
    Chip,
    Grid,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { DateTimePicker, MuiPickersUtilsProvider } from "@material-ui/pickers";
import { isEmpty, map } from "lodash";
import React, { FunctionComponent, useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { Event, TargetDimensionMap } from "../../../rest/dto/event.interfaces";
import { createEmptyEvent } from "../../../utils/events/events.util";
import { DateTimePickerToolbar } from "../../time-range/time-range-selector/date-time-picker-toolbar/date-time-picker-toolbar.component";
import {
    DynamicFormType,
    EventPropertiesFormProps,
} from "./event-properties-form.interfaces";
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
        const { name, type, startTime, endTime } = event;
        const targetDimensionMap = {} as TargetDimensionMap;
        dynamicFormState.map(({ propertyName, propertyValue }) => {
            if (!isEmpty(propertyName) && !isEmpty(propertyValue)) {
                targetDimensionMap[propertyName] = propertyValue;
            }
        });

        const newEvent: Event = {
            name,
            type,
            startTime,
            endTime,
            targetDimensionMap,
        } as Event;
        onSubmit && onSubmit(newEvent);
    };

    const [dynamicFormState, setDynamicFormState] = useState<DynamicFormType[]>(
        () => {
            let dynamicForm: DynamicFormType[] = [];
            if (!isEmpty(event.targetDimensionMap)) {
                dynamicForm = map(event.targetDimensionMap, (value, key) => {
                    return {
                        key,
                        propertyName: key,
                        propertyValue: value,
                    };
                });
            } else {
                dynamicForm = [
                    {
                        key: "0",
                        propertyName: "",
                        propertyValue: [],
                    },
                ];
            }

            return dynamicForm;
        }
    );

    const handleListChange = (
        e: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>,
        item: DynamicFormType
    ): void => {
        const { value } = e.target;
        setDynamicFormState((prev) => {
            const newArr = prev.map((subItem) => {
                return subItem.key === item.key
                    ? {
                          ...item,
                          propertyName: value,
                      }
                    : subItem;
            });

            return newArr;
        });
    };

    const handleAddListItem = (): void => {
        const newItem = {
            key: dynamicFormState.length.toString(),
            propertyName: "",
            propertyValue: [],
        };
        setDynamicFormState((prev) => [...prev, newItem]);
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

                {/* Spacer */}
                <Grid item sm={12} />

                <Grid item xs={12}>
                    <Box marginBottom={2}>
                        <Typography variant="h5">
                            {t("label.event-metadata")}
                        </Typography>
                        <Typography variant="body2">
                            Create custom Event properties and corresponding
                            values
                        </Typography>
                    </Box>
                </Grid>

                <Grid item xs={6}>
                    <Box paddingBottom={1}>Property name</Box>
                </Grid>
                <Grid item xs={6}>
                    <Box paddingBottom={1}>Property value</Box>
                </Grid>

                <Grid item xs={12}>
                    {dynamicFormState.map((item) => {
                        return (
                            <Grid container item key={item.key} xs={12}>
                                <Grid item xs={5}>
                                    <TextField
                                        fullWidth
                                        inputProps={{ tabIndex: -1 }}
                                        inputRef={register}
                                        name="propertyName"
                                        placeholder={t(
                                            "label.add-property-key"
                                        )}
                                        value={item.propertyName}
                                        onChange={(
                                            e: React.ChangeEvent<
                                                | HTMLTextAreaElement
                                                | HTMLInputElement
                                            >
                                        ) => handleListChange(e, item)}
                                    />
                                </Grid>
                                <Grid item xs={1} />
                                <Grid
                                    item
                                    className={
                                        eventPropertiesFormStyles.propertyValue
                                    }
                                    xs={5}
                                >
                                    <Controller
                                        control={control}
                                        name="propertyValue"
                                        render={({ onChange }) => (
                                            <Autocomplete
                                                freeSolo
                                                multiple
                                                defaultValue={
                                                    item.propertyValue.length >
                                                    0
                                                        ? item.propertyValue
                                                        : undefined
                                                }
                                                options={[] as string[]}
                                                renderInput={(params) => (
                                                    <TextField
                                                        {...params}
                                                        name="propertyValue"
                                                        placeholder="Type and press enter to add values"
                                                        variant="outlined"
                                                    />
                                                )}
                                                renderTags={(
                                                    value: readonly string[],
                                                    getTagProps
                                                ) =>
                                                    value.map(
                                                        (
                                                            option: string,
                                                            index: number
                                                        ) => (
                                                            <Chip
                                                                key={index}
                                                                variant="outlined"
                                                                {...getTagProps(
                                                                    {
                                                                        index,
                                                                    }
                                                                )}
                                                                label={option}
                                                                size="small"
                                                            />
                                                        )
                                                    )
                                                }
                                                onChange={(
                                                    _event: React.ChangeEvent<
                                                        Record<string, unknown>
                                                    >,
                                                    value: string[]
                                                ) => {
                                                    onChange(value);
                                                    setDynamicFormState(
                                                        (prev) => {
                                                            const newArr =
                                                                prev.map(
                                                                    (
                                                                        subItem
                                                                    ) => {
                                                                        return subItem.key ===
                                                                            item.key
                                                                            ? {
                                                                                  ...item,
                                                                                  propertyValue:
                                                                                      value,
                                                                              }
                                                                            : subItem;
                                                                    }
                                                                );

                                                            return newArr;
                                                        }
                                                    );
                                                }}
                                            />
                                        )}
                                    />
                                </Grid>

                                {/* Spacer */}
                                <Grid item sm={12} />
                            </Grid>
                        );
                    })}
                    <Grid item xs={12}>
                        <Button variant="contained" onClick={handleAddListItem}>
                            Add metadata
                        </Button>
                    </Grid>
                </Grid>
            </Grid>
        </form>
    );
};
