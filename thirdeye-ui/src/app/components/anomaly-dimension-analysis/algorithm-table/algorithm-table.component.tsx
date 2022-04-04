import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { AlgorithmRow } from "./algorithm-row.component";
import { AlgorithmTableProps } from "./algorithm-table.interfaces";

export const AnomalyDimensionAnalysisTable: FunctionComponent<
    AlgorithmTableProps
> = ({ anomalyDimensionAnalysisData, anomaly, comparisonOffset }) => {
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
                    <TableCell>Name</TableCell>
                    <TableCell>Type</TableCell>
                    <TableCell>Impact Score</TableCell>
                    <TableCell />
                </TableRow>
            </TableHead>
            <TableBody>
                {data.map((row) => (
                    <AlgorithmRow
                        anomaly={anomaly}
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
                    />
                ))}
            </TableBody>
        </Table>
    );
};
