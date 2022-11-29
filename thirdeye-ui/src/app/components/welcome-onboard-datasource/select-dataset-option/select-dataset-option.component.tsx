/*
 * Copyright 2022 StarTree Inc
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

import {
    Checkbox,
    FormControl,
    FormControlLabel,
    FormHelperText,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { SelectDatasetProps } from "./select-dataset-option.interfaces";

export const SelectDatasetOption: FunctionComponent<SelectDatasetProps> = ({
    checked,
    indeterminate,
    onChange,
    labelPrimaryText,
    labelSecondaryText,
    name,
}) => {
    return (
        <FormControl component="fieldset" margin="dense">
            <FormControlLabel
                control={
                    <Checkbox
                        checked={checked}
                        {...(indeterminate && { indeterminate })}
                        color="primary"
                        inputProps={{ "aria-label": name }}
                        name={name}
                        onChange={onChange}
                    />
                }
                label={
                    <>
                        {labelPrimaryText}
                        {labelSecondaryText ? (
                            <FormHelperText>
                                {labelSecondaryText}
                            </FormHelperText>
                        ) : null}
                    </>
                }
            />
        </FormControl>
    );
};
