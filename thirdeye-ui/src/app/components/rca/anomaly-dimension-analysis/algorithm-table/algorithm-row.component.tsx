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
    Checkbox,
    Collapse,
    LinearProgress,
    TableCell,
    TableRow,
    Tooltip,
    Typography,
} from "@material-ui/core";
import IconButton from "@material-ui/core/IconButton";
import { KeyboardArrowDown, KeyboardArrowUp } from "@material-ui/icons";
import { createStyles, withStyles } from "@material-ui/styles";
import classNames from "classnames";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { AnomalyDimensionAnalysisMetricRow } from "../../../../rest/dto/rca.interfaces";
import { AlgorithmRowExpanded } from "./algorithm-row-expanded.component";
import { useAlgorithmRowExpandedStyles } from "./algorithm-row-expanded.styles";
import { AlgorithmRowProps } from "./algorithm-table.interfaces";
import {
    generateFilterOptions,
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
    alertId,
    startTime,
    endTime,
    comparisonOffset,
    checked,
    onCheckClick,
    timezone,
}) => {
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);
    const classes = useAlgorithmRowExpandedStyles();
    const nameDisplay = generateName(
        row as unknown as AnomalyDimensionAnalysisMetricRow,
        metric,
        dataset,
        dimensionColumns,
        t
    );
    const parentRowClasses = [classes.root];

    if (open) {
        parentRowClasses.push(classes.expandedRowParent);
    }

    const handleOnCheckboxClick = (): void => {
        onCheckClick &&
            onCheckClick(
                generateFilterOptions(
                    row.names,
                    dimensionColumns,
                    row.otherDimensionValues
                )
            );
    };

    return (
        <>
            {/** Main Content */}
            <TableRow className={classNames(...parentRowClasses)}>
                <TableCell>
                    {row.names.length > 0 && (
                        <Checkbox
                            checked={checked}
                            onChange={handleOnCheckboxClick}
                        />
                    )}
                </TableCell>
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
                <TableCell>{t("label.metric")}</TableCell>
                <TableCell>
                    {row.names.length > 0 && (
                        <ScoreIndicator
                            value={(row.cost / totalSum) * 100}
                            variant="determinate"
                        />
                    )}
                </TableCell>
                <TableCell align="center">
                    <IconButton
                        aria-label="expand row"
                        color="inherit"
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
                    className={open ? classes.expandedRow : classes.closedRow}
                    colSpan={6}
                >
                    <Collapse unmountOnExit in={open} timeout="auto">
                        {open && (
                            <AlgorithmRowExpanded
                                alertId={alertId}
                                comparisonOffset={comparisonOffset}
                                dimensionColumns={dimensionColumns}
                                endTime={endTime}
                                row={row}
                                startTime={startTime}
                                timezone={timezone}
                            />
                        )}
                    </Collapse>
                </TableCell>
            </TableRow>
        </>
    );
};
