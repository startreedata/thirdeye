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
import {
    findRequiredFields,
    hasRequiredPropertyValuesSet,
    setUpFieldInputRenderConfig,
} from "./alert-template.utils";

describe("AlertWizardV2/AlertTemplate Utils", () => {
    it("findRequiredFields should return array of fields in alphabetical order with no duplicates", () => {
        expect(findRequiredFields(MOCK_ALERT_TEMPLATE)).toEqual([
            "aggregationColumn",
            "completenessDelay",
            "dataSource",
            "dataset",
            "lookback",
            "monitoringGranularity",
            "pattern",
            "queryFilters",
            "seasonalityPeriod",
            "sensitivity",
            "timezone",
        ]);
    });

    it("findRequiredFields should return empty array if no template properties exist", () => {
        expect(findRequiredFields(MOCK_ALERT_EMPTY_TEMPLATE)).toEqual([]);
    });

    it("setUpFieldInputRenderConfig should return array with correct configs for required and optional fields", () => {
        expect(
            setUpFieldInputRenderConfig(
                ["requiredField", "optionalField"],
                { requiredField: "helloWorld" },
                { optionalField: "fooBar" }
            )
        ).toEqual([
            [
                {
                    key: "requiredField",
                    value: "helloWorld",
                    defaultValue: undefined,
                },
            ],
            [
                {
                    key: "optionalField",
                    value: undefined,
                    defaultValue: "fooBar",
                },
            ],
        ]);
    });

    it("hasRequiredPropertyValuesSet return true if required fields have an entry in templateProperties and defaultProperties", () => {
        expect(
            hasRequiredPropertyValuesSet(
                ["requiredField", "optionalField"],
                { requiredField: "helloWorld" },
                { optionalField: "fooBar" }
            )
        ).toEqual(true);
    });

    it("hasRequiredPropertyValuesSet return false if required field has no entry in templateProperties", () => {
        expect(
            hasRequiredPropertyValuesSet(
                ["requiredField", "optionalField"],
                {},
                { optionalField: "fooBar" }
            )
        ).toEqual(false);
    });

    it("hasRequiredPropertyValuesSet return true the only field is optional", () => {
        expect(
            hasRequiredPropertyValuesSet(
                ["optionalField"],
                {},
                { optionalField: "fooBar" }
            )
        ).toEqual(true);
    });

    it("hasRequiredPropertyValuesSet return true if a field is an array or boolean", () => {
        expect(
            hasRequiredPropertyValuesSet(
                ["arrayField", "booleanField"],
                {
                    arrayField: [],
                    booleanField: false,
                },
                {}
            )
        ).toEqual(true);
    });
});

const MOCK_ALERT_TEMPLATE = {
    id: 1,
    name: "startree-mean-variance-percentile",
    description:
        "Mean-Variance template. Aggregation function with 2 operands: PERCENTILETDIGEST, DISTINCTCOUNTHLL,etc...",
    nodes: [
        {
            name: "root",
            type: "AnomalyDetector",
            params: {
                "component.lookbackPeriod": "${lookback}",
                "component.lookbackPeriod2": "${lookback}",
                "component.monitoringGranularity": "${monitoringGranularity}",
                "component.monitoringGranularity2": "${monitoringGranularity}",
                "component.seasonalityPeriod": "${seasonalityPeriod}",
                "anomaly.metric": "${aggregationColumn}",
                "component.pattern": "${pattern}",
                "component.sensitivity": "${sensitivity}",
            },
            outputs: [],
        },
    ],
    metadata: {
        datasource: {
            name: "${dataSource}",
        },
        dataset: {
            name: "${dataset}",
            completenessDelay: "${completenessDelay}",
        },
        metric: {
            name: "${aggregationColumn}",
            where: "${queryFilters}",
        },
        granularity: "${monitoringGranularity}",
        timezone: "${timezone}",
    },
    defaultProperties: {
        completenessDelay: "P0D",
        timezone: "UTC",
        pattern: "UP_OR_DOWN",
        queryFilters: "",
        queryLimit: "100000000",
        seasonalityPeriod: "PT0S",
    },
};
const MOCK_ALERT_EMPTY_TEMPLATE = {
    id: 2,
    name: "startree-mean-variance-percentile",
    description:
        "Mean-Variance template. Aggregation function with 2 operands: PERCENTILETDIGEST, DISTINCTCOUNTHLL,etc...",
    nodes: [],
    defaultProperties: {
        completenessDelay: "P0D",
        timezone: "UTC",
        pattern: "UP_OR_DOWN",
        queryFilters: "",
        queryLimit: "100000000",
        seasonalityPeriod: "PT0S",
    },
};
