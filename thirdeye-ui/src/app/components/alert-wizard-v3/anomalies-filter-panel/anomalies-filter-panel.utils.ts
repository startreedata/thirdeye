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

import { TFunction } from "react-i18next";
import {
    AlertTemplate,
    MetadataProperty,
} from "../../../rest/dto/alert-template.interfaces";
import { AnomaliesFilterConfiguratorRenderConfigs } from "./anomalies-filter-panel.interfaces";

export const getAvailableFilterOptions = (
    alertTemplate: AlertTemplate,
    translation: TFunction
): AnomaliesFilterConfiguratorRenderConfigs[] => {
    if (alertTemplate.properties === undefined) {
        return [];
    }

    const filteringPropertiesBySubStep: { [key: string]: MetadataProperty[] } =
        {};

    alertTemplate.properties.forEach((property) => {
        if (property.step === "FILTER" && property.subStep) {
            const bucket = filteringPropertiesBySubStep[property.subStep] || [];
            bucket.push(property);
            filteringPropertiesBySubStep[property.subStep] = bucket;
        }
    });

    return Object.keys(filteringPropertiesBySubStep).map((filterName) => {
        const descriptionLookupId = `message.${filterName
            .toLowerCase()
            .split(" ")
            .join("-")}-filter-description`;
        const descriptionLookupResult = translation(descriptionLookupId);

        return {
            name: filterName,
            description:
                descriptionLookupId === descriptionLookupResult
                    ? null
                    : descriptionLookupResult,
            requiredPropertiesWithMetadata:
                filteringPropertiesBySubStep[filterName],
        };
    });
};
