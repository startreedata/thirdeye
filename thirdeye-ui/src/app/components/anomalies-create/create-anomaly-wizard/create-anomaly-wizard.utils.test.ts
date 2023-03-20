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

import { Alert } from "../../../rest/dto/alert.interfaces";
import {
    AnomalyResultSource,
    EditableAnomaly,
} from "../../../rest/dto/anomaly.interfaces";
import { Metric } from "../../../rest/dto/metric.interfaces";
import {
    createEditableAnomaly,
    getEnumerationItemsConfigFromAlert,
    getIsAnomalyValid,
} from "./create-anomaly-wizard.utils";

describe("CreateAnomalyWizard/Utils", () => {
    it("getEnumerationItemsConfigFromAlert returns null for no templateProperties data", () => {
        expect(getEnumerationItemsConfigFromAlert(dummyAlert1)).toBeNull();
    });

    it("getEnumerationItemsConfigFromAlert returns null for no templateProperties.enumerationItems data", () => {
        expect(getEnumerationItemsConfigFromAlert(dummyAlert2)).toBeNull();
    });

    it("getEnumerationItemsConfigFromAlert returns null for empty templateProperties.enumerationItems list", () => {
        expect(getEnumerationItemsConfigFromAlert(dummyAlert3)).toBeNull();
    });

    it("getEnumerationItemsConfigFromAlert returns the proper values for non-empty templateProperties.enumerationItems list", () => {
        expect(getEnumerationItemsConfigFromAlert(dummyAlert4)).toEqual([
            { name: "dummyAlert4-firstEnumerationItem" },
            { name: "dummyAlert4-secondEnumerationItem" },
        ]);
    });

    it("getIsAnomalyValid should return false for null anomaly data", () => {
        expect(getIsAnomalyValid(null)).toEqual(false);
    });

    it("getIsAnomalyValid should return false for empty anomaly data object", () => {
        expect(getIsAnomalyValid(editableAnomaly2)).toEqual(false);
    });

    it("getIsAnomalyValid should return false for invalid start and end dateTime", () => {
        expect(getIsAnomalyValid(editableAnomaly3)).toEqual(false);
    });

    it("getIsAnomalyValid should return false for startTime > endTime", () => {
        expect(getIsAnomalyValid(editableAnomaly4)).toEqual(false);
    });

    it("getIsAnomalyValid should return true for valid anomaly data", () => {
        expect(getIsAnomalyValid(editableAnomaly1)).toEqual(true);
    });

    it("createEditableAnomaly should create an appropriate anomaly for the params passed", () => {
        expect(createEditableAnomaly(editableAnomalyPayload1)).toEqual(
            editableAnomaly1
        );
    });
});

const dummyAlert1 = {
    id: 1,
} as Alert;

const dummyAlert2 = {
    id: 1,
    templateProperties: {},
} as Alert;

const dummyAlert3 = {
    id: 1,
    templateProperties: {
        enumerationItems: [],
    },
} as unknown as Alert;

const dummyAlert4 = {
    id: 1,
    templateProperties: {
        enumerationItems: [
            { name: "dummyAlert4-firstEnumerationItem" },
            { name: "dummyAlert4-secondEnumerationItem" },
        ],
    },
} as unknown as Alert;

const editableAnomalyPayload1 = {
    alert: {
        name: "dummyAlertNameForEditableAnomaly1",
        id: 111,
        templateProperties: {
            dataset: "dummyDatasetNameForEditableAnomaly1",
            aggregationColumn: "dummyMetricNameForEditableAnomaly1",
        },
    } as unknown as Alert,
    startTime: 101010,
    endTime: 101100,
};

const editableAnomaly1 = {
    alert: {
        name: "dummyAlertNameForEditableAnomaly1",
        id: 111,
        templateProperties: {
            dataset: "dummyDatasetNameForEditableAnomaly1",
            aggregationColumn: "dummyMetricNameForEditableAnomaly1",
        },
    },
    startTime: 101010,
    endTime: 101100,
    sourceType: AnomalyResultSource.USER_LABELED_ANOMALY,
    metric: {
        name: "dummyMetricNameForEditableAnomaly1",
    } as Metric,
    metadata: {
        metric: {
            name: "dummyMetricNameForEditableAnomaly1",
        },
        dataset: {
            name: "dummyDatasetNameForEditableAnomaly1",
        },
    },

    avgBaselineVal: 0,
    avgCurrentVal: 0,
} as EditableAnomaly;

const editableAnomaly2 = {} as EditableAnomaly;

const editableAnomaly3 = {
    alert: {
        id: 333,
    },
    startTime: -1,
    endTime: -1,
} as EditableAnomaly;

const editableAnomaly4 = {
    alert: {
        id: 555,
    },
    startTime: 100,
    endTime: 50,
} as EditableAnomaly;
