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
import { Box, Card, Grid, Typography } from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import { sortBy } from "lodash";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridScrollV1,
    DataGridSelectionModelV1,
    DataGridSortOrderV1,
    DataGridV1,
    PageContentsCardV1,
    SkeletonV1,
} from "../../../platform/components";
import { formatLargeNumberV1 } from "../../../platform/utils";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { CohortResult } from "../../../rest/dto/rca.interfaces";
import { generateFilterStrings } from "../../anomaly-dimension-analysis/algorithm-table/algorithm-table.utils";
import { LoadingErrorStateSwitch } from "../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import {
    CohortsTableProps,
    CohortTableRowData,
} from "./cohorts-table.interfaces";

const NAME_JOIN_KEY = " AND ";
const PERCENTAGE = "percentage";

export const CohortsTable: FunctionComponent<CohortsTableProps> = ({
    getCohortsRequestStatus,
    cohortsData,
    onSelectionChange,
    children,
    title,
    subtitle,
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
        return sorted.slice(0, 100).map((result: CohortResult) => {
            const copied: CohortTableRowData = { ...result, name: "" };

            const values: string[] = [];
            const columnKeys: string[] = [];
            Object.keys(result.dimensionFilters).forEach((dimensionColumn) => {
                values.push(result.dimensionFilters[dimensionColumn]);
                columnKeys.push(dimensionColumn);
            });

            copied.name = generateFilterStrings(values, columnKeys, []).join(
                NAME_JOIN_KEY
            );

            return copied;
        });
    }, [cohortsData]);

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
            header: t("label.name"),
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
                    <Grid
                        container
                        alignItems="center"
                        justifyContent="space-between"
                    >
                        <Grid item>
                            <Typography variant="h5">{title}</Typography>
                            {subtitle && (
                                <Typography variant="body2">
                                    {subtitle}
                                </Typography>
                            )}
                        </Grid>
                        <Grid item>
                            {cohortsData &&
                                tableRows.length !==
                                    cohortsData.results.length && (
                                    <Typography variant="caption">
                                        {t("message.showing-top-100")}
                                    </Typography>
                                )}
                        </Grid>
                    </Grid>
                </Grid>
                <Grid item xs={12}>
                    <LoadingErrorStateSwitch
                        isError={getCohortsRequestStatus === ActionStatus.Error}
                        isLoading={
                            getCohortsRequestStatus === ActionStatus.Working
                        }
                        loadingState={
                            <>
                                <SkeletonV1 animation="pulse" />
                                <SkeletonV1 animation="pulse" />
                                <SkeletonV1 animation="pulse" />
                            </>
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
