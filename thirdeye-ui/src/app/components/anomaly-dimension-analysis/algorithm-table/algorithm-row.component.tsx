import {
    Collapse,
    LinearProgress,
    TableCell,
    TableRow,
    Tooltip,
    Typography,
} from "@material-ui/core";
import IconButton from "@material-ui/core/IconButton";
import { KeyboardArrowDown, KeyboardArrowUp } from "@material-ui/icons";
import { createStyles, makeStyles, withStyles } from "@material-ui/styles";
import React, { FunctionComponent, useState } from "react";
import { AnomalyDimensionAnalysisMetricRow } from "../../../rest/dto/rca.interfaces";
import { AlgorithmRowExpanded } from "./algorithm-row-expanded.component";
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

/**
 * Make the row on top of the one with expanded content blend by removing
 * the bottom border
 */
const useRowStyles = makeStyles({
    root: {
        "& > *": {
            borderBottom: "unset",
        },
    },
});

export const AlgorithmRow: FunctionComponent<AlgorithmRowProps> = ({
    dataset,
    metric,
    row,
    totalSum,
    dimensionColumns,
    anomaly,
}) => {
    const [open, setOpen] = useState(false);
    const classes = useRowStyles();
    const nameDisplay = generateName(
        row as unknown as AnomalyDimensionAnalysisMetricRow,
        metric,
        dataset,
        dimensionColumns
    );

    return (
        <>
            {/** Main Content */}
            <TableRow className={classes.root}>
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
                <TableCell>
                    <IconButton
                        aria-label="expand row"
                        size="small"
                        onClick={() => setOpen(!open)}
                    >
                        {open ? <KeyboardArrowUp /> : <KeyboardArrowDown />}
                    </IconButton>
                </TableCell>
            </TableRow>
            {/** Expanded Content */}
            <TableRow>
                <TableCell
                    colSpan={6}
                    style={{
                        paddingBottom: 0,
                        paddingTop: 0,
                        backgroundColor: "rgba(247, 249, 255)",
                    }}
                >
                    <Collapse unmountOnExit in={open} timeout="auto">
                        {open && (
                            <AlgorithmRowExpanded
                                anomaly={anomaly}
                                dimensionColumns={dimensionColumns}
                                row={row}
                            />
                        )}
                    </Collapse>
                </TableCell>
            </TableRow>
        </>
    );
};
