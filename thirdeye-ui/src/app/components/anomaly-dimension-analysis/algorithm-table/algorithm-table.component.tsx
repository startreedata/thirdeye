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

export const AnomalyDimensionAnalysisTable: FunctionComponent<
    AlgorithmTableProps
> = ({
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
                            anomaly={anomaly}
                            checked={checked}
                            comparisonOffset={comparisonOffset}
                            dataset={
                                anomalyDimensionAnalysisData.metric.dataset.name
                            }
                            dimensionColumns={
                                anomalyDimensionAnalysisData.dimensions
                            }
                            key={row.names.join()}
                            metric={anomalyDimensionAnalysisData.metric.name}
                            row={row}
                            totalSum={totalSum}
                            onCheckClick={onCheckClick}
                        />
                    );
                })}
            </TableBody>
        </Table>
    );
};
