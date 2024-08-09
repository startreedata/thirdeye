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
import { Box, Card, Grid, Typography } from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import { sortBy } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridSortOrderV1,
    DataGridV1,
    PageContentsCardV1,
} from "../../../platform/components";
import { formatLargeNumberV1 } from "../../../platform/utils";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    CohortsTableProps,
    CohortTableRowData,
} from "./cohorts-table.interfaces";
import { getCohortTableRowFromData, PERCENTAGE } from "./cohorts-table.utils";

export const CohortsTable: FunctionComponent<CohortsTableProps> = ({
    getCohortsRequestStatus,
    initiallySelectedCohorts,
    cohortsData,
    onSelectionChange,
    children,
}) => {
    const { t } = useTranslation();

    const tableRows: CohortTableRowData[] = useMemo(() => {
        if (!cohortsData) {
            return [];
        }
        const sorted = sortBy(cohortsData.results, PERCENTAGE);
        sorted.reverse();

        // Showing more than 100 breaks the table and this feature is not used
        // enough to illicit enabling pagination
        return sorted.slice(0, 100).map(getCohortTableRowFromData);
    }, [cohortsData]);

    // Manage the table selection
    const [selectedCohorts, setSelectedCohorts] = useState<
        DataGridSelectionModelV1<CohortTableRowData>
    >(() => ({
        rowKeyValues: [],
        rowKeyValueMap: new Map(),
    }));

    useEffect(() => {
        if (
            selectedCohorts.rowKeyValues.length === 0 &&
            initiallySelectedCohorts.length > 0
        ) {
            // If there are initially selected cohorts but none selected already,
            // update the row selection
            setSelectedCohorts({
                rowKeyValues: initiallySelectedCohorts.map(
                    (cohort) => cohort.name
                ),
                rowKeyValueMap: new Map(
                    initiallySelectedCohorts.map((cohort) => [
                        cohort.name,
                        cohort,
                    ])
                ),
            });
        }
    }, [initiallySelectedCohorts]);

    const renderPercentageCell = (
        cellValue: Record<string, unknown>
    ): string => {
        return `${cellValue}%`;
    };

    const renderValueCell = (cellValue: Record<string, unknown>): string => {
        return formatLargeNumberV1(Number(cellValue));
    };

    const columns = [
        {
            key: "name",
            dataKey: "name",
            header: t("label.dimension"),
            minWidth: 100,
            flex: 1,
        },
        {
            key: "value",
            dataKey: "value",
            header: t("label.individual-contribution"),
            minWidth: 100,
            flex: 1,
            customCellRenderer: renderValueCell,
        },
        {
            key: "percentage",
            dataKey: "percentage",
            header: t("label.total-impact"),
            minWidth: 100,
            flex: 1,
            customCellRenderer: renderPercentageCell,
        },
    ];

    const handleSelectionChange = (
        selectedCohorts: DataGridSelectionModelV1<CohortTableRowData>
    ): void => {
        if (!selectedCohorts || !selectedCohorts.rowKeyValueMap) {
            return;
        }
        onSelectionChange &&
            onSelectionChange(
                Array.from(selectedCohorts.rowKeyValueMap.values())
            );
    };

    return (
        <PageContentsCardV1>
            <Grid container>
                <Grid item xs={12}>
                    <LoadingErrorStateSwitch
                        isError={getCohortsRequestStatus === ActionStatus.Error}
                        isLoading={
                            getCohortsRequestStatus === ActionStatus.Working
                        }
                    >
                        {!cohortsData && (
                            <Card variant="outlined">
                                <Box padding={3} textAlign="center">
                                    <SearchIcon fontSize="large" />
                                    <Typography variant="body1">
                                        {t(
                                            "message.fill-up-form-to-search-for-top-contributors"
                                        )}
                                    </Typography>
                                </Box>
                            </Card>
                        )}

                        {cohortsData && tableRows.length > 0 && (
                            <>
                                <DataGridV1<CohortTableRowData>
                                    disableSearch
                                    hideBorder
                                    hideToolbar
                                    columns={columns}
                                    data={tableRows}
                                    disableSelection={!onSelectionChange}
                                    initialSortState={{
                                        key: PERCENTAGE,
                                        order: DataGridSortOrderV1.DESC,
                                    }}
                                    rowKey="name"
                                    scroll={DataGridScrollV1.Body}
                                    selectionModel={selectedCohorts}
                                    onSelectionChange={handleSelectionChange}
                                />
                            </>
                        )}

                        {cohortsData && tableRows.length === 0 && (
                            <Box padding={3} textAlign="center">
                                <SearchIcon fontSize="large" />
                                <Typography variant="body1">
                                    {t("message.no-results-were-generated")}
                                </Typography>
                            </Box>
                        )}
                    </LoadingErrorStateSwitch>
                </Grid>
            </Grid>
            {cohortsData && tableRows.length > 0 && children}
        </PageContentsCardV1>
    );
};
