/*
 * Copyright 2023 StarTree Inc
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

import { cleanUpAssociations } from "./subscription-group-wizard.utils";

describe("Subscription Group Wizard Util", () => {
    it("cleanUpAssociations should return array without alert level association if enumeration item exists", () => {
        const result = cleanUpAssociations([
            {
                alert: { id: 1 },
            },
            {
                alert: { id: 2 },
            },
            {
                alert: { id: 1 },
                enumerationItem: { id: 1 },
            },
            {
                alert: { id: 1 },
                enumerationItem: { id: 2 },
            },
        ]);

        expect(result).toEqual([
            {
                alert: { id: 1 },
                enumerationItem: { id: 1 },
            },
            {
                alert: { id: 1 },
                enumerationItem: { id: 2 },
            },
            {
                alert: { id: 2 },
            },
        ]);
    });

    it("cleanUpAssociations should return array with enumeration item exists if only enumeration items exist", () => {
        const result = cleanUpAssociations([
            {
                alert: { id: 2 },
                enumerationItem: { id: 3 },
            },
            {
                alert: { id: 1 },
                enumerationItem: { id: 1 },
            },
            {
                alert: { id: 1 },
                enumerationItem: { id: 2 },
            },
        ]);

        expect(result).toEqual([
            {
                alert: { id: 2 },
                enumerationItem: { id: 3 },
            },
            {
                alert: { id: 1 },
                enumerationItem: { id: 1 },
            },
            {
                alert: { id: 1 },
                enumerationItem: { id: 2 },
            },
        ]);
    });

    it("cleanUpAssociations should return array alert level association if no enumeration item exists", () => {
        const result = cleanUpAssociations([
            {
                alert: { id: 1 },
            },
            {
                alert: { id: 2 },
            },
        ]);

        expect(result).toEqual([
            {
                alert: { id: 1 },
            },
            {
                alert: { id: 2 },
            },
        ]);
    });
});
