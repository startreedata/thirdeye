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

import { DataGridSelectionModelV1 } from "../../../../platform/components";
import { Alert } from "../../../../rest/dto/alert.interfaces";
import { EnumerationItem } from "../../../../rest/dto/enumeration-item.interfaces";
import { generateNameForEnumerationItem } from "../../../../utils/enumeration-items/enumeration-items.util";
import { getMapFromList } from "../../../../utils/subscription-groups/subscription-groups.util";
import { Association } from "../../subscription-group-wizard.interfaces";
import { getAssociationId } from "../alerts-dimensions.utils";
import { DimensionRow } from "./add-dimension-dialog.interfaces";

export const getDimensionRow = (
    enumerationItem: EnumerationItem,
    alertId: number
): DimensionRow => ({
    id: getAssociationId({ alertId, enumerationId: enumerationItem.id }),
    name: generateNameForEnumerationItem(enumerationItem, true),
});

export const getDimensionRows = (
    enumerationItems: EnumerationItem[],
    alertId: number
): DimensionRow[] =>
    enumerationItems.map((item) => getDimensionRow(item, alertId));

export const ALL_DIMENSIONS_INDEX = -1;

export const getSelectedRows = ({
    associations = [],
    enumerationItems = [],
    allSelected = false,
    allDimensionsSelectedRow,
    selectedAlertId,
}: {
    associations?: Association[];
    enumerationItems?: EnumerationItem[];
    allSelected?: boolean;
    allDimensionsSelectedRow: DimensionRow;
    selectedAlertId: number;
}): DataGridSelectionModelV1<DimensionRow> => {
    const enumerationItemsMap = getMapFromList(enumerationItems);

    const rowKeyValues: unknown[] = allSelected
        ? [ALL_DIMENSIONS_INDEX]
        : associations.map((item) => getAssociationId(item));

    const rowKeyValueMap = new Map<number, DimensionRow>(
        allSelected
            ? [[selectedAlertId, allDimensionsSelectedRow]]
            : (
                  associations
                      .filter((item) => item.enumerationId)
                      .map((item) =>
                          enumerationItemsMap.get(item.enumerationId as number)
                      )
                      .filter((v) => v) as EnumerationItem[]
              ).map<[number, DimensionRow]>((enumerationItem) => [
                  enumerationItem?.id || ALL_DIMENSIONS_INDEX,
                  getDimensionRow(enumerationItem, selectedAlertId),
              ])
    );

    return {
        rowKeyValues,
        rowKeyValueMap,
    };
};

export const getEnumerationItemsForAlert = (
    enumerationItems: EnumerationItem[],
    alert: Alert
): EnumerationItem[] => {
    return (enumerationItems || []).filter(
        (enumerationItem) => enumerationItem.alert?.id === alert.id
    );
};

export const getAssociationIdsForAlert = ({
    enumerationIds,
    alertId,
}: {
    enumerationIds: number[];
    alertId: number;
}): string[] =>
    [undefined, ...enumerationIds].map((enumerationId) =>
        getAssociationId({
            alertId,
            enumerationId,
        })
    );

export const getDataRowsForAlert = ({
    allDimensionsSelectedRow,
    allSelected,
    enumerationItemsForAlert,
    alertId,
}: {
    allDimensionsSelectedRow: DimensionRow;
    allSelected: boolean;
    enumerationItemsForAlert: EnumerationItem[];
    alertId: number;
}): DimensionRow[] => [
    allDimensionsSelectedRow,
    ...(allSelected ? [] : getDimensionRows(enumerationItemsForAlert, alertId)),
];

export const areAllRowsSelected = ({
    selectedRow,
    alertId,
}: {
    selectedRow: DataGridSelectionModelV1<DimensionRow>;
    alertId: number;
}): boolean => selectedRow.rowKeyValues.includes(getAssociationId({ alertId }));
