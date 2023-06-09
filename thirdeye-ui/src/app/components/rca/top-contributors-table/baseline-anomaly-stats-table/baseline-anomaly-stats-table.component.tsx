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
import { Box, Table, TableRow, Typography } from "@material-ui/core";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { formatLargeNumberV1 } from "../../../../platform/utils";
import { useCommonStyles } from "../../../../utils/material-ui/common.styles";
import { BaselineAnomalyStatsTableProps } from "./baseline-anomaly-stats-table.interface";

export const BaselineAnomalyStatsTable: FunctionComponent<BaselineAnomalyStatsTableProps> =
    ({ row, anomalyDimensionAnalysisData }) => {
        const { t } = useTranslation();
        const commonClasses = useCommonStyles();

        const deviation = row.currentValue - row.baselineValue;
        const hasNegativeDeviation = deviation < 0;

        return (
            <>
                <Box>
                    <Typography variant="h6">
                        {t("label.metrics-for-dimension-combination-filter")}
                    </Typography>
                    <Table size="small">
                        <TableBody>
                            <TableRow>
                                <TableCell>{t("label.baseline")}</TableCell>
                                <TableCell>{row.baselineValue}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell>{t("label.current")}</TableCell>
                                <TableCell>{row.currentValue}</TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell>
                                    {t("label.percent-change")}
                                </TableCell>
                                <TableCell>
                                    <Box
                                        className={
                                            hasNegativeDeviation
                                                ? commonClasses.decreased
                                                : commonClasses.increased
                                        }
                                        component="div"
                                    >
                                        {row.changePercentage === "NaN" && (
                                            <span>-</span>
                                        )}
                                        {row.changePercentage !== "NaN" && (
                                            <>
                                                <Box
                                                    display="inline"
                                                    marginRight="5px"
                                                >
                                                    <span>
                                                        (
                                                        {formatLargeNumberV1(
                                                            row.changePercentage as number
                                                        )}
                                                        %)
                                                    </span>
                                                </Box>
                                                <Box display="inline">
                                                    <span>{deviation}</span>
                                                </Box>
                                            </>
                                        )}
                                    </Box>
                                </TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </Box>
                <Box mt={5}>
                    <Typography variant="h6">
                        {t(
                            "message.metrics-without-dimension-combination-filter-non-filtered"
                        )}
                    </Typography>
                    <Table size="small">
                        <TableBody>
                            <TableRow>
                                <TableCell>{t("label.baseline")}</TableCell>
                                <TableCell>
                                    {anomalyDimensionAnalysisData.baselineTotal}
                                </TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell>{t("label.current")}</TableCell>
                                <TableCell>
                                    {anomalyDimensionAnalysisData.currentTotal}
                                </TableCell>
                            </TableRow>
                            <TableRow>
                                <TableCell>
                                    {t("label.percent-change")}
                                </TableCell>
                                <TableCell>
                                    (
                                    {formatLargeNumberV1(
                                        ((anomalyDimensionAnalysisData.currentTotal -
                                            anomalyDimensionAnalysisData.baselineTotal) /
                                            anomalyDimensionAnalysisData.baselineTotal) *
                                            100
                                    )}
                                    % ){" "}
                                    {anomalyDimensionAnalysisData.currentTotal -
                                        anomalyDimensionAnalysisData.baselineTotal}
                                </TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </Box>
                <Box mt={5}>
                    <Typography variant="h6">
                        {t(
                            "message.dimension-combination-relative-to-non-filtered"
                        )}
                    </Typography>
                    <Table size="small">
                        <TableBody>
                            <TableRow>
                                <TableCell>
                                    {t("label.percent-contribution-change")}
                                </TableCell>
                                <TableCell>
                                    {formatLargeNumberV1(
                                        row.contributionChangePercentage
                                    )}
                                    %
                                </TableCell>
                            </TableRow>

                            <TableRow>
                                <TableCell>
                                    {t(
                                        "label.percent-contribution-to-overall-change"
                                    )}
                                </TableCell>
                                <TableCell>
                                    {formatLargeNumberV1(
                                        row.contributionToOverallChangePercentage
                                    )}
                                    %
                                </TableCell>
                            </TableRow>
                        </TableBody>
                    </Table>
                </Box>
            </>
        );
    };
