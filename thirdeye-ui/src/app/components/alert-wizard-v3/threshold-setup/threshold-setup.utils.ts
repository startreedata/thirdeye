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
/**
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import i18n from "i18next";
import { Dispatch, SetStateAction } from "react";
import {
    AlertTemplate,
    MetadataProperty,
} from "../../../rest/dto/alert-template.interfaces";
import {
    EditableAlert,
    TemplatePropertiesObject,
} from "../../../rest/dto/alert.interfaces";
import { Dataset } from "../../../rest/dto/dataset.interfaces";
import { MetricAggFunction } from "../../../rest/dto/metric.interfaces";
import { SENSITIVITY_RANGE } from "../../../utils/constants/constants.util";
import { DatasetInfo } from "../../../utils/datasources/datasources.util";
import {
    AlgorithmOptionInputFieldConfig,
    SliderAlgorithmOptionInputFieldConfig,
} from "./threshold-setup.interfaces";

export function generateTemplateProperties(
    metric: string,
    dataset: Dataset,
    aggregationFunction: string
): TemplatePropertiesObject {
    const templateProperties: TemplatePropertiesObject = {
        dataSource: dataset.dataSource.name,
        dataset: dataset.name,
        aggregationColumn: metric,
        aggregationFunction: aggregationFunction,
    };

    templateProperties.timezone = dataset.timeColumn.timezone;

    return templateProperties;
}

export function resetSelectedMetrics(
    datasetsInfo: DatasetInfo[],
    alertConfiguration: EditableAlert,
    setSelectedTable: Dispatch<SetStateAction<DatasetInfo | null>>,
    setSelectedMetric: Dispatch<SetStateAction<string | null>>,
    setSelectedAggregationFunction: Dispatch<SetStateAction<MetricAggFunction>>
): void {
    const newlySelectedDataset = datasetsInfo.find((candidate) => {
        return (
            candidate.dataset.name ===
                alertConfiguration.templateProperties?.dataset &&
            candidate.dataset.dataSource.name ===
                alertConfiguration.templateProperties?.dataSource
        );
    });

    setSelectedTable(newlySelectedDataset || null);

    if (newlySelectedDataset) {
        setSelectedMetric(
            (alertConfiguration.templateProperties
                ?.aggregationColumn as string) || null
        );
    } else {
        setSelectedMetric(null);
    }

    if (alertConfiguration.templateProperties?.aggregationFunction) {
        setSelectedAggregationFunction(
            alertConfiguration.templateProperties
                .aggregationFunction as MetricAggFunction
        );
    }
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
