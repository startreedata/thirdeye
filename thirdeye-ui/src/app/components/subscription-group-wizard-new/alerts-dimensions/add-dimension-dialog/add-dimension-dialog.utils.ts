import { DataGridSelectionModelV1 } from "../../../../platform/components";
import { EnumerationItem } from "../../../../rest/dto/enumeration-item.interfaces";
import { getMapFromList } from "../../../../utils/subscription-groups/subscription-groups.util";
import { Association } from "../../subscription-group-wizard-new.interface";
import { DimensionRow } from "./add-dimension-dialog.interface";

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

export const getEnumerationItemName = (
    item?: EnumerationItem
): string | null => {
    if (!item) {
        return null;
    }

    const { params, name } = item;

    if (!params) {
        return name;
    }

    return Object.entries(params)
        .map(([k, v]) => `${k}=${v}`)
        .join(";");
};

export const getId = ({ alertId, enumerationId }: Association): string =>
    `${alertId}${enumerationId ? `-${enumerationId}` : ""}`;

export const getDimensionRow = (
    enumerationItem: EnumerationItem
): DimensionRow => ({
    id: enumerationItem.id,
    name: getEnumerationItemName(enumerationItem) as string,
});

export const getDimensionRows = (
    enumerationItems: EnumerationItem[]
): DimensionRow[] => enumerationItems.map(getDimensionRow);

export const ALL_DIMENSIONS_INDEX = -1;

export const getSelectedRows = ({
    associations = [],
    enumerationItems = [],
    allSelected = false,
    AllDimensionsSelectedRow,
}: {
    associations?: Association[];
    enumerationItems?: EnumerationItem[];
    allSelected?: boolean;
    AllDimensionsSelectedRow: DimensionRow;
}): DataGridSelectionModelV1<DimensionRow> => {
    const enumerationItemsMap = getMapFromList(enumerationItems);

    const rowKeyValues: unknown[] = allSelected
        ? [ALL_DIMENSIONS_INDEX]
        : associations.map((item) => item.enumerationId);

    const rowKeyValueMap = new Map<number, DimensionRow>(
        allSelected
            ? [[ALL_DIMENSIONS_INDEX, AllDimensionsSelectedRow]]
            : (
                  associations
                      .filter((item) => item.enumerationId)
                      .map((item) =>
                          enumerationItemsMap.get(item.enumerationId as number)
                      )
                      .filter((v) => v) as EnumerationItem[]
              ).map<[number, DimensionRow]>((enumerationItem) => [
                  enumerationItem?.id || ALL_DIMENSIONS_INDEX,
                  getDimensionRow(enumerationItem),
              ])
    );

    return {
        rowKeyValues,
        rowKeyValueMap,
    };
};
