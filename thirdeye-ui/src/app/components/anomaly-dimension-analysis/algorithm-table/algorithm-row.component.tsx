import {
    LinearProgress,
    TableCell,
    TableRow,
    Tooltip,
    Typography,
} from "@material-ui/core";
import { createStyles, withStyles } from "@material-ui/styles";
import React, { FunctionComponent } from "react";
import { AnomalyDimensionAnalysisMetricRow } from "../../../rest/dto/rca.interfaces";
import { AlgorithmRowProps } from "./algorithm-table.interfaces";
import {
    generateName,
    generateOtherDimensionTooltipString,
    SERVER_VALUE_FOR_OTHERS,
} from "./algorithm-table.utils";

const SCORE_INDICATOR_HEIGHT = 30;
/**
 * Restyle an indicator component to make it a score visualization
 */
const ScoreIndicator = withStyles(() =>
    createStyles({
        root: {
            height: SCORE_INDICATOR_HEIGHT,
            borderRadius: 0,
        },
        colorPrimary: {
            backgroundColor: "transparent",
        },
    })
)(LinearProgress);

export const AlgorithmRow: FunctionComponent<AlgorithmRowProps> = ({
    dataset,
    metric,
    row,
    totalSum,
    dimensionColumns,
}) => {
    const nameDisplay = generateName(
        row as unknown as AnomalyDimensionAnalysisMetricRow,
        metric,
        dataset,
        dimensionColumns
    );

    return (
        <>
            {/** Main Content */}
            <TableRow>
                <TableCell component="th" scope="row">
                    {row.names.includes(SERVER_VALUE_FOR_OTHERS) ? (
                        <Tooltip
                            title={
                                <Typography variant="body2">
                                    {generateOtherDimensionTooltipString(
                                        row.otherDimensionValues
                                    )}
                                </Typography>
                            }
                        >
                            {nameDisplay}
                        </Tooltip>
                    ) : (
                        nameDisplay
                    )}
                </TableCell>
                <TableCell>Metric</TableCell>
                <TableCell>
                    <ScoreIndicator
                        value={(row.cost / totalSum) * 100}
                        variant="determinate"
                    />
                </TableCell>
            </TableRow>
        </>
    );
};
