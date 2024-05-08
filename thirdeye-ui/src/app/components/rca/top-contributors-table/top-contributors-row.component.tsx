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
    Box,
    Checkbox,
    Collapse,
    Grid,
    LinearProgress,
    TableCell,
    TableRow,
    Tooltip,
    Typography,
} from "@material-ui/core";
import IconButton from "@material-ui/core/IconButton";
import { KeyboardArrowDown, KeyboardArrowUp } from "@material-ui/icons";
import ArrowDownwardIcon from "@material-ui/icons/ArrowDownward";
import ArrowUpwardIcon from "@material-ui/icons/ArrowUpward";
import { createStyles, withStyles } from "@material-ui/styles";
import classNames from "classnames";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { formatLargeNumberV1 } from "../../../platform/utils";
import { AnomalyDimensionAnalysisMetricRow } from "../../../rest/dto/rca.interfaces";
import { TopContributorsRowExpanded } from "./top-contributors-row-expanded.component";
import { useAlgorithmRowExpandedStyles } from "./top-contributors-row-expanded.styles";
import { TopContributorsRowProps } from "./top-contributors-table.interfaces";
import {
    SERVER_VALUE_FOR_OTHERS,
    generateFilterOptions,
    generateName,
    generateOtherDimensionTooltipString,
    isValidChangePercentage,
} from "./top-contributors-table.utils";

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

export const TopContributorsRow: FunctionComponent<TopContributorsRowProps> = ({
    dataset,
    metric,
    row,
    totalSum,
    dimensionColumns,
    comparisonOffset,
    checked,
    onCheckClick,
    timezone,
    hideTime,
    anomaly,
    granularity,
    anomalyDimensionAnalysisData,
}) => {
    const { t } = useTranslation();
    const [open, setOpen] = useState(false);
    const classes = useAlgorithmRowExpandedStyles();
    const nameDisplay = generateName(
        row as unknown as AnomalyDimensionAnalysisMetricRow,
        metric,
        dataset,
        dimensionColumns,
        t,
        true
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

    const isDown = Number(row.changePercentage) < 0;

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
                <TableCell>
                    <Box position="relative">
                        <ScoreIndicator
                            value={(row.cost / totalSum) * 100}
                            variant="determinate"
                        />
                        <Box position="absolute" top={0}>
                            <Grid container spacing={1}>
                                <Grid item>
                                    <Box mt="3px">
                                        {row.names.length > 0 && isDown && (
                                            <ArrowDownwardIcon />
                                        )}
                                        {row.names.length > 0 && !isDown && (
                                            <ArrowUpwardIcon />
                                        )}
                                    </Box>
                                </Grid>
                                <Grid item>
                                    <Box mt="4px">
                                        {formatLargeNumberV1(
                                            Number(
                                                row.contributionToOverallChangePercentage
                                            )
                                        )}
                                        %
                                    </Box>
                                </Grid>
                            </Grid>
                        </Box>
                    </Box>
                </TableCell>
                <TableCell>
                    <Box position="relative">
                        <ScoreIndicator
                            value={
                                isValidChangePercentage(row.changePercentage)
                                    ? row.changePercentage
                                    : 0
                            }
                            variant="determinate"
                        />
                        <Box position="absolute" top={0}>
                            <Grid container spacing={1}>
                                {isValidChangePercentage(
                                    row.changePercentage
                                ) ? (
                                    <Grid item>
                                        <Box mt="3px">
                                            {row.names.length > 0 && isDown && (
                                                <ArrowDownwardIcon />
                                            )}
                                            {row.names.length > 0 &&
                                                !isDown && <ArrowUpwardIcon />}
                                        </Box>
                                    </Grid>
                                ) : null}
                                <Grid item>
                                    <Box mt="4px">
                                        {formatLargeNumberV1(
                                            Number(row.changePercentage)
                                        )}
                                        {isValidChangePercentage(
                                            row.changePercentage
                                        )
                                            ? "%"
                                            : ""}
                                    </Box>
                                </Grid>
                            </Grid>
                        </Box>
                    </Box>
                </TableCell>
                <TableCell>
                    <IconButton
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
                            <TopContributorsRowExpanded
                                alertId={anomaly.alert.id}
                                anomaly={anomaly}
                                anomalyDimensionAnalysisData={
                                    anomalyDimensionAnalysisData
                                }
                                comparisonOffset={comparisonOffset}
                                dimensionColumns={dimensionColumns}
                                granularity={granularity}
                                hideTime={hideTime}
                                row={row}
                                timezone={timezone}
                            />
                        )}
                    </Collapse>
                </TableCell>
            </TableRow>
        </>
    );
};
