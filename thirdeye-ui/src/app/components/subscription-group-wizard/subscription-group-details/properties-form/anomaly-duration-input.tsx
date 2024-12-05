/*
 * Copyright 2024 StarTree Inc
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
import React, { ChangeEvent, useEffect } from "react";
import { useState } from "react";
import {
    FormControlLabel,
    MenuItem,
    Radio,
    RadioGroup,
    Select,
    TextField,
} from "@material-ui/core";
import { AnomalyDurationInput } from "./properties-form.interfaces";

const options: { [key: string]: string } = {
    D: "Days",
    H: "Hours",
    M: "Minutes",
    S: "Seconds",
};

export const AnomayDurationInput = ({
    value,
    onChange,
}: AnomalyDurationInput): JSX.Element => {
    const [showCustomInput, setShowCustomInput] = useState(false);
    const [selectedOption, setSelectedOption] = useState<string | null>(
        options["D"]
    );
    const [numberInput, setNumberInput] = useState<number | null>(1);

    useEffect(() => {
        if (value) {
            const midValue = value?.substring(1, value.length - 1);
            if (isNaN(Number(midValue))) {
                setShowCustomInput(true);
            } else {
                setShowCustomInput(false);
                setSelectedOption(options[value[value.length - 1]]);
                setNumberInput(Number(midValue));
            }
        } else {
            onChange("P1D");
        }
    }, [value]);

    const handleNumberInputChange = (
        e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
    ): void => {
        const value = Number(e.target.value);
        setNumberInput(value);
        const constructedValue = `P${value}${selectedOption![0]}`;
        onChange(constructedValue);
    };

    const handleSelectInputChange = (
        e: React.ChangeEvent<{ name?: string; value: string | unknown }>
    ): void => {
        const value = e.target.value as string;
        setSelectedOption(value as string);
        const constructedValue = `P${numberInput}${value[0]}`;
        onChange(constructedValue);
    };

    const renderCustomInput = (): JSX.Element => {
        return (
            <TextField
                id="text-field"
                value={value}
                onChange={(e) => onChange(e.target.value)}
            />
        );
    };

    const renderDefaultInput = (): JSX.Element => {
        return (
            <div style={{ display: "flex", gap: "4px" }}>
                <TextField
                    InputLabelProps={{
                        shrink: true,
                    }}
                    id="standard-number"
                    inputProps={{
                        min: 0,
                    }}
                    type="number"
                    value={numberInput}
                    onChange={handleNumberInputChange}
                />
                <Select
                    style={{ height: "37px" }}
                    value={selectedOption}
                    variant="outlined"
                    onChange={handleSelectInputChange}
                >
                    {Object.values(options).map((value) => (
                        <MenuItem key={value} value={value}>
                            {value}
                        </MenuItem>
                    ))}
                </Select>
            </div>
        );
    };

    return (
        <>
            <RadioGroup
                row
                aria-label="input-type-radio-buttons"
                name="input-type-radio-buttons"
                value={showCustomInput ? "CUSTOM" : "DEFAULT"}
                onChange={(e) =>
                    setShowCustomInput(e.target.value === "CUSTOM")
                }
            >
                <FormControlLabel
                    control={<Radio />}
                    label="Default"
                    value="DEFAULT"
                />
                <FormControlLabel
                    control={<Radio />}
                    label="Custom"
                    value="CUSTOM"
                />
            </RadioGroup>
            {showCustomInput ? renderCustomInput() : renderDefaultInput()}
        </>
    );
};
