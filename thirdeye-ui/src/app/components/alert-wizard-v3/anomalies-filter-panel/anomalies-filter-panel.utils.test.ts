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

import { AlertTemplate } from "../../../rest/dto/alert-template.interfaces";
import { getAvailableFilterOptions } from "./anomalies-filter-panel.utils";

describe("Anomalies Filter Panel Utils", () => {
    it("getAvailableFilterOptions should return array of one object that matches FILTER step type", () => {
        expect(
            getAvailableFilterOptions(MOCK_ALERT_TEMPLATE_1, () => "")
        ).toEqual([
            {
                description: "",
                name: "Threshold",
                requiredPropertiesWithMetadata: [
                    {
                        defaultValue: "-1",
                        description: "",
                        jsonType: "NUMBER",
                        multiselect: false,
                        name: "thresholdFilterMin",
                        step: "FILTER",
                        subStep: "Threshold",
                    },
                    {
                        defaultValue: "-1",
                        description: "",
                        jsonType: "NUMBER",
                        multiselect: false,
                        name: "thresholdFilterMax",
                        step: "FILTER",
                        subStep: "Threshold",
                    },
                ],
            },
        ]);
    });

    it("getAvailableFilterOptions should return empty array if no FILTER step types", () => {
        expect(
            getAvailableFilterOptions(MOCK_ALERT_TEMPLATE_2, () => "")
        ).toEqual([]);
    });
});

const MOCK_ALERT_TEMPLATE_1 = {
    id: 1,
    name: "mockDatasource",
    properties: [
        {
            name: "thresholdFilterMin",
            description: "",
            defaultValue: "-1",
            jsonType: "NUMBER",
            step: "FILTER",
            subStep: "Threshold",
            multiselect: false,
        },
        {
            name: "thresholdFilterMax",
            description: "",
            defaultValue: "-1",
            jsonType: "NUMBER",
            step: "FILTER",
            subStep: "Threshold",
            multiselect: false,
        },
        {
            name: "someOther",
            description: "",
            defaultValue: "-1",
            jsonType: "NUMBER",
            step: "NOT_FILTER",
            multiselect: false,
        },
    ],
} as unknown as AlertTemplate;

const MOCK_ALERT_TEMPLATE_2 = {
    id: 1,
    name: "mockDatasource",
    properties: [
        {
            name: "thresholdFilterMin",
            description: "",
            defaultValue: "-1",
            jsonType: "NUMBER",
            step: "NOT_FILTER",
            multiselect: false,
        },
        {
            name: "thresholdFilterMax",
            description: "",
            defaultValue: "-1",
            jsonType: "NUMBER",
            step: "NOT_FILTER",
            multiselect: false,
        },
        {
            name: "someOther",
            description: "",
            defaultValue: "-1",
            jsonType: "NUMBER",
            step: "NOT_FILTER",
            multiselect: false,
        },
    ],
} as unknown as AlertTemplate;
