/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Box, Checkbox, Toolbar, Typography } from "@material-ui/core";
import CheckBoxIcon from "@material-ui/icons/CheckBox";
import IndeterminateCheckBoxIcon from "@material-ui/icons/IndeterminateCheckBox";
import { Skeleton } from "@material-ui/lab";
import classNames from "classnames";
import {
    cloneDeep,
    debounce,
    delay,
    get,
    isEmpty,
    isNil,
    some,
    toString,
} from "lodash";
import React, {
    ReactElement,
    ReactNode,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import Table, { AutoResizer, Column } from "react-base-table";
import {
    booleanSortComparatorV1,
    numberSortComparatorV1,
    stringSortComparatorV1,
} from "../../../utils/comparator";
import { BorderV1 } from "../../../utils/material-ui/border.util";
import { SearchInputV1 } from "../../search-input-v1/search-input-v1.component";
import { TooltipV1 } from "../../tooltip-v1/tooltip-v1.component";
import { DataGridExpandIconV1 } from "../data-grid-expand-icon-v1/data-grid-expand-icon-v1.component";
import { DataGridSortIndicatorV1 } from "../data-grid-sort-indicator-v1/data-grid-sort-indicator-v1.component";
import {
    DataGridAlignmentV1,
    DataGridColumnV1,
    DataGridExpandPanelV1,
    DataGridScrollV1,
    DataGridSelectionModelInternalV1,
    DataGridSortOrderV1,
    DataGridSortStateV1,
    DataGridV1Props,
} from "./data-grid-v1.interfaces";
import {
    HEIGHT_DATA_GRID_HEADER_ROW,
    HEIGHT_DATA_GRID_ROW,
    HEIGHT_DATA_GRID_TOOLBAR,
    useDataGridV1Styles,
    WIDTH_DATA_GRID_ROW_SELECTION_COLUMN,
} from "./data-grid-v1.styles";
import "./data-grid-v1.styles.scss";

const KEY_VALUE_ROW_SELECTION_COLUMN = "row-selection-column";
const ROW_COUNT_PERFORMANCE_THRESHOLD = 100;

export function DataGridV1<T>({
    columns,
    data,
    rowKey,
    expandColumnKey,
    searchDataKeys,
    searchPlaceholder,
    searchDelay,
    disableSearch,
    toolbarComponent,
    hideToolbar,
    hideBorder,
    className,
    scroll,
    initialSortState,
    disableMultiSelection,
    disableSelection,
    selectionModel,
    onSelectionChange,
    searchAndSelectionTextFn,
    onRowExpand,
    searchFilterValue,
    onSearchFilterValueChange,

    ...otherProps
}: DataGridV1Props<T>): ReactElement {
    const dataGridV1Classes = useDataGridV1Styles();
    const [fixedTable, setFixedTable] = useState(true);
    const [searchDataKeysInternal, setSearchDataKeysInternal] = useState<
        string[]
    >([]);
    const [searchValue, setSearchValue] = useState(searchFilterValue || "");
    const [filteredData, setFilteredData] = useState<T[]>([]);
    const [sortState, setSortState] = useState<DataGridSortStateV1>(
        initialSortState || ({} as DataGridSortStateV1)
    );
    const [rowKeyValueMap, setRowKeyValueMap] = useState<Map<unknown, T>>(
        new Map()
    );
    const [selectionModelInternal, setSelectionModelInternal] = useState<
        DataGridSelectionModelInternalV1<T>
    >({
        rowKeyValues: new Set(),
        rowKeyValueMap: new Map(),
    });
    const [filteredDataSelectionCount, setFilteredDataSelectionCount] =
        useState(0);
    const [rowsRenderedState, setRowsRenderedState] = useState(false);
    const [gridContainerHeight, setGridContainerHeight] = useState<
        number | string
    >("100%");
    const [tableRef, setTableRef] = useState<Table<unknown> | null>(null);

    // Data to be rendered as loading indicator
    const loadingIndicatorData = [{ id: 0 }, { id: 1 }, { id: 2 }];

    useEffect(() => {
        if (!tableRef) {
            setGridContainerHeight("100%");

            return;
        }

        // Input/rendered data changed, recalculate grid container height
        if (data && data.length > ROW_COUNT_PERFORMANCE_THRESHOLD) {
            // For number of rows above set threshold, calculating grid container height freezes the
            // UI
            setGridContainerHeight("100%");

            return;
        }

        if (scroll === DataGridScrollV1.Body) {
            // Data grid container height must be set to the grid height so that the page expands to
            // accommodate the grid
            const dataGridToolbarHeight = hideToolbar
                ? 0
                : HEIGHT_DATA_GRID_TOOLBAR;
            setGridContainerHeight(
                dataGridToolbarHeight +
                    HEIGHT_DATA_GRID_HEADER_ROW +
                    tableRef.getTotalRowsHeight()
            );

            return;
        }

        // Data grid container height must be set to the parent container height and grid contents
        // can scroll within
        setGridContainerHeight("100%");
    }, [hideToolbar, scroll, filteredData, rowsRenderedState, tableRef]);

    useEffect(() => {
        if (!selectionModelInternal.dirty) {
            return;
        }

        // Selection model changed, notify
        onSelectionChange &&
            onSelectionChange({
                rowKeyValues: [...selectionModelInternal.rowKeyValues.values()],
                rowKeyValueMap: selectionModelInternal.rowKeyValueMap,
            });
    }, [selectionModelInternal]);

    // Utilities

    const isSearchDisabled = useCallback(() => {
        return disableSearch || hideToolbar;
    }, [disableSearch, hideToolbar]);

    const isRowSelected = useCallback(
        (rowKeyValue) => {
            return selectionModelInternal.rowKeyValues.has(rowKeyValue);
        },
        [selectionModelInternal]
    );

    const isRowSelectionComplete = useCallback(() => {
        if (!data) {
            return false;
        }

        return (
            !isEmpty(data) &&
            data.length === selectionModelInternal.rowKeyValues.size
        );
    }, [data, selectionModelInternal]);

    const isFilteredRowSelectionComplete = useCallback(() => {
        return filteredData.length === filteredDataSelectionCount;
    }, [filteredData, filteredDataSelectionCount]);

    const isRowSelectionIndeterminate = useCallback(() => {
        if (!data) {
            return false;
        }

        return (
            !isEmpty(data) &&
            data.length > selectionModelInternal.rowKeyValues.size &&
            selectionModelInternal.rowKeyValues.size > 0
        );
    }, [data, selectionModelInternal]);

    const isRowSelectionHeaderDisabled = useCallback(() => {
        return disableMultiSelection || isEmpty(filteredData);
    }, [disableMultiSelection, filteredData]);

    const getSortedData = (data: T[]): T[] => {
        if (isEmpty(data)) {
            return [];
        }

        if (isEmpty(columns) || isEmpty(sortState)) {
            // Sort not defined
            return [...data];
        }

        // Get column to sort on
        const column = columns.find(
            (eachColumn) => eachColumn.key === sortState.key
        );
        if (!column) {
            // Column to sort on not found
            return [...data];
        }

        // Custom sort, if available
        const sortComparatorFn = column.sortComparatorFn;
        if (sortComparatorFn) {
            return getSortedDataCustom(data, sortComparatorFn, sortState.order);
        }

        // Get data key to sort filtered data
        const dataKey = column.dataKey;
        const dataKeyValue = get(data[0], dataKey);
        if (
            typeof dataKeyValue === "string" ||
            typeof dataKeyValue === "number" ||
            typeof dataKeyValue === "boolean"
        ) {
            return getSortedDataDefault(data, dataKey, sortState.order);
        }

        return [...data];
    };

    const getSortedDataDefault = (
        data: T[],
        dataKey: string,
        order: DataGridSortOrderV1
    ): T[] => {
        return [...data].sort((data1, data2) => {
            const dataKeyValue1 = get(data1, dataKey);
            const dataKeyValue2 = get(data2, dataKey);
            if (typeof dataKeyValue1 === "number") {
                // Sort as numbers
                return numberSortComparatorV1(
                    dataKeyValue1,
                    dataKeyValue2,
                    order
                );
            }

            if (typeof dataKeyValue1 === "boolean") {
                // Sort as booleans
                return booleanSortComparatorV1(
                    dataKeyValue1,
                    dataKeyValue2,
                    order
                );
            }

            // Sort as strings
            return stringSortComparatorV1(dataKeyValue1, dataKeyValue2, order);
        });
    };

    const getSortedDataCustom = (
        data: T[],
        sortComparatorFn: (
            data1: T,
            data2: T,
            order: DataGridSortOrderV1
        ) => number,
        order: DataGridSortOrderV1
    ): T[] => {
        return [...data].sort((data1, data2) =>
            sortComparatorFn(data1, data2, order)
        );
    };

    const getRowClassName = (handlerProps: Record<string, unknown>): string => {
        if (disableSelection) {
            return "";
        }

        // Get row key value
        const rowKeyValue = get(handlerProps.rowData, rowKey);
        if (isRowSelected(rowKeyValue)) {
            // Row selected
            return dataGridV1Classes.dataGridRowSelected;
        }

        return "";
    };

    // Event handlers

    const handleRowSelectionHeaderClick = useCallback(() => {
        setSelectionModelInternal((draftSelectionModel) => {
            const rowKeyValues = cloneDeep(draftSelectionModel.rowKeyValues);
            const rowKeyValueMap = cloneDeep(
                draftSelectionModel.rowKeyValueMap
            );

            // Set or clear selection for all filtered rows
            for (const eachFilteredData of filteredData) {
                const rowKeyValue = get(eachFilteredData, rowKey);
                if (isFilteredRowSelectionComplete()) {
                    // Clear selection for filtered row
                    rowKeyValues.delete(rowKeyValue);
                    rowKeyValueMap.delete(rowKeyValue);

                    continue;
                }

                // Set selection for filtered row
                rowKeyValues.add(rowKeyValue);
                rowKeyValueMap.set(rowKeyValue, eachFilteredData);
            }

            if (isFilteredRowSelectionComplete()) {
                // Cleared selection for all filtered rows
                setFilteredDataSelectionCount(0);
            } else {
                // Set selection for all filtered rows
                setFilteredDataSelectionCount(filteredData.length);
            }

            return {
                rowKeyValues: rowKeyValues,
                rowKeyValueMap: rowKeyValueMap,
                dirty: true,
            };
        });
    }, [
        // filteredData dependency required, indirectly included with isFilteredRowSelectionComplete
        rowKey,
        isFilteredRowSelectionComplete,
    ]);

    const handleRowSelectionCellClick = useCallback(
        (data) => {
            // Toggle selection for row
            setSelectionModelInternal((draftSelectionModel) => {
                const rowKeyValues = cloneDeep(
                    draftSelectionModel.rowKeyValues
                );
                const rowKeyValueMap = cloneDeep(
                    draftSelectionModel.rowKeyValueMap
                );

                const rowKeyValue = get(data, rowKey);
                if (rowKeyValues.has(rowKeyValue)) {
                    // Clear selection for row
                    rowKeyValues.delete(rowKeyValue);
                    rowKeyValueMap.delete(rowKeyValue);

                    // Update filtered row selection count
                    setFilteredDataSelectionCount(
                        (draftFilteredRowSelectionCount) =>
                            --draftFilteredRowSelectionCount
                    );

                    return {
                        rowKeyValues: rowKeyValues,
                        rowKeyValueMap: rowKeyValueMap,
                        dirty: true,
                    };
                }

                // If multiple selection is disabled, clear existing selection before recording the
                // new one
                if (disableMultiSelection) {
                    rowKeyValues.clear();
                    rowKeyValueMap.clear();
                }

                // Set selection for row
                rowKeyValues.add(rowKeyValue);
                rowKeyValueMap.set(rowKeyValue, data);

                // Update filtered row selection count
                setFilteredDataSelectionCount(
                    (draftFilteredRowSelectionCount) =>
                        ++draftFilteredRowSelectionCount
                );

                return {
                    rowKeyValues: rowKeyValues,
                    rowKeyValueMap: rowKeyValueMap,
                    dirty: true,
                };
            });
        },
        [rowKey]
    );

    const handleTableRefDebounced = useCallback(
        debounce((tableRef) => {
            if (!tableRef) {
                return;
            }

            setTableRef(tableRef);
        }),
        []
    );

    const handleColumnSort = (handlerProps: Record<string, unknown>): void => {
        setSortState({
            key: handlerProps.key as string,
            order: handlerProps.order as DataGridSortOrderV1,
        });
    };

    const handleRowExpand = (handlerProps: Record<string, unknown>): void => {
        // Trigger grid container height calculations
        // Delay ensures the expanded row is completely rendered
        delay(() => {
            setRowsRenderedState(
                (draftRowsRenderedState) => !draftRowsRenderedState
            );
        }, 500);

        onRowExpand &&
            onRowExpand(
                handlerProps.expanded as boolean,
                handlerProps.rowData as T
            );
    };

    // Custom renderers

    const rowRendererDefault = (
        rendererProps: Record<string, unknown>
    ): ReactNode => {
        // Determine whether a regular row or an expand panel is being rendered
        const expandPanel = rendererProps.rowData as DataGridExpandPanelV1;
        if (
            expandPanel.expandPanelContents ||
            expandPanel.expandPanelContentsRenderer
        ) {
            // Expand panel
            return (
                <div
                    className={classNames(
                        dataGridV1Classes.dataGridExpandPanel,
                        "data-grid-v1-expand-panel"
                    )}
                >
                    {expandPanel.expandPanelContentsRenderer
                        ? expandPanel.expandPanelContentsRenderer(expandPanel)
                        : expandPanel.expandPanelContents}
                </div>
            );
        }

        // Regular row
        return rendererProps.cells as ReactNode;
    };

    const headerRendererDefault = (
        rendererProps: Record<string, unknown>
    ): ReactNode => {
        // Get column
        const column = rendererProps.column as DataGridColumnV1<T>;

        // Get header contents
        const headerContents = column.customHeaderRenderer
            ? column.customHeaderRenderer(column.key, column)
            : column.header;
        const headerTooltipContents = column.customHeaderTooltipRenderer
            ? column.customHeaderTooltipRenderer(column.key, column)
            : typeof column.headerTooltip === "string"
            ? column.headerTooltip
            : headerContents;

        // Determine tooltip visibility
        const headerTooltipVisible = isNil(column.headerTooltip)
            ? true
            : typeof column.headerTooltip === "boolean"
            ? column.headerTooltip
            : true;

        if (headerTooltipVisible && headerTooltipContents) {
            // Render with a tooltip
            return (
                <TooltipV1
                    className="data-grid-v1-header-tooltip"
                    title={headerTooltipContents}
                >
                    <div
                        className={classNames(
                            dataGridV1Classes.dataGridCell,
                            "data-grid-v1-header"
                        )}
                    >
                        {headerContents}
                    </div>
                </TooltipV1>
            );
        }

        // Render without a tooltip
        return (
            <div
                className={classNames(
                    dataGridV1Classes.dataGridCell,
                    "data-grid-v1-header"
                )}
            >
                {headerContents}
            </div>
        );
    };

    const headerRendererRowSelection = useCallback(() => {
        return (
            <Checkbox
                checked={isRowSelectionComplete()}
                checkedIcon={
                    <CheckBoxIcon
                        color={
                            isRowSelectionHeaderDisabled()
                                ? "disabled"
                                : "primary"
                        }
                    />
                }
                className={classNames(
                    dataGridV1Classes.dataGridSelectionCheckbox,
                    (className = "data-grid-v1-header-row-selection")
                )}
                disabled={isRowSelectionHeaderDisabled()}
                indeterminate={isRowSelectionIndeterminate()}
                indeterminateIcon={
                    <IndeterminateCheckBoxIcon
                        color={
                            isRowSelectionHeaderDisabled()
                                ? "disabled"
                                : "primary"
                        }
                    />
                }
                onClick={handleRowSelectionHeaderClick}
            />
        );
    }, [
        // filteredData dependency required, indirectly included with isRowSelectionHeaderDisabled
        isRowSelectionComplete,
        isRowSelectionHeaderDisabled,
        isRowSelectionIndeterminate,
        handleRowSelectionHeaderClick,
    ]);

    const cellRendererDefault = (
        rendererProps: Record<string, unknown>
    ): ReactNode => {
        // Get column
        const column = rendererProps.column as DataGridColumnV1<T>;

        // Get cell contents
        const cellContents = column.customCellRenderer
            ? column.customCellRenderer(
                  rendererProps.cellData as Record<string, unknown>,
                  rendererProps.rowData as T,
                  column
              )
            : toString(rendererProps.cellData);
        const cellTooltipContents = column.customCellTooltipRenderer
            ? column.customCellTooltipRenderer(
                  rendererProps.cellData as Record<string, unknown>,
                  rendererProps.rowData as T,
                  column
              )
            : cellContents;

        // Determine tooltip visibility
        const cellTooltipVisible =
            isNil(column.cellTooltip) || column.cellTooltip;

        if (cellTooltipVisible && cellTooltipContents) {
            // Render with a tooltip
            return (
                <TooltipV1
                    className="data-grid-v1-cell-tooltip"
                    title={cellTooltipContents}
                >
                    <div
                        className={classNames(
                            dataGridV1Classes.dataGridCell,
                            "data-grid-v1-cell",
                            column.cellClasses
                        )}
                    >
                        {cellContents}
                    </div>
                </TooltipV1>
            );
        }

        // Render without a tooltip
        return (
            <div
                className={classNames(
                    dataGridV1Classes.dataGridCell,
                    "data-grid-v1-cell",
                    column.cellClasses
                )}
            >
                {cellContents}
            </div>
        );
    };

    const cellRendererLoadingIndicator = (): ReactNode => {
        return (
            <Typography
                className={classNames(
                    dataGridV1Classes.dataGridCellSkeleton,
                    "data-grid-v1-cell-loading-indicator"
                )}
                variant="body2"
            >
                <Skeleton />
            </Typography>
        );
    };

    const cellRendererRowSelection = useCallback(
        (rendererProps) => {
            return (
                <Checkbox
                    checked={isRowSelected(rendererProps.cellData)}
                    className={classNames(
                        dataGridV1Classes.dataGridSelectionCheckbox,
                        "data-grid-v1-cell-row-selection"
                    )}
                    color="primary"
                    onClick={() =>
                        handleRowSelectionCellClick(rendererProps.rowData)
                    }
                />
            );
        },
        [isRowSelected, handleRowSelectionCellClick]
    );

    // Memoized invocations

    useMemo(() => {
        // Table to be flexible if any of the columns have flex property
        if (some(columns, "flex")) {
            setFixedTable(false);
        }
    }, [columns]);

    useMemo(() => {
        // Gather search data keys, if not provided
        if (!isEmpty(searchDataKeys)) {
            setSearchDataKeysInternal(searchDataKeys as string[]);

            return;
        }

        if (isEmpty(columns) || isSearchDisabled()) {
            // Search to be disabled
            setSearchDataKeysInternal([]);

            return;
        }

        const searchDataKeysInternal = [];
        for (const eachColumn of columns) {
            searchDataKeysInternal.push(eachColumn.dataKey);
        }
        setSearchDataKeysInternal(searchDataKeysInternal);
    }, [columns, searchDataKeys, isSearchDisabled]);

    useMemo(() => {
        // Filter data to be rendered
        if (!data || isEmpty(data)) {
            setFilteredData([]);

            // No selection
            setFilteredDataSelectionCount(0);

            return;
        }

        if (
            isEmpty(searchDataKeysInternal) ||
            !searchValue ||
            isSearchDisabled()
        ) {
            // Search to be disabled
            setFilteredData(getSortedData(data));

            // Filtered data selection same as selection model
            setFilteredDataSelectionCount(
                selectionModelInternal.rowKeyValues.size
            );

            return;
        }

        const filteredRowKeyValues = new Set();
        const filteredData = [];
        let filteredDataSelectionCount = 0;
        for (const eachSearchDataKey of searchDataKeysInternal) {
            for (const eachData of data) {
                const rowKeyValue = get(eachData, rowKey);
                if (filteredRowKeyValues.has(rowKeyValue)) {
                    // Row already filtered
                    continue;
                }

                // Get data at search key
                const searchKeyValue = get(eachData, eachSearchDataKey);
                if (
                    isNil(searchKeyValue) ||
                    (typeof searchKeyValue !== "string" &&
                        typeof searchKeyValue !== "number" &&
                        typeof searchKeyValue !== "boolean")
                ) {
                    // Skip searching
                    continue;
                }

                if (
                    toString(searchKeyValue)
                        .toLocaleLowerCase()
                        .includes(searchValue.toLocaleLowerCase().trim())
                ) {
                    // Match found
                    filteredRowKeyValues.add(rowKeyValue);
                    filteredData.push(eachData);

                    // Record filtered row selection
                    if (isRowSelected(rowKeyValue)) {
                        filteredDataSelectionCount++;
                    }
                }
            }
        }
        setFilteredData(getSortedData(filteredData));
        setFilteredDataSelectionCount(filteredDataSelectionCount);
    }, [
        // props.data dependency required, indirectly included with selectionModel
        rowKey,
        searchDataKeysInternal,
        searchValue,
        // selectionModel dependency required, indirectly included with isRowSelected
        isSearchDisabled,
        isRowSelected,
    ]);

    useMemo(() => {
        setFilteredData((draftFilteredData) =>
            getSortedData(draftFilteredData)
        );
    }, [columns, sortState]);

    useMemo(() => {
        if (!data || isEmpty(data) || disableSelection) {
            setRowKeyValueMap(new Map());

            return;
        }

        // Input data changed, keep row key value map ready to help construct selection model
        const rowKeyValueMap = new Map();
        for (const eachData of data) {
            const rowKeyValue = get(eachData, rowKey);
            rowKeyValueMap.set(rowKeyValue, eachData);
        }
        setRowKeyValueMap(rowKeyValueMap);
    }, [data, rowKey, disableSelection]);

    useMemo(() => {
        if (
            !selectionModel ||
            isEmpty(selectionModel.rowKeyValues) ||
            disableSelection
        ) {
            // Selection model input not available or selection to be disabled
            setSelectionModelInternal({
                rowKeyValues: new Set(),
                rowKeyValueMap: new Map(),
            });

            return;
        }

        // Construct selection model
        const rowKeyValues = new Set();
        const selectionRowKeyValueMap = new Map();
        for (const eachRowKeyValue of selectionModel.rowKeyValues) {
            const data = rowKeyValueMap.get(eachRowKeyValue);
            if (!data) {
                // Corresponding data not found
                continue;
            }

            rowKeyValues.add(eachRowKeyValue);
            selectionRowKeyValueMap.set(eachRowKeyValue, data);

            if (disableMultiSelection) {
                // Multi-selection disabled, pick up only the first selection from selection model
                // input
                break;
            }
        }
        setSelectionModelInternal({
            rowKeyValues: rowKeyValues,
            rowKeyValueMap: selectionRowKeyValueMap,
        });
    }, [
        selectionModel,
        disableMultiSelection,
        disableSelection,
        rowKeyValueMap,
    ]);

    const onSearchInputChange = (value: string): void => {
        onSearchFilterValueChange && onSearchFilterValueChange(value);
        setSearchValue(value);
    };

    return (
        <Box
            {...otherProps}
            border={hideBorder ? "none" : BorderV1.BorderDefault}
            boxSizing="content-box"
            className={classNames(
                dataGridV1Classes.dataGrid,
                className,
                "data-grid-v1"
            )}
            height={gridContainerHeight}
            minHeight="100%"
            width="100%"
        >
            {/* Toolbar */}
            {!hideToolbar ? (
                <Toolbar
                    className={classNames(
                        dataGridV1Classes.dataGridToolbar,
                        "data-grid-v1-toolbar"
                    )}
                    classes={{
                        gutters: dataGridV1Classes.dataGridToolbarGutters,
                    }}
                >
                    {/* Toolbar components */}
                    <div
                        className={classNames(
                            dataGridV1Classes.dataGridToolbarComponent,
                            "data-grid-v1-toolbar-component"
                        )}
                    >
                        {toolbarComponent}
                    </div>

                    {!disableSearch && (
                        <>
                            {/* Search and selection status text */}
                            {data && searchAndSelectionTextFn && (
                                <Typography
                                    className={classNames(
                                        dataGridV1Classes.dataGridToolbarSearchAndSelectionText,
                                        "data-grid-v1-search-and-selection-text"
                                    )}
                                    variant="body2"
                                >
                                    {searchAndSelectionTextFn(
                                        data.length,
                                        filteredData.length,
                                        selectionModelInternal.rowKeyValues.size
                                    )}
                                </Typography>
                            )}

                            {/* Search input */}
                            <div
                                className={
                                    dataGridV1Classes.dataGridToolbarSearch
                                }
                            >
                                <SearchInputV1
                                    fullWidth
                                    className="data-grid-v1-search-input"
                                    placeholder={searchPlaceholder}
                                    value={searchValue}
                                    onChange={onSearchInputChange}
                                    onChangeDelay={searchDelay}
                                />
                            </div>
                        </>
                    )}
                </Toolbar>
            ) : (
                // When hideToolbar is present <AutoResizer> get's height as 0
                // Which leads to Table un-rendered and ref is not set
                // If ref is not set and scroll is set to Body then,
                // Height of the table will be undefined and table won't rendered
                // Issue arise when hideToolbar is true and DataGridScrollV1 is Body
                <Box padding={0.1} />
            )}

            {/* Data grid */}
            <AutoResizer>
                {({ height, width }) => (
                    <Table<T | { id: number }>
                        className="data-grid-v1-table"
                        components={{
                            SortIndicator: DataGridSortIndicatorV1,
                            ExpandIcon: DataGridExpandIconV1,
                        }}
                        data={data ? filteredData : loadingIndicatorData}
                        estimatedRowHeight={HEIGHT_DATA_GRID_ROW}
                        expandColumnKey={expandColumnKey}
                        fixed={fixedTable}
                        headerHeight={HEIGHT_DATA_GRID_HEADER_ROW}
                        height={
                            hideToolbar
                                ? height
                                : height - HEIGHT_DATA_GRID_TOOLBAR
                        }
                        // React Base Table closure issue with custom renderers means custom
                        // renderers don't have the latest state values and thus
                        // ignoreFunctionInColumnCompare is set to false and all the custom
                        // renderers/handlers in columns are memoized via useCallback
                        ignoreFunctionInColumnCompare={false}
                        ref={handleTableRefDebounced}
                        rowClassName={getRowClassName}
                        rowKey={data ? rowKey : "id"}
                        rowRenderer={rowRendererDefault}
                        sortBy={sortState}
                        width={width}
                        onColumnSort={handleColumnSort}
                        onRowExpand={handleRowExpand}
                    >
                        {/* Row selection column */}
                        {data && !disableSelection && (
                            <Column<T | { id: number }>
                                align="center"
                                cellRenderer={cellRendererRowSelection}
                                className="data-grid-v1-column"
                                dataKey={rowKey}
                                headerRenderer={headerRendererRowSelection}
                                key={KEY_VALUE_ROW_SELECTION_COLUMN}
                                minWidth={WIDTH_DATA_GRID_ROW_SELECTION_COLUMN}
                                sortable={false}
                                width={WIDTH_DATA_GRID_ROW_SELECTION_COLUMN}
                            />
                        )}

                        {/* Loading indicator columns */}
                        {!data &&
                            columns &&
                            columns.map((eachColumn) => (
                                <Column
                                    align={
                                        eachColumn.align ||
                                        DataGridAlignmentV1.Left
                                    }
                                    cellRenderer={cellRendererLoadingIndicator}
                                    className="data-grid-v1-column"
                                    customHeaderRenderer={
                                        eachColumn.customHeaderRenderer
                                    }
                                    customHeaderTooltipRenderer={
                                        eachColumn.customHeaderTooltipRenderer
                                    }
                                    flexGrow={eachColumn.flex}
                                    header={eachColumn.header}
                                    headerRenderer={headerRendererDefault}
                                    headerTooltip={eachColumn.headerTooltip}
                                    key={eachColumn.key}
                                    minWidth={eachColumn.minWidth}
                                    sortable={false}
                                    title={eachColumn.header}
                                    width={eachColumn.minWidth}
                                />
                            ))}

                        {/* Columns */}
                        {data &&
                            columns &&
                            columns.map((eachColumn) => (
                                <Column
                                    align={
                                        eachColumn.align ||
                                        DataGridAlignmentV1.Left
                                    }
                                    cellClasses={eachColumn.cellClasses}
                                    cellRenderer={cellRendererDefault}
                                    cellTooltip={eachColumn.cellTooltip}
                                    className="data-grid-v1-column"
                                    customCellRenderer={
                                        eachColumn.customCellRenderer
                                    }
                                    customCellTooltipRenderer={
                                        eachColumn.customCellTooltipRenderer
                                    }
                                    customHeaderRenderer={
                                        eachColumn.customHeaderRenderer
                                    }
                                    customHeaderTooltipRenderer={
                                        eachColumn.customHeaderTooltipRenderer
                                    }
                                    dataKey={eachColumn.dataKey}
                                    flexGrow={eachColumn.flex}
                                    header={eachColumn.header}
                                    headerRenderer={headerRendererDefault}
                                    headerTooltip={eachColumn.headerTooltip}
                                    key={eachColumn.key}
                                    minWidth={eachColumn.minWidth}
                                    sortComparatorFn={
                                        eachColumn.sortComparatorFn
                                    }
                                    sortable={eachColumn.sortable}
                                    title={eachColumn.header}
                                    width={eachColumn.minWidth}
                                />
                            ))}
                    </Table>
                )}
            </AutoResizer>
        </Box>
    );
}
