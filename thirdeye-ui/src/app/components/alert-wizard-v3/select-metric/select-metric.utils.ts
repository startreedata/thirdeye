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

import i18n from "i18next";
import {
    AlertTemplate,
    MetadataProperty,
} from "../../../rest/dto/alert-template.interfaces";
import {
    EditableAlert,
    TemplatePropertiesObject,
} from "../../../rest/dto/alert.interfaces";
import { Dataset } from "../../../rest/dto/dataset.interfaces";
import { SENSITIVITY_RANGE } from "../../../utils/constants/constants.util";
import { DatasetInfo } from "../../../utils/datasources/datasources.util";
import {
    AlgorithmOptionInputFieldConfig,
    SliderAlgorithmOptionInputFieldConfig,
} from "./select-metric.interfaces";

export function generateTemplateProperties(
    metric: string,
    dataset: Dataset,
    aggregationFunction: string,
    granularity: string
): TemplatePropertiesObject {
    const templateProperties: TemplatePropertiesObject = {
        dataSource: dataset.dataSource.name,
        dataset: dataset.name,
        aggregationColumn: metric,
        aggregationFunction: aggregationFunction,
        monitoringGranularity: granularity,
    };

    templateProperties.timezone = dataset.timeColumn.timezone;

    return templateProperties;
}

export function determineDatasetInitialSelectionsFromServerData(
    datasetsInfo: DatasetInfo[],
    alertConfiguration: EditableAlert
): [DatasetInfo | null, string | null] {
    const matchingDataset = datasetsInfo.find((candidate) => {
        return (
            candidate.dataset.name ===
                alertConfiguration.templateProperties?.dataset &&
            candidate.dataset.dataSource.name ===
                alertConfiguration.templateProperties?.dataSource
        );
    });

    if (matchingDataset) {
        return [
            matchingDataset,
            (alertConfiguration.templateProperties
                ?.aggregationColumn as string) || null,
        ];
    }

    return [null, null];
}

const SUPPORTED_SIMPLE_MODE_PROPERTIES: {
    [key: string]:
        | AlgorithmOptionInputFieldConfig
        | SliderAlgorithmOptionInputFieldConfig;
} = {
    sensitivity: {
        templatePropertyName: "sensitivity",
        label: i18n.t("label.sensitivity"),
        type: "slider",
        min: SENSITIVITY_RANGE.LOW,
        max: SENSITIVITY_RANGE.HIGH,
    },
    baselineOffset: {
        templatePropertyName: "baselineOffset",
        label: i18n.t("label.baseline-offset"),
        type: "string",
    },
    percentageChange: {
        templatePropertyName: "percentageChange",
        label: i18n.t("label.percentage-change"),
        type: "number",
    },
    absoluteChange: {
        templatePropertyName: "absoluteChange",
        label: i18n.t("label.absolute-change"),
        type: "number",
    },
    min: {
        templatePropertyName: "min",
        label: i18n.t("label.minimum"),
        type: "number",
    },
    max: {
        templatePropertyName: "max",
        label: i18n.t("label.maximum"),
        type: "number",
    },
    monitoringGranularity: {
        templatePropertyName: "monitoringGranularity",
        label: i18n.t("label.monitoring-granularity"),
        type: "string",
    },
    seasonalityPeriod: {
        templatePropertyName: "seasonalityPeriod",
        label: i18n.t("label.seasonality-period"),
        type: "string",
    },
    lookback: {
        templatePropertyName: "lookback",
        label: i18n.t("label.lookback"),
        type: "string",
    },
};

export function generateInputFieldConfigsForAlertTemplate(
    alertTemplate: AlertTemplate
): (AlgorithmOptionInputFieldConfig | SliderAlgorithmOptionInputFieldConfig)[] {
    if (alertTemplate.properties === undefined) {
        return [];
    }
    const inputFieldConfigs: (
        | AlgorithmOptionInputFieldConfig
        | SliderAlgorithmOptionInputFieldConfig
    )[] = [];

    const propertyNameToMetadata: { [key: string]: MetadataProperty } = {};

    alertTemplate.properties.forEach((alertTemplatePropertyMeta) => {
        propertyNameToMetadata[alertTemplatePropertyMeta.name] =
            alertTemplatePropertyMeta;
    });

    Object.keys(SUPPORTED_SIMPLE_MODE_PROPERTIES).forEach((propertyName) => {
        if (propertyNameToMetadata[propertyName]) {
            inputFieldConfigs.push({
                ...SUPPORTED_SIMPLE_MODE_PROPERTIES[propertyName],
                description: propertyNameToMetadata[propertyName].description,
            });
        }
    });

    return inputFieldConfigs;
}

export enum GranularityValue {
    WEEKLY = "P7D",
    DAILY = "P1D",
    HOURLY = "PT1H",
    FIFTEEN_MINUTES = "PT15M",
    TEN_MINUTES = "PT10M",
    FIVE_MINUTES = "PT5M",
    ONE_MINUTE = "PT1M",
}

export const GRANULARITY_OPTIONS = [
    {
        label: i18n.t("label.weekly"),
        value: GranularityValue.WEEKLY,
    },
    {
        label: i18n.t("label.daily"),
        value: GranularityValue.DAILY,
    },
    {
        label: i18n.t("label.hourly"),
        value: GranularityValue.HOURLY,
    },
    {
        label: i18n.t("label.15-minutes"),
        value: GranularityValue.FIFTEEN_MINUTES,
    },
    {
        label: i18n.t("label.10-minutes"),
        value: GranularityValue.TEN_MINUTES,
    },
    {
        label: i18n.t("label.5-minutes"),
        value: GranularityValue.FIVE_MINUTES,
    },
    {
        label: i18n.t("label.1-minute"),
        value: GranularityValue.ONE_MINUTE,
    },
];

export const GRANULARITY_OPTIONS_TOOLTIP: Record<string, string> = {
    PT1M: i18n.t("message.one-min-monitoring-granularity-beta-tooltip"),
    PT5M: i18n.t("message.five-min-monitoring-granularity-beta-tooltip"),
};
