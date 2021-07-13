import {
    FormControl,
    FormHelperText,
    InputLabel,
    MenuItem,
    Select,
    TextField,
} from "@material-ui/core";
import React, { ReactNode } from "react";
import { Control, Controller } from "react-hook-form";
import {
    Field,
    SelectOption,
} from "../../components/multi-field-dropdown/multi-value-dropdown.interface";

export const renderFields = (
    field: Field,
    register: () => void,
    control: Control,
    error: boolean | string
): ReactNode | null => {
    const options: SelectOption[] = field.options
        ? field.options.map<SelectOption>((option) =>
              typeof option === "string"
                  ? { value: option, label: option }
                  : option
          )
        : [];

    switch (field.type) {
        case "text":
        case "number":
            return (
                <TextField
                    fullWidth
                    error={Boolean(error)}
                    helperText={error && error}
                    inputRef={register}
                    key={field.name}
                    label={field.label}
                    name={field.name}
                    type={field.type}
                    variant="outlined"
                />
            );
        case "select":
            return (
                <FormControl fullWidth variant="outlined">
                    <InputLabel id={field.label}>{field.label}</InputLabel>
                    <Controller
                        control={control}
                        name={field.name}
                        render={({ onChange, value }) => (
                            <Select
                                label={field.label}
                                labelId={field.label}
                                value={value}
                                variant="outlined"
                                onChange={(e) => onChange(e.target.value)}
                            >
                                {options &&
                                    options.map((option, index) => (
                                        <MenuItem
                                            key={index}
                                            value={option.value}
                                        >
                                            {option.label}
                                        </MenuItem>
                                    ))}
                            </Select>
                        )}
                    />
                    {Boolean(error) && (
                        <FormHelperText error>{error}</FormHelperText>
                    )}
                </FormControl>
            );
        default:
            console.warn(`${field.type} isn't supported yet`);

            return null;
    }
};
