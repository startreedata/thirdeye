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
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { concatKeyValueWithEqual } from "../../../../utils/params/params.util";
import { EmptyStateSwitch } from "../../../page-states/empty-state-switch/empty-state-switch.component";
import { AlgorithmRow } from "./algorithm-row.component";
import { AlgorithmTableProps } from "./algorithm-table.interfaces";
import { generateFilterStrings } from "./algorithm-table.utils";

export const AnomalyDimensionAnalysisTable: FunctionComponent<AlgorithmTableProps> =
    ({
        anomalyDimensionAnalysisData,
        anomaly,
        comparisonOffset,
        chartTimeSeriesFilterSet,
        onCheckClick,
        timezone,
    }) => {
        const { t } = useTranslation();
        const totalSum = anomalyDimensionAnalysisData.responseRows.reduce(
            (countSoFar, current) => {
                return countSoFar + current.cost;
            },
            0
        );

        const data = anomalyDimensionAnalysisData.responseRows;
        data.sort((a, b) => {
            return b.cost - a.cost;
        });

        return (
            <Table>
                <TableHead>
                    <TableRow>
                        <TableCell>
                            <strong>{t("label.add-to-chart")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.name")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.type")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.impact-score")}</strong>
                        </TableCell>
                        <TableCell>
                            <strong>{t("label.show-details")}</strong>
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
                            data.length === 1 && data[0].names.length === 0
                        }
                    >
                        {data.map((row) => {
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
                                <AlgorithmRow
                                    alertId={anomaly.alert.id}
                                    checked={checked}
                                    comparisonOffset={comparisonOffset}
                                    dataset={
                                        anomalyDimensionAnalysisData.metric
                                            .dataset.name
                                    }
                                    dimensionColumns={
                                        anomalyDimensionAnalysisData.dimensions
                                    }
                                    endTime={anomaly.endTime}
                                    key={row.names.join()}
                                    metric={
                                        anomalyDimensionAnalysisData.metric.name
                                    }
                                    row={row}
                                    startTime={anomaly.startTime}
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
