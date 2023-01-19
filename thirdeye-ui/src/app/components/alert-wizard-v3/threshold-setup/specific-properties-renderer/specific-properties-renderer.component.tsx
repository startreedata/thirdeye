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
import { Slider, TextField } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { SliderAlgorithmOptionInputFieldConfig } from "../../algorithm-selection/algorithm-selection.interfaces";
import { SpecificPropertiesRendererProps } from "./specific-properties-renderer.interfaces";

export const SpecificPropertiesRenderer: FunctionComponent<SpecificPropertiesRendererProps> =
    ({ onAlertPropertyChange, alert, inputFieldConfig }) => {
        const { t } = useTranslation();
        const existingValue =
            alert.templateProperties[inputFieldConfig.templatePropertyName];

        const handlePropertyChange = (
            newValue: string,
            propertyName: string
        ): void => {
            onAlertPropertyChange({
                templateProperties: {
                    ...alert.templateProperties,
                    [propertyName]: newValue,
                },
            });
        };

        if (inputFieldConfig.type === "slider") {
            const sliderFieldConfig =
                inputFieldConfig as SliderAlgorithmOptionInputFieldConfig;
            const middlePoint =
                (sliderFieldConfig.min + sliderFieldConfig.max) / 2;

            return (
                <Slider
                    defaultValue={
                        existingValue ? Number(existingValue) : middlePoint
                    }
                    marks={[
                        {
                            value: sliderFieldConfig.min as number,
                            label: t("label.low"),
                        },
                        {
                            value: middlePoint,
                            label: t("label.medium"),
                        },
                        {
                            value: sliderFieldConfig.max as number,
                            label: t("label.high"),
                        },
                    ]}
                    max={sliderFieldConfig.max}
                    min={sliderFieldConfig.min}
                    step={1}
                    onChange={(_, value) =>
                        handlePropertyChange(
                            value.toString(),
                            sliderFieldConfig.templatePropertyName
                        )
                    }
                />
            );
        }

        return (
            <TextField
                fullWidth
                defaultValue={existingValue ?? undefined}
                type={inputFieldConfig.type}
                variant="outlined"
                onChange={(e) => {
                    handlePropertyChange(
                        e.currentTarget.value,
                        inputFieldConfig.templatePropertyName
                    );
                }}
            />
        );
    };
