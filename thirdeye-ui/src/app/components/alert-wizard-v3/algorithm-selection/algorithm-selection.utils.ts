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
    AlgorithmOption,
    AvailableAlgorithmOption,
} from "./algorithm-selection.interfaces";

export const generateOptions = (
    t: (id: string) => string
): [AlgorithmOption[], AlgorithmOption[]] => {
    return [
        // simple
        [
            {
                title: t("label.threshold"),
                description: t("message.threshold-algorithm-description"),
                alertTemplate: "startree-threshold",
                alertTemplateForMultidimension: "startree-threshold-dx",
                alertTemplateForPercentile: "startree-threshold-percentile",
                inputFieldConfigs: [
                    {
                        templatePropertyName: "min",
                        label: t("label.minimum"),
                        description: t(
                            "message.minimum-alert-template-value-property-description"
                        ),
                        type: "number",
                    },
                    {
                        templatePropertyName: "max",
                        label: t("label.maximum"),
                        description: t(
                            "message.maximum-alert-template-value-property-description"
                        ),
                        type: "number",
                    },
                ],
            },
            {
                title: t("label.mean-variance-rule"),
                description: t(
                    "message.mean-variance-rule-algorithm-description"
                ),
                alertTemplate: "startree-mean-variance",
                alertTemplateForMultidimension: "startree-mean-variance-dx",
                alertTemplateForPercentile: "startree-mean-variance-percentile",
                inputFieldConfigs: [
                    {
                        templatePropertyName: "sensitivity",
                        label: t("label.sensitivity"),
                        description: "",
                        type: "slider",
                        min: -26,
                        max: 14,
                    },
                ],
            },
            {
                title: t("label.percentage-rule"),
                description: t("message.percentage-rule-algorithm-description"),
                alertTemplate: "startree-percentage-rule",
                alertTemplateForMultidimension: "startree-percentage-rule-dx",
                alertTemplateForPercentile: "startree-percentage-percentile",
                inputFieldConfigs: [
                    {
                        templatePropertyName: "baselineOffset",
                        label: t("label.baseline-offset"),
                        description: "",
                        type: "number",
                    },
                    {
                        templatePropertyName: "percentageChange",
                        label: t("label.percentage-change"),
                        description: "",
                        type: "number",
                    },
                ],
            },
            {
                title: t("label.absolute-change-rule"),
                description: t(
                    "message.absolute-change-rule-algorithm-description"
                ),
                alertTemplate: "startree-absolute-rule",
                alertTemplateForMultidimension: "startree-absolute-rule-dx",
                alertTemplateForPercentile: "startree-absolute-percentile",
                inputFieldConfigs: [
                    {
                        templatePropertyName: "baselineOffset",
                        label: t("label.baseline-offset"),
                        description: "",
                        type: "number",
                    },
                    {
                        templatePropertyName: "absoluteChange",
                        label: t("label.absolute-change"),
                        description: "",
                        type: "number",
                    },
                ],
            },
        ],
        // advanced
        [
            {
                title: t("label.holt-winters-rule"),
                description: t(
                    "message.holt-winters-rule-algorithm-description"
                ),
                alertTemplate: "startree-holt-winters",
                alertTemplateForMultidimension: "startree-holt-winters-dx",
                alertTemplateForPercentile: "startree-holt-winters-percentile",
                inputFieldConfigs: [
                    {
                        templatePropertyName: "sensitivity",
                        label: t("label.sensitivity"),
                        description: "",
                        type: "slider",
                        min: -26,
                        max: 14,
                    },
                ],
            },
            {
                title: t("label.startree-ets"),
                description: t("message.startree-ets-algorithm-description"),
                alertTemplate: "startree-ets",
                alertTemplateForMultidimension: "startree-ets-dx",
                alertTemplateForPercentile: "startree-ets-percentile",
                inputFieldConfigs: [
                    {
                        templatePropertyName: "sensitivity",
                        label: t("label.sensitivity"),
                        description: "",
                        type: "slider",
                        min: -26,
                        max: 14,
                    },
                ],
            },
        ],
    ];
};

export const filterOptionWithTemplateNames = (
    options: AvailableAlgorithmOption[]
): AvailableAlgorithmOption[] => {
    return options.filter(
        (c) => c.hasAlertTemplate || c.hasPercentile || c.hasMultidimension
    );
};

export const generateAvailableAlgorithmOptions = (
    t: (id: string) => string,
    availableTemplateNames: string[]
): [AvailableAlgorithmOption[], AvailableAlgorithmOption[]] => {
    const [simpleOptions, advancedOptions] = generateOptions(t);

    const availableSimpleOptions = simpleOptions.map((option) => {
        return {
            algorithmOption: option,
            hasAlertTemplate: availableTemplateNames.includes(
                option.alertTemplate
            ),
            hasPercentile: availableTemplateNames.includes(
                option.alertTemplateForPercentile
            ),
            hasMultidimension: availableTemplateNames.includes(
                option.alertTemplateForMultidimension
            ),
        };
    });

    const availableAdvancedOptions = advancedOptions.map((option) => {
        return {
            algorithmOption: option,
            hasAlertTemplate: availableTemplateNames.includes(
                option.alertTemplate
            ),
            hasPercentile: availableTemplateNames.includes(
                option.alertTemplateForPercentile
            ),
            hasMultidimension: availableTemplateNames.includes(
                option.alertTemplateForMultidimension
            ),
        };
    });

    return [availableSimpleOptions, availableAdvancedOptions];
};
