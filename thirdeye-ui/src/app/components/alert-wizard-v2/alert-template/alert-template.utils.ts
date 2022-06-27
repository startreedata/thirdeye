/**
 * Copyright 2022 StarTree Inc
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
import { AlertTemplate } from "../../../rest/dto/alert-template.interfaces";
import { TemplatePropertiesObject } from "../../../rest/dto/alert.interfaces";
import { PropertyRenderConfig } from "./alert-template-properties-builder/alert-template-properties-builder.interfaces";

const PROPERTY_CAPTURE = /\${(\w*)}/g;

export function findRequiredFields(alertTemplate: AlertTemplate): string[] {
    const matches = new Set<string>();
    let match;

    while (
        (match = PROPERTY_CAPTURE.exec(JSON.stringify(alertTemplate))) !== null
    ) {
        matches.add(match[1]);
    }

    return Array.from(matches).sort();
}

export function setUpFieldInputRenderConfig(
    requiredFields: string[],
    templateProperties: TemplatePropertiesObject,
    defaultProperties: TemplatePropertiesObject
): [PropertyRenderConfig[], PropertyRenderConfig[]] {
    const keysWithDefaultProperties = new Set(Object.keys(defaultProperties));

    const requiredKeys: PropertyRenderConfig[] = [];
    const optionalKeys: PropertyRenderConfig[] = [];

    requiredFields.forEach((fieldKey: string) => {
        const renderConfig = {
            key: fieldKey,
            value: templateProperties[fieldKey],
            defaultValue: defaultProperties[fieldKey],
        };

        if (keysWithDefaultProperties.has(fieldKey)) {
            optionalKeys.push(renderConfig);
        } else {
            requiredKeys.push(renderConfig);
        }
    });

    return [requiredKeys, optionalKeys];
}

export function hasRequiredPropertyValuesSet(
    requiredFields: string[],
    alertTemplateProperties: TemplatePropertiesObject,
    defaultProperties: TemplatePropertiesObject
): boolean {
    return requiredFields.every((fieldKey) => {
        if (alertTemplateProperties[fieldKey]) {
            return alertTemplateProperties[fieldKey].length > 0;
        } else if (defaultProperties[fieldKey] !== undefined) {
            // Empty string can be valid for default property
            return true;
        }

        return false;
    });
}

export function ensureArrayOfStrings(candidate: string | string[]): string[] {
    if (typeof candidate === "string") {
        return [candidate];
    }

    return candidate;
}
