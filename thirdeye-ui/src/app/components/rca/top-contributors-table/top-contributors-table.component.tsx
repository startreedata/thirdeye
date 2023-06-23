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
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
} from "@material-ui/core";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import {
    determineTimezoneFromAlertInEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../../utils/alerts/alerts.util";
import { concatKeyValueWithEqual } from "../../../utils/params/params.util";
import { EmptyStateSwitch } from "../../page-states/empty-state-switch/empty-state-switch.component";
import { TopContributorsRow } from "./top-contributors-row.component";
import { TopContributorsTableProps } from "./top-contributors-table.interfaces";
import { generateFilterStrings } from "./top-contributors-table.utils";

export const TopContributorsTable: FunctionComponent<TopContributorsTableProps> =
    ({
        anomalyDimensionAnalysisData,
        anomaly,
        comparisonOffset,
        chartTimeSeriesFilterSet,
        onCheckClick,
        alertInsight,
    }) => {
        const { t } = useTranslation();
        const totalSum = anomalyDimensionAnalysisData.responseRows.reduce(
            (countSoFar: number, current) => {
                return countSoFar + current.cost;
            },
            0
        );

        const rowsData = anomalyDimensionAnalysisData.responseRows;
        rowsData.sort((a, b) => {
            return b.cost - a.cost;
        });

        const [hideTime, timezone, granularity] = useMemo(() => {
            return [
                shouldHideTimeInDatetimeFormat(
                    alertInsight?.templateWithProperties
                ),
                determineTimezoneFromAlertInEvaluation(
                    alertInsight?.templateWithProperties
                ),
                alertInsight?.templateWithProperties?.metadata?.granularity,
            ];
        }, [alertInsight]);

        return (
            <Table size="small">
                <TableHead>
                    <TableRow>
                        <TableCell width="20px" />
                        <TableCell width="20px">
                            <strong>{t("label.details")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.dimension-combination")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.impact-percentage")}</strong>
                        </TableCell>
                    </TableRow>
                </TableHead>
                <TableBody>
                    <EmptyStateSwitch
                        emptyState={
                            <TableRow>
                                <TableCell align="center" colSpan={10}>
                                    {t(
                                        "message.no-specific-top-contributor-is-detected"
                                    )}
                                </TableCell>
                            </TableRow>
                        }
                        isEmpty={
                            rowsData.length === 1 &&
                            rowsData[0].names.length === 0
                        }
                    >
                        {rowsData.map((row) => {
                            const id = generateFilterStrings(
                                row.names,
                                anomalyDimensionAnalysisData.dimensions,
                                row.otherDimensionValues
                            ).join();

                            const checked = chartTimeSeriesFilterSet.some(
                                (filterSet) =>
                                    filterSet
                                        .map((item) =>
                                            concatKeyValueWithEqual(item, false)
                                        )
                                        .sort()
                                        .join() === id
                            );

                            return (
                                <TopContributorsRow
                                    anomaly={anomaly}
                                    anomalyDimensionAnalysisData={
                                        anomalyDimensionAnalysisData
                                    }
                                    checked={checked}
                                    comparisonOffset={comparisonOffset}
                                    dataset={
                                        anomalyDimensionAnalysisData.metric
                                            .dataset.name
                                    }
                                    dimensionColumns={
                                        anomalyDimensionAnalysisData.dimensions
                                    }
                                    granularity={granularity}
                                    hideTime={hideTime}
                                    key={row.names.join()}
                                    metric={
                                        anomalyDimensionAnalysisData.metric.name
                                    }
                                    row={row}
                                    timezone={timezone}
                                    totalSum={totalSum}
                                    onCheckClick={onCheckClick}
                                />
                            );
                        })}
                    </EmptyStateSwitch>
                </TableBody>
            </Table>
        );
    };
