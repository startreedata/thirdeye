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
import { AlertTemplatePropertyValidationError } from "../../pages/alerts-create-page/alerts-create-page.interfaces";
import { MetadataProperty } from "../../rest/dto/alert-template.interfaces";
import { PropertyConfigValueTypes } from "../../rest/dto/alert.interfaces";

export function validateTemplateProperties(
    alertTemplateProperties: MetadataProperty[],
    propertyValues: { [index: string]: PropertyConfigValueTypes },
    translate: (
        key: string,
        params?: { [key: string]: string | number }
    ) => string
): AlertTemplatePropertyValidationError[] {
    const errors: AlertTemplatePropertyValidationError[] = [];

    Object.keys(propertyValues).forEach((propertyKey) => {
        const matchingPropertyMetadata = alertTemplateProperties.find(
            (propertyMetadataCandidate) =>
                propertyMetadataCandidate.name === propertyKey
        );

        if (matchingPropertyMetadata) {
            const valueForProperty = propertyValues[propertyKey];

            // Value needs to be in list of predefined options
            if (
                matchingPropertyMetadata.options &&
                matchingPropertyMetadata.options.length > 0
            ) {
                if (matchingPropertyMetadata.multiselect) {
                    if (!Array.isArray(valueForProperty)) {
                        errors.push({
                            key: propertyKey,
                            msg: translate("message.needs-to-be-an-array", {
                                propertyKey: propertyKey,
                            }),
                        });
                    } else {
                        const allValuesInOptions = (
                            valueForProperty as string[]
                        ).every((value) => {
                            return (
                                matchingPropertyMetadata.options as string[]
                            ).includes(value);
                        });

                        if (!allValuesInOptions) {
                            errors.push({
                                key: propertyKey,
                                msg: translate(
                                    "message.values-need-to-be-one-of-the-following-values",
                                    {
                                        propertyKey: propertyKey,
                                        expectedKeys:
                                            matchingPropertyMetadata.options.join(
                                                ","
                                            ),
                                    }
                                ),
                            });
                        }
                    }
                } else if (
                    !matchingPropertyMetadata.options.includes(
                        valueForProperty as string
                    )
                ) {
                    errors.push({
                        key: propertyKey,
                        msg: translate(
                            "message.values-need-to-be-one-of-the-following-values-it-is",
                            {
                                propertyKey: propertyKey,
                                expectedKeys:
                                    matchingPropertyMetadata.options.join(","),
                                valueForProperty:
                                    valueForProperty?.toString() as string,
                            }
                        ),
                    });
                }
                // Check if value is an array
            } else if (matchingPropertyMetadata.jsonType === "ARRAY") {
                if (!Array.isArray(valueForProperty)) {
                    errors.push({
                        key: propertyKey,
                        msg: translate("message.needs-to-be-an-array", {
                            propertyKey: propertyKey,
                        }),
                    });
                }
                // Check if boolean
            } else if (matchingPropertyMetadata.jsonType === "BOOLEAN") {
                if (typeof valueForProperty !== "boolean") {
                    errors.push({
                        key: propertyKey,
                        msg: translate(
                            "message.needs-to-be-a-boolean-true-or-false",
                            { propertyKey: propertyKey }
                        ),
                    });
                }
                // Check if object
            } else if (matchingPropertyMetadata.jsonType === "OBJECT") {
                if (typeof valueForProperty !== "object") {
                    errors.push({
                        key: propertyKey,
                        msg: translate("message.needs-to-be-an-object", {
                            propertyKey: propertyKey,
                        }),
                    });
                }
            }
        }
    });

    return errors;
}
