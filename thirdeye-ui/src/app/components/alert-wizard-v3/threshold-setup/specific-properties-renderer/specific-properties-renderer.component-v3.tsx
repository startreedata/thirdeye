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
import { Slider, TextField } from "@material-ui/core";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { SliderAlgorithmOptionInputFieldConfig } from "../threshold-setup.interfaces";
import { SpecificPropertiesRendererProps } from "./specific-properties-renderer.interfaces";

export const SpecificPropertiesRendererV3: FunctionComponent<SpecificPropertiesRendererProps> =
    ({
        onAlertPropertyChange,
        selectedTemplateProperties,
        inputFieldConfig,
    }) => {
        const { t } = useTranslation();

        const existingValue =
            selectedTemplateProperties[inputFieldConfig.templatePropertyName];

        const [localInputFieldValue, setLocalInputFieldValue] =
            useState<string>(existingValue as string);

        useEffect(() => {
            setLocalInputFieldValue(existingValue as string);
        }, [existingValue]);

        const handlePropertyChange = (
            propertyName: string,
            newValue: string
        ): void => {
            setLocalInputFieldValue(newValue);
            onAlertPropertyChange(propertyName, newValue);
        };

        React.useEffect(() => {
            if (
                inputFieldConfig.type === "slider" &&
                (existingValue === undefined || existingValue === null)
            ) {
                const sliderFieldConfig =
                    inputFieldConfig as SliderAlgorithmOptionInputFieldConfig;
                const middlePoint =
                    (sliderFieldConfig.min + sliderFieldConfig.max) / 2;
                handlePropertyChange(
                    sliderFieldConfig.templatePropertyName,
                    middlePoint.toString()
                );
            }
        }, [
            inputFieldConfig.type,
            existingValue,
            inputFieldConfig,
            handlePropertyChange,
        ]);

        if (inputFieldConfig.type === "slider") {
            const sliderFieldConfig =
                inputFieldConfig as SliderAlgorithmOptionInputFieldConfig;
            const middlePoint =
                (sliderFieldConfig.min + sliderFieldConfig.max) / 2;

            return (
                <Slider
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
                    value={Number(existingValue)}
                    onChange={(_, value) =>
                        handlePropertyChange(
                            sliderFieldConfig.templatePropertyName,
                            value.toString()
                        )
                    }
                />
            );
        }

        return (
            <TextField
                fullWidth
                data-testid={`${inputFieldConfig.templatePropertyName}-container`}
                type={inputFieldConfig.type}
                value={localInputFieldValue ?? ""}
                variant="outlined"
                onChange={(e) =>
                    handlePropertyChange(
                        inputFieldConfig.templatePropertyName,
                        e.currentTarget.value
                    )
                }
            />
        );
    };
