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
import { Grid, Switch, TextField } from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { JSONEditorWithLocalCache } from "../../../../json-editor-with-local-cache/json-editor-with-local-cache.component";
import { FormComponentForTemplateFieldProps } from "./form-component-for-template-field.interfaces";

export const FormComponentForTemplateField: FunctionComponent<FormComponentForTemplateFieldProps> =
    ({
        templateFieldProperty,
        onChange,
        placeholder,
        tabIndex,
        value,
        propertyKey,
    }) => {
        const { t } = useTranslation();

        let inputComponent;

        // If value of property should one or multiple predefined values
        if (
            templateFieldProperty.options &&
            templateFieldProperty.options.length > 0
        ) {
            inputComponent = (
                <Autocomplete
                    fullWidth
                    multiple={templateFieldProperty.multiselect}
                    options={templateFieldProperty.options}
                    renderInput={(params) => (
                        <TextField
                            data-testid={`optionedselect-${propertyKey}`}
                            {...params}
                            InputProps={{
                                ...params.InputProps,
                                tabIndex: tabIndex,
                            }}
                            placeholder={
                                placeholder
                                    ? placeholder
                                    : t(
                                          "message.click-to-open-list-of-available-options"
                                      )
                            }
                            variant="outlined"
                        />
                    )}
                    value={value}
                    onChange={(_, selected) => {
                        onChange && onChange(selected as string[]);
                    }}
                />
            );

            // If value of item is some JSON object
        } else if (
            templateFieldProperty.jsonType === "OBJECT" ||
            propertyKey === "enumerationItems"
        ) {
            inputComponent = (
                <JSONEditorWithLocalCache
                    disableAutoFormat
                    hideValidationSuccessIcon
                    data-testid={`jsoneditor-${propertyKey}`}
                    initialValue={(value as Record<string, unknown>) ?? {}}
                    onChange={(json) => {
                        onChange && onChange(JSON.parse(json));
                    }}
                />
            );

            // If value of item is arbitrary list of strings
        } else if (templateFieldProperty.jsonType === "ARRAY") {
            inputComponent = (
                <Autocomplete
                    freeSolo
                    fullWidth
                    multiple
                    options={[]}
                    renderInput={(params) => (
                        <TextField
                            data-testid={`multifreesolo-${propertyKey}`}
                            {...params}
                            InputProps={{
                                ...params.InputProps,
                                tabIndex: tabIndex,
                            }}
                            inputProps={{
                                ...params.inputProps,
                                "data-testid": `multifreesolo-input-${propertyKey}`,
                            }}
                            placeholder={
                                placeholder
                                    ? placeholder
                                    : t(
                                          "message.type-a-value-and-press-enter-to-add"
                                      )
                            }
                            variant="outlined"
                        />
                    )}
                    value={(value || []) as string[]}
                    onChange={(_, selected) => {
                        onChange && onChange(selected as string[]);
                    }}
                />
            );
            // If value of item is a boolean value
        } else if (templateFieldProperty.jsonType === "BOOLEAN") {
            inputComponent = (
                <Grid
                    container
                    alignItems="center"
                    component="label"
                    justifyContent="space-around"
                    spacing={1}
                >
                    <Grid item>{t("label.false")}</Grid>
                    <Grid item>
                        <Switch
                            data-testid={`switch-${propertyKey}`}
                            defaultChecked={!!value}
                            onChange={(_, checked) => {
                                onChange && onChange(checked);
                            }}
                        />
                    </Grid>
                    <Grid item>{t("label.true")}</Grid>
                </Grid>
            );
            // Default to string input field
        } else {
            inputComponent = (
                <TextField
                    fullWidth
                    data-testid={`textfield-${propertyKey}`}
                    defaultValue={value}
                    inputProps={{
                        tabIndex: tabIndex,
                        "data-testid": `input-${propertyKey}`,
                    }}
                    placeholder={placeholder}
                    onChange={(e) => {
                        onChange && onChange(e.currentTarget.value);
                    }}
                />
            );
        }

        return inputComponent;
    };
