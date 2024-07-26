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
import type {
    AlertTemplate,
    MetadataProperty,
} from "../../../rest/dto/alert-template.interfaces";
import {
    EditableAlert,
    TemplatePropertiesObject,
} from "../../../rest/dto/alert.interfaces";
import { PropertyRenderConfig } from "./alert-template-properties-builder/alert-template-properties-builder.interfaces";
import { AlertTemplatePropertyParsedMetadata } from "./alert-template.interfaces";

const PROPERTY_CAPTURE = /\${(\w*)}/g;
// 5am, Monday through Friday
const DEFAULT_CRON = "0 0 5 ? * MON-FRI *";

export function findAvailableFields(alertTemplate: AlertTemplate): string[] {
    const matches = new Set<string>();
    let match;

    while (
        (match = PROPERTY_CAPTURE.exec(JSON.stringify(alertTemplate))) !== null
    ) {
        matches.add(match[1]);
    }

    return Array.from(matches).sort();
}

export function determinePropertyFieldConfigurationFromDefaultFields(
    alertTemplate: AlertTemplate,
    defaultProperties: { [index: string]: string }
): AlertTemplatePropertyParsedMetadata[] {
    const availableInputFields = findAvailableFields(alertTemplate);

    return availableInputFields.map((fieldName) => {
        const metadata: MetadataProperty = {
            name: fieldName,
            defaultIsNull: false,
            multiselect: false,
        };
        const isOptional = defaultProperties[fieldName] !== undefined;

        if (isOptional) {
            metadata.defaultValue = defaultProperties[fieldName];
        }

        return {
            name: fieldName,
            isOptional,
            metadata,
        };
    });
}

export function determinePropertyFieldConfigurationFromProperties(
    properties: MetadataProperty[]
): AlertTemplatePropertyParsedMetadata[] {
    return properties.map((property) => {
        return {
            name: property.name,
            isOptional:
                property.defaultValue !== undefined || property.defaultIsNull,
            metadata: property,
        };
    });
}

export function determinePropertyFieldConfiguration(
    alertTemplate: AlertTemplate
): AlertTemplatePropertyParsedMetadata[] {
    const metadata: {
        [key: string]: AlertTemplatePropertyParsedMetadata;
    } = {};
    /* This is the old way of figuring out if a field is required or not.
    We allow users to add placeholders of variale in the template without any entry
    in the properties key. For eg they can just put this in template,
    "dataSource: ${dataSource}".
    We have to show this field for the user to add value into, even if its not defined
    in the proerties key of the template.
    Bottomline is we have to always show inout box for any variable mentioned in any of the
    template properties. The properties key is to provide extra information about the variable,
    based on which we categorise them under different steps. As of now if the key just exists
    in the property section, even though we show an inpput box for it, it wont have any
    impact as there is no mapping for this variable to any template property.
    */
    determinePropertyFieldConfigurationFromDefaultFields(
        alertTemplate,
        alertTemplate.defaultProperties ?? {}
    ).forEach((propertyMetadata) => {
        metadata[propertyMetadata.name] = propertyMetadata;
    });

    // `properties` was introduced in December 2022 to explicitly define
    // what the fields are
    if (alertTemplate.properties) {
        // Override any existing metadata determined by the old way with the new
        determinePropertyFieldConfigurationFromProperties(
            alertTemplate.properties
        ).forEach((propertyMetadata) => {
            metadata[propertyMetadata.name] = propertyMetadata;
        });
    }

    return Object.values(metadata);
}

export function setUpFieldInputRenderConfig(
    availableFields: AlertTemplatePropertyParsedMetadata[],
    templateProperties: TemplatePropertiesObject
): [PropertyRenderConfig[], PropertyRenderConfig[]] {
    const requiredKeys: PropertyRenderConfig[] = [];
    const optionalKeys: PropertyRenderConfig[] = [];

    availableFields.forEach((propertyMetadata) => {
        const renderConfig = {
            key: propertyMetadata.name,
            value: templateProperties[propertyMetadata.name],
            metadata: propertyMetadata.metadata,
        };

        if (propertyMetadata.isOptional) {
            optionalKeys.push(renderConfig);
        } else {
            requiredKeys.push(renderConfig);
        }
    });

    return [requiredKeys, optionalKeys];
}

export function hasRequiredPropertyValuesSet(
    availableFields: AlertTemplatePropertyParsedMetadata[],
    alertTemplateProperties: TemplatePropertiesObject
): boolean {
    return availableFields.every((fieldMetadata) => {
        // See https://cortexdata.atlassian.net/browse/TE-817
        // Just check is not an empty value (empty array is ok)
        if (typeof alertTemplateProperties[fieldMetadata.name] === "boolean") {
            return true;
        } else if (!fieldMetadata.isOptional) {
            return !!alertTemplateProperties[fieldMetadata.name];
        }

        return true;
    });
}

export function createNewStartingAlert(): EditableAlert {
    return {
        name: "",
        description: "",
        cron: DEFAULT_CRON,
        template: {
            name: "startree-mean-variance",
        },
        templateProperties: {
            dataSource: "sample_datasource",
            dataset: "sample_dataset",
            aggregationColumn: "sample_column_name",
            aggregationFunction: "SUM",
            lookback: "P28D",
            monitoringGranularity: "P1D",
            sensitivity: "3",
        },
    };
}
