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
import { Grid } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { PropertyConfigValueTypes } from "../../../../rest/dto/alert.interfaces";
import { InputSectionV2 } from "../../../form-basics/input-section-v2/input-section-v2.component";
import { AlertTemplateFormFieldProps } from "./alert-template-form-field.interfaces";
import { FormComponentForTemplateField } from "./form-component-for-template-field/form-component-for-template-field.component";
import { LabelForTemplateField } from "./label-for-template-field/label-for-template-field-v2.component";

function isDefaultValueStringArray(
    defaultValue?: PropertyConfigValueTypes
): boolean {
    return (
        Array.isArray(defaultValue) &&
        !!defaultValue.length &&
        (defaultValue as Array<unknown>).every(
            (element: unknown) => typeof element === "string"
        )
    );
}

export const AlertTemplateFormField: FunctionComponent<AlertTemplateFormFieldProps> =
    ({ item, tabIndex, tooltipText, onChange, placeholder, defaultValue }) => {
        const [localValueCopy, setLocalValueCopy] =
            useState<PropertyConfigValueTypes>(item.value);

        const handleOnChange = (newValue: PropertyConfigValueTypes): void => {
            setLocalValueCopy(newValue);
            onChange(newValue);
        };

        useEffect(() => {
            if (isDefaultValueStringArray(defaultValue)) {
                handleOnChange(defaultValue as PropertyConfigValueTypes);
            }
        }, [defaultValue]);

        const getPlaceholder = (): string => {
            if (placeholder) {
                return placeholder;
            }
            if (isDefaultValueStringArray(defaultValue)) {
                return "";
            } else if (defaultValue !== undefined && defaultValue !== null) {
                return defaultValue.toString();
            }

            return "";
        };

        return (
            <Grid item xs={item.key === "enumerationItems" ? 8 : 4}>
                <InputSectionV2
                    gridContainerProps={{ alignItems: "flex-start" }}
                    inputComponent={
                        <FormComponentForTemplateField
                            placeholder={getPlaceholder()}
                            propertyKey={item.key}
                            tabIndex={tabIndex}
                            templateFieldProperty={item.metadata}
                            value={localValueCopy}
                            onChange={handleOnChange}
                        />
                    }
                    key={item.key}
                    labelComponent={
                        <LabelForTemplateField
                            name={item.key}
                            tooltipText={tooltipText}
                        />
                    }
                />
            </Grid>
        );
    };
