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
import { Box, Card, Grid, Typography } from "@material-ui/core";
import SearchIcon from "@material-ui/icons/Search";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import {
    DataGridScrollV1,
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

export const CohortsTable: FunctionComponent<CohortsTableProps> = ({
    getCohortsRequestStatus,
    cohortsData,
}) => {
    const { t } = useTranslation();
    const tableRows: CohortTableRowData[] = useMemo(() => {
        if (!cohortsData) {
            return [];
        }

        return cohortsData.results.map((result: CohortResult) => {
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
            sortable: true,
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
            sortable: true,
            header: t("label.total-impact"),
            minWidth: 100,
            flex: 1,
            customCellRenderer: renderPercentageCell,
        },
    ];

    return (
        <PageContentsCardV1>
            <Grid container>
                <Grid item xs={12}>
                    <Box marginBottom={2}>
                        <Typography variant="h5">
                            {t("label.cohorts")}
                        </Typography>
                    </Box>
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
                            <DataGridV1<CohortTableRowData>
                                disableSearch
                                hideBorder
                                hideToolbar
                                columns={columns}
                                data={tableRows}
                                rowKey="name"
                                scroll={DataGridScrollV1.Body}
                            />
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
        </PageContentsCardV1>
    );
};
