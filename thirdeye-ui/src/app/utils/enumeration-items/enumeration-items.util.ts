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
import { DetectionEvaluationForRender } from "../../components/alert-view/enumeration-item-merger/enumeration-item-merger.interfaces";
import {
    DetectionEvaluation,
    EnumerationItemInEvaluation,
    EnumerationItemParams,
} from "../../rest/dto/detection.interfaces";
import { EnumerationItem } from "../../rest/dto/enumeration-item.interfaces";

const DISPLAY_SEPARATOR = " AND ";
const CLAUSE_SEPARATOR = "AND";
const SINGLE_QUOTE = "'";
const DOUBLE_QUOTE = '"';

export const generateNameFromParams = (
    enumerationItem: EnumerationItem,
    includeQuotes = true
): string => {
    return Object.entries(enumerationItem.params)
        .map(([key, value]) => {
            let displayValue: string | number = value;

            if (typeof value === "string") {
                if (includeQuotes) {
                    displayValue = `'${value}'`;
                } else {
                    displayValue = `${value}`;
                }
            }

            return `${key}=${displayValue}`;
        })
        .join(DISPLAY_SEPARATOR);
};

export const generateNameForEnumerationItem = (
    enumerationItem: EnumerationItem,
    includeQuotes = true
): string => {
    return (
        enumerationItem.name ??
        generateNameFromParams(enumerationItem, includeQuotes)
    );
};

export const generateNameForDetectionResult = (
    detectionEvaluation: DetectionEvaluation
): string => {
    if (detectionEvaluation.enumerationItem) {
        return generateNameForEnumerationItem(
            detectionEvaluation.enumerationItem as EnumerationItem
        );
    }

    return "";
};

export const parseSearchString = (
    searchTerm: string
): EnumerationItemParams => {
    const searchParams: EnumerationItemParams = {};
    const split = searchTerm.split(CLAUSE_SEPARATOR);

    split.forEach((clause) => {
        if (clause) {
            const [key, equalsValue] = clause.split("=");
            // Take on the value as a string if the criteria below don't match
            let parsedValue: string | number = equalsValue.trim();

            if (
                parsedValue.startsWith(SINGLE_QUOTE) ||
                parsedValue.startsWith(DOUBLE_QUOTE)
            ) {
                parsedValue = parsedValue
                    .substr(1)
                    .substr(0, parsedValue.length - 2);
            } else if (!isNaN(Number(parsedValue))) {
                parsedValue = Number(parsedValue);
            }

            searchParams[key.trim()] = parsedValue;
        }
    });

    return searchParams;
};

export const doesMatch = (
    candidate: EnumerationItemParams,
    criteria: EnumerationItemParams
): boolean => {
    let numLeftToMatch = Object.keys(criteria).length;

    Object.entries(criteria).forEach(([key, value]) => {
        if (candidate[key] === value) {
            numLeftToMatch--;
        }
    });

    return numLeftToMatch === 0;
};

export const doesMatchString = (
    candidate: EnumerationItemInEvaluation,
    criteria: string
): boolean => {
    const name = generateNameFromParams(candidate as EnumerationItem);
    const nameWithoutQuotes = generateNameFromParams(
        candidate as EnumerationItem,
        false
    );
    const lowerCasedCriteria = criteria.toLowerCase();

    return (
        name.toLowerCase().indexOf(lowerCasedCriteria) > -1 ||
        nameWithoutQuotes.toLowerCase().indexOf(lowerCasedCriteria) > -1
    );
};

export const filterEvaluations = (
    detectionEvaluations: DetectionEvaluationForRender[],
    term: string
): DetectionEvaluationForRender[] => {
    if (term) {
        return detectionEvaluations.filter((c) =>
            c.enumerationItem ? doesMatchString(c.enumerationItem, term) : false
        );
    }

    return detectionEvaluations;
};
