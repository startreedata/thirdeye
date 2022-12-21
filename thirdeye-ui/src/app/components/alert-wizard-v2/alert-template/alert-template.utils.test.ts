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
import type {
    AlertTemplate,
    MetadataProperty,
} from "../../../rest/dto/alert-template.interfaces";
import {
    determinePropertyFieldConfiguration,
    determinePropertyFieldConfigurationFromDefaultFields,
    determinePropertyFieldConfigurationFromProperties,
    findAvailableFields,
    hasRequiredPropertyValuesSet,
    setUpFieldInputRenderConfig,
} from "./alert-template.utils";

describe("AlertWizardV2/AlertTemplate Utils", () => {
    it("findAvailableFields should return array of fields in alphabetical order with no duplicates", () => {
        expect(findAvailableFields(MOCK_ALERT_TEMPLATE)).toEqual([
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

    it("findAvailableFields should return empty array if no template properties exist", () => {
        expect(findAvailableFields(MOCK_ALERT_EMPTY_TEMPLATE)).toEqual([]);
    });

    it("determinePropertyFieldConfigurationFromDefaultFields should return expected array of objects", () => {
        expect(
            determinePropertyFieldConfigurationFromDefaultFields(
                MOCK_ALERT_TEMPLATE_NO_PROPERTIES,
                MOCK_ALERT_TEMPLATE_NO_PROPERTIES.defaultProperties as {
                    [index: string]: string;
                }
            )
        ).toEqual([
            {
                isOptional: false,
                metadata: {
                    defaultIsNull: false,
                    multiselect: false,
                    name: "dataSource",
                },
                name: "dataSource",
            },
            {
                isOptional: true,
                metadata: {
                    defaultIsNull: false,
                    defaultValue: "P0D",
                    multiselect: false,
                    name: "lookback",
                },
                name: "lookback",
            },
            {
                isOptional: true,
                metadata: {
                    defaultIsNull: false,
                    defaultValue: "UTC",
                    multiselect: false,
                    name: "monitoringGranularity",
                },
                name: "monitoringGranularity",
            },
            {
                isOptional: false,
                metadata: {
                    defaultIsNull: false,
                    multiselect: false,
                    name: "seasonalityPeriod",
                },
                name: "seasonalityPeriod",
            },
        ]);
    });

    it("determinePropertyFieldConfigurationFromProperties should return expected array of objects", () => {
        expect(
            determinePropertyFieldConfigurationFromProperties(
                MOCK_ALERT_TEMPLATE_WITH_PROPERTIES_AND_DEFAULT_PROPERTIES.properties
            )
        ).toEqual([
            {
                isOptional: true,
                metadata: {
                    defaultIsNull: false,
                    defaultValue: "UTC",
                    description: "Timezone used to group by time.",
                    jsonType: "STRING",
                    name: "timezone",
                },
                name: "timezone",
            },
            {
                isOptional: true,
                metadata: {
                    defaultIsNull: false,
                    defaultValue: "AUTO",
                    description:
                        "TimeColumn to use to group by time. If set to AUTO (the default value), the Pinot primary time column is used.",
                    jsonType: "STRING",
                    name: "timeColumn",
                },
                name: "timeColumn",
            },
        ]);
    });

    it("determinePropertyFieldConfiguration should return expected array of objects", () => {
        expect(
            determinePropertyFieldConfiguration(
                MOCK_ALERT_TEMPLATE_WITH_PROPERTIES_AND_DEFAULT_PROPERTIES
            )
        ).toEqual([
            {
                isOptional: true,
                metadata: {
                    defaultIsNull: false,
                    defaultValue: "P0D",
                    multiselect: false,
                    name: "completenessDelay",
                },
                name: "completenessDelay",
            },
            {
                isOptional: false,
                metadata: {
                    defaultIsNull: false,
                    multiselect: false,
                    name: "dataSource",
                },
                name: "dataSource",
            },
            {
                isOptional: false,
                metadata: {
                    defaultIsNull: false,
                    multiselect: false,
                    name: "dataset",
                },
                name: "dataset",
            },
            {
                isOptional: true,
                metadata: {
                    defaultIsNull: false,
                    defaultValue: "90D",
                    multiselect: false,
                    name: "lookback",
                },
                name: "lookback",
            },
            {
                isOptional: false,
                metadata: {
                    defaultIsNull: false,
                    multiselect: false,
                    name: "monitoringGranularity",
                },
                name: "monitoringGranularity",
            },
            {
                isOptional: true,
                metadata: {
                    defaultIsNull: false,
                    defaultValue: "UTC",
                    description: "Timezone used to group by time.",
                    jsonType: "STRING",
                    name: "timezone",
                },
                name: "timezone",
            },
            {
                isOptional: true,
                metadata: {
                    defaultIsNull: false,
                    defaultValue: "AUTO",
                    description:
                        "TimeColumn to use to group by time. If set to AUTO (the default value), the Pinot primary time column is used.",
                    jsonType: "STRING",
                    name: "timeColumn",
                },
                name: "timeColumn",
            },
        ]);
    });

    it("setUpFieldInputRenderConfig should return array with correct configs for required and optional fields", () => {
        expect(
            setUpFieldInputRenderConfig(
                [REQUIRED_FIELD_METADATA, OPTIONAL_FIELD_METADATA],
                { requiredField: "helloWorld" }
            )
        ).toEqual([
            [
                {
                    key: "requiredField",
                    metadata: {
                        defaultIsNull: false,
                        multiselect: false,
                        name: "requiredField",
                    },
                    value: "helloWorld",
                },
            ],
            [
                {
                    key: "optionalField",
                    metadata: {
                        defaultIsNull: false,
                        defaultValue: "fooBar",
                        multiselect: false,
                        name: "optionalField",
                    },
                },
            ],
        ]);
    });

    it("hasRequiredPropertyValuesSet return true if required fields have an entry in templateProperties and defaultProperties", () => {
        expect(
            hasRequiredPropertyValuesSet(
                [REQUIRED_FIELD_METADATA, OPTIONAL_FIELD_METADATA],
                { requiredField: "helloWorld" }
            )
        ).toEqual(true);
    });

    it("hasRequiredPropertyValuesSet return false if required field has no entry in templateProperties", () => {
        expect(
            hasRequiredPropertyValuesSet(
                [REQUIRED_FIELD_METADATA, OPTIONAL_FIELD_METADATA],
                {}
            )
        ).toEqual(false);
    });

    it("hasRequiredPropertyValuesSet return true the only field is optional", () => {
        expect(
            hasRequiredPropertyValuesSet([OPTIONAL_FIELD_METADATA], {})
        ).toEqual(true);
    });

    it("hasRequiredPropertyValuesSet return true if a field is an array or boolean", () => {
        expect(
            hasRequiredPropertyValuesSet(
                [
                    {
                        name: "arrayField",
                        isOptional: false,
                        metadata: {
                            name: "arrayField",
                            defaultIsNull: false,
                            multiselect: false,
                        },
                    },
                    {
                        name: "booleanField",
                        isOptional: false,
                        metadata: {
                            name: "booleanField",
                            defaultIsNull: false,
                            multiselect: false,
                        },
                    },
                ],
                {
                    arrayField: [],
                    booleanField: false,
                }
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

const MOCK_ALERT_TEMPLATE_NO_PROPERTIES = {
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
                "component.monitoringGranularity": "${monitoringGranularity}",
                "component.seasonalityPeriod": "${seasonalityPeriod}",
            },
            outputs: [],
        },
    ],
    metadata: {
        datasource: {
            name: "${dataSource}",
        },
        dataset: {
            name: "dataset",
        },
        metric: {
            name: "metric",
        },
    },
    defaultProperties: {
        lookback: "P0D",
        monitoringGranularity: "UTC",
    },
} as AlertTemplate;

const MOCK_ALERT_TEMPLATE_WITH_PROPERTIES_AND_DEFAULT_PROPERTIES = {
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
            name: "metric",
        },
        granularity: "granularity",
        timezone: "${timezone}",
    },
    defaultProperties: {
        completenessDelay: "P0D",
        lookback: "90D",
    },
    properties: [
        {
            name: "timezone",
            description: "Timezone used to group by time.",
            defaultValue: "UTC",
            defaultIsNull: false,
            jsonType: "STRING",
        },
        {
            name: "timeColumn",
            description:
                "TimeColumn to use to group by time. If set to AUTO (the default value), the Pinot primary time column is used.",
            defaultValue: "AUTO",
            defaultIsNull: false,
            jsonType: "STRING",
        },
    ] as MetadataProperty[],
};

const OPTIONAL_FIELD_METADATA = {
    name: "optionalField",
    isOptional: true,
    metadata: {
        name: "optionalField",
        defaultValue: "fooBar",
        defaultIsNull: false,
        multiselect: false,
    },
};

const REQUIRED_FIELD_METADATA = {
    name: "requiredField",
    isOptional: false,
    metadata: {
        name: "requiredField",
        defaultIsNull: false,
        multiselect: false,
    },
};
