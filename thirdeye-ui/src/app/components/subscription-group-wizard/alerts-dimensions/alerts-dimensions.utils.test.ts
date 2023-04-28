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

import { Association as FullAssociation } from "../subscription-group-wizard.interfaces";
import { getAssociationId } from "./alerts-dimensions.utils";

type Association = Pick<FullAssociation, "alertId" | "enumerationId">;

describe("Alerts Dimensions Utils", () => {
    it("Get appropriate association ID for both alert and enumeration ID", () => {
        expect(getAssociationId(mockAssociation1)).toEqual(mockAssociationId1);
    });

    it("Get appropriate association ID for just alert ID", () => {
        expect(getAssociationId(mockAssociation2)).toEqual(mockAssociationId2);
    });

    it("Get appropriate association ID for another pair of alert and enumeration ID", () => {
        expect(getAssociationId(mockAssociation3)).toEqual(mockAssociationId3);
    });
});

const mockAssociation1: Association = {
    alertId: 1,
    enumerationId: 100,
};

const mockAssociation2: Association = {
    alertId: 2,
};

const mockAssociation3: Association = {
    alertId: 999,
    enumerationId: 9999,
};

const mockAssociationId1 = "1-100";

const mockAssociationId2 = "2";

const mockAssociationId3 = "999-9999";
