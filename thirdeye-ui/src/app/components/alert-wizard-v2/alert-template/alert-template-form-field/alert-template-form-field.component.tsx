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
import React, { FunctionComponent, useState } from "react";
import { PropertyConfigValueTypes } from "../../../../rest/dto/alert.interfaces";
import { InputSection } from "../../../form-basics/input-section/input-section.component";
import { AlertTemplateFormFieldProps } from "./alert-template-form-field.interfaces";
import { FormComponentForTemplateField } from "./form-component-for-template-field/form-component-for-template-field.component";
import { LabelForTemplateField } from "./label-for-template-field/label-for-template-field.component";

export const AlertTemplateFormField: FunctionComponent<AlertTemplateFormFieldProps> =
    ({ item, tabIndex, placeholder, tooltipText, onChange }) => {
        const [localValueCopy, setLocalValueCopy] =
            useState<PropertyConfigValueTypes>(item.value);

        const handleOnChange = (newValue: PropertyConfigValueTypes): void => {
            setLocalValueCopy(newValue);
            onChange(newValue);
        };

        return (
            <InputSection
                gridContainerProps={{ alignItems: "flex-start" }}
                inputComponent={
                    <FormComponentForTemplateField
                        placeholder={placeholder}
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
        );
    };
