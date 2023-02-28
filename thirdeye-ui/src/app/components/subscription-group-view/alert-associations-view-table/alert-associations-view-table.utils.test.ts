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

import i18next from "i18next";
import { UiSubscriptionGroupAlert } from "../../../rest/dto/ui-subscription-group.interfaces";
import { UiAssociation } from "./alert-associations-view-table.interfaces";
import { getUiAssociation } from "./alert-associations-view-table.utils";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Alert Associations View Table Utils", () => {
    it("Returns appropriate ui associations for the input subscription group alerts", () => {
        expect(
            getUiAssociation(mockUiSubscriptionGroupAlerts1, i18next.t)
        ).toEqual(mockUiAssociations);
    });

    it("Returns empty output for empty input", () => {
        expect(getUiAssociation([], i18next.t)).toEqual([]);
    });

    it("Returns empty output for undefined", () => {
        expect(
            getUiAssociation(
                undefined as unknown as UiSubscriptionGroupAlert[],
                i18next.t
            )
        ).toEqual([]);
    });
});

const mockUiSubscriptionGroupAlerts1: UiSubscriptionGroupAlert[] = [
    {
        id: 1,
        name: "firstAlert",
    },
    {
        id: 2,
        name: "secondAlert",
        enumerationItems: [],
    },
    {
        id: 3,
        name: "thirdAlert",
        enumerationItems: [
            { id: 100, name: "enum1", params: {} },
            { id: 200, name: "enum2", params: { queryFilters: "abc" } },
        ],
    },
];

const mockUiAssociations: UiAssociation[] = [
    {
        alertId: 1,
        alertName: "firstAlert",
        id: "1",
        enumerationName: "label.overall-entity",
    },
    {
        alertId: 2,
        alertName: "secondAlert",
        id: "2",
        enumerationName: "label.overall-entity",
    },
    {
        alertId: 3,
        alertName: "thirdAlert",
        id: "3-100",
        enumerationId: 100,
        enumerationName: "enum1",
    },
    {
        alertId: 3,
        alertName: "thirdAlert",
        id: "3-200",
        enumerationId: 200,
        enumerationName: "enum2",
    },
];
