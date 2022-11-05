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
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { concatKeyValueWithEqual } from "../../../utils/params/params.util";
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
    }) => {
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
                        <TableCell />
                        <TableCell>
                            <strong>Name</strong>
                        </TableCell>
                        <TableCell>
                            <strong>Type</strong>
                        </TableCell>
                        <TableCell>
                            <strong>Impact Score</strong>
                        </TableCell>
                        <TableCell />
                    </TableRow>
                </TableHead>
                <TableBody>
                    {data.map((row) => {
                        const id = generateFilterStrings(
                            row.names,
                            anomalyDimensionAnalysisData.dimensions,
                            row.otherDimensionValues
                        ).join();

                        const checked = chartTimeSeriesFilterSet.some(
                            (filterSet) =>
                                filterSet
                                    .map(concatKeyValueWithEqual)
                                    .sort()
                                    .join() === id
                        );

                        return (
                            <AlgorithmRow
                                alertId={anomaly.alert.id}
                                checked={checked}
                                comparisonOffset={comparisonOffset}
                                dataset={
                                    anomalyDimensionAnalysisData.metric.dataset
                                        .name
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
                                totalSum={totalSum}
                                onCheckClick={onCheckClick}
                            />
                        );
                    })}
                </TableBody>
            </Table>
        );
    };
