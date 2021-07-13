import {
    FormControl,
    FormHelperText,
    Grid,
    InputLabel,
    MenuItem,
    Select,
} from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { Controller, useFormContext } from "react-hook-form";
import { renderFields } from "../../utils/form/form.utils";
import {
    Field,
    MultiFieldDropdownProps,
} from "./multi-value-dropdown.interface";

/**
 *
 * Since this component expacted to be used with form context
 * This component must be wrap around `<FormProvider>` from "react-hook-form"
 * Component will render a select box with given options and
 * Render fields mention for a selected value
 *
 * ```jsx
 *  <FormProvider {...methods}>
 *       <form onSubmit={methods.handleSubmit(onSubmit)}>
 *           <MultiFieldDropdown
 *               label="Recurrence"
 *               name="recurrence"
 *               options={RecurrenceOptions}
 *           />
 *       </form>
 *   </FormProvider>
 * ```
 */
export const MultiFieldDropdown: FunctionComponent<MultiFieldDropdownProps> = ({
    label,
    name,
    options,
}: MultiFieldDropdownProps) => {
    const [fields, setFields] = useState<Field[]>([]);
    const formContextValues = useFormContext();

    const { control, watch, errors, register } = formContextValues || {};
    const recurrence = watch && watch(name);

    useEffect(() => {
        if (recurrence) {
            const currValue = options.find(
                (option) => option.value === recurrence
            );
            if (currValue) {
                setFields(currValue.fields || []);
            }
        }
    }, [recurrence]);

    if (!formContextValues) {
        console.warn(
            "Component: MultiFieldDropdown should wrap around <FormProvider></FormProvider>"
        );

        return null;
    }

    return (
        <Grid container item>
            <Grid item xs={3}>
                <FormControl fullWidth variant="outlined">
                    <InputLabel id="metrics-dataset">{label}</InputLabel>
                    <Controller
                        control={control}
                        name={name}
                        render={({ onChange, value }) => (
                            <Select
                                label={label}
                                labelId={label + "recurrence"}
                                value={value}
                                variant="outlined"
                                onChange={(e) => onChange(e.target.value)}
                            >
                                {options.map((option, index) => (
                                    <MenuItem key={index} value={option.value}>
                                        {option.label}
                                    </MenuItem>
                                ))}
                            </Select>
                        )}
                    />
                    {errors[name] && (
                        <FormHelperText error>
                            {errors[name].message}
                        </FormHelperText>
                    )}
                </FormControl>
            </Grid>
            {/* render fields if present */}
            {fields.map((field) => (
                <Grid item key={field.name} xs={3}>
                    {renderFields(
                        field,
                        register,
                        control,
                        errors[field.name]?.message
                    )}
                </Grid>
            ))}
        </Grid>
    );
};
