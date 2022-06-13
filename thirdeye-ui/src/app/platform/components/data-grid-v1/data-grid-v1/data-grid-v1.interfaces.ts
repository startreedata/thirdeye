// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { ReactNode } from "react";

export interface DataGridV1Props<T> {
    columns: DataGridColumnV1<T>[];
    data: T[] | null;
    rowKey: string; // Identifying property of data object
    expandColumnKey?: string; // Key of the column to display expand/collapse icon
    searchDataKeys?: string[]; // Properties of data object to search and filter, defaults to all the string/number/boolean type data object properties rendered in cells
    searchPlaceholder?: string; // Placeholder for search input
    searchDelay?: number; // Delay in milliseconds before search triggers
    searchFilterValue?: string | null;
    onSearchFilterValueChange?: (value: string) => void;
    disableSearch?: boolean;
    toolbarComponent?: ReactNode; // Toolbar items
    hideToolbar?: boolean;
    hideBorder?: boolean;
    className?: string;
    scroll?: DataGridScrollV1;
    initialSortState?: DataGridSortStateV1;
    disableMultiSelection?: boolean;
    disableSelection?: boolean;
    selectionModel?: DataGridSelectionModelV1<T>;
    onSelectionChange?: (selectionModel: DataGridSelectionModelV1<T>) => void;
    searchAndSelectionTextFn?: (
        totalCount: number,
        filteredCount: number,
        selectedCount: number
    ) => string; // Search and selection status text to render next to search input
    onRowExpand?: (expanded: boolean, data: T) => void;
}

export interface DataGridColumnV1<T> {
    key: string; // Identifier for column
    dataKey: string; // Property of data object to render in cell
    header?: string;
    headerTooltip?: boolean | string; // Set to false to not render header tooltip, or a string to render as tooltip
    cellTooltip?: boolean; // Set to false to not render cell tooltip
    align?: DataGridAlignmentV1;
    sortable?: boolean;
    sortComparatorFn?: (
        data1: T,
        data2: T,
        order: DataGridSortOrderV1
    ) => number; // Custom comparator to define sorting on data objects, data grid can sort string/number/boolean type data object properties by default
    minWidth: number;
    flex?: number;
    customHeaderRenderer?: (
        key: string, // Identifier for column
        column: DataGridColumnV1<T>
    ) => ReactNode; // Has precedence over header property
    customHeaderTooltipRenderer?: (
        key: string, // Identifier for column
        column: DataGridColumnV1<T>
    ) => ReactNode; // Has precedence over headerTooltip property
    customCellRenderer?: (
        cellValue: Record<string, unknown>,
        data: T,
        column: DataGridColumnV1<T>
    ) => ReactNode;
    customCellTooltipRenderer?: (
        cellValue: Record<string, unknown>,
        data: T,
        column: DataGridColumnV1<T>
    ) => ReactNode;
}

// Expand panel should also carry a unique identifier with same property name as the rest of the
// data grid data
export interface DataGridExpandPanelV1 {
    expandPanelContents?: ReactNode;
    expandPanelContentsRenderer?: (data: DataGridExpandPanelV1) => ReactNode;
}

export enum DataGridAlignmentV1 {
    Left = "left",
    Center = "center",
    Right = "right",
}

export enum DataGridScrollV1 {
    Body, // Grid expands to render all rows
    Contents, // Grid occupies the container and overflow rows scroll within
}

export interface DataGridSortStateV1 {
    key: string; // Key of the column to sort on
    order: DataGridSortOrderV1;
}

export enum DataGridSortOrderV1 {
    ASC = "asc",
    DESC = "desc",
}

export interface DataGridSelectionModelV1<T> {
    rowKeyValues: unknown[];
    rowKeyValueMap?: Map<unknown, T>;
}

export interface DataGridSelectionModelInternalV1<T> {
    rowKeyValues: Set<unknown>;
    rowKeyValueMap: Map<unknown, T>;
    dirty?: boolean;
}
