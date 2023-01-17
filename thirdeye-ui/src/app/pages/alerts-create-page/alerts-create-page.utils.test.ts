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

import type { Alert, EditableAlert } from "../../rest/dto/alert.interfaces";
import { createAlertCopy } from "./alerts-create-page.utils";

describe("createAlertCopy test", () => {
    it("should copy the alert correctly, without the instance specific keys", () => {
        expect(createAlertCopy(mockAlert)).toEqual(mockEditableAlert);
    });

    it("should be able to copy an editable alert without issue if passed as a parameter", () => {
        expect(createAlertCopy(mockEditableAlert as Alert)).toEqual(
            mockEditableAlert
        );
    });
});

const mockAlert: Alert = {
    id: 73,
    name: "PageViewsDemo-Seasonal-Trend",
    description: "Uses template startree-ets",
    template: {
        name: "startree-ets",
    },
    templateProperties: {
        dataSource: "pinot",
        dataset: "CleanPageViewsData",
        aggregationFunction: "sum",
        aggregationColumn: "views",
        seasonalityPeriod: "P7D",
        lookback: "P28D",
        monitoringGranularity: "P1D",
        rcaAggregationFunction: "sum",
        sensitivity: "7",
    },
    cron: "0 0 0 1/1 * ? *",
    lastTimestamp: 1673913600000,
    active: true,
    created: 1668306830000,
    updated: 1673913602000,
    owner: {
        id: 100,
        principal: "madhumita@startree.ai",
    },
};

const mockEditableAlert: EditableAlert = {
    name: "PageViewsDemo-Seasonal-Trend",
    description: "Uses template startree-ets",
    template: {
        name: "startree-ets",
    },
    templateProperties: {
        dataSource: "pinot",
        dataset: "CleanPageViewsData",
        aggregationFunction: "sum",
        aggregationColumn: "views",
        seasonalityPeriod: "P7D",
        lookback: "P28D",
        monitoringGranularity: "P1D",
        rcaAggregationFunction: "sum",
        sensitivity: "7",
    },
    cron: "0 0 0 1/1 * ? *",
};
