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
import { Button, Checkbox } from "@material-ui/core";
import TableCell from "@material-ui/core/TableCell";
import TableRow from "@material-ui/core/TableRow";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { LocalThemeProviderV1 } from "../../../../../platform/components";
import { lightV1 } from "../../../../../platform/utils";
import { concatKeyValueWithEqual } from "../../../../../utils/params/params.util";
import { FilteredTimeSeriesProps } from "./filtered-time-series-row.interfaces";
import { useFilteredTimeSeriesRowStyles } from "./filtered-time-series-row.styles";

export const FilteredTimeSeriesRow: FunctionComponent<
    FilteredTimeSeriesProps
> = ({ series, colorScale, filterSet, onRemoveBtnClick, onCheckBoxClick }) => {
    const classes = useFilteredTimeSeriesRowStyles();
    const { t } = useTranslation();

    const name = filterSet.map(concatKeyValueWithEqual).join(" & ");
    const seriesData = series.find((candidate) => candidate.name === name);

    let color = colorScale(name);

    if (seriesData && seriesData.color !== undefined) {
        color = seriesData.color;
    }

    if (!seriesData || !seriesData.enabled) {
        color = "#EEE";
    }

    return (
        <TableRow>
            <TableCell
                className={classes.assignedChartColorCell}
                style={{
                    borderLeftColor: color,
                }}
            >
                {seriesData && (
                    <Checkbox
                        checked={seriesData.enabled}
                        size="small"
                        onChange={onCheckBoxClick}
                    />
                )}
            </TableCell>
            <TableCell>{t("label.metric")}</TableCell>
            <TableCell>{name}</TableCell>
            <TableCell align="right">
                <LocalThemeProviderV1 primary={lightV1.palette.error}>
                    <Button
                        className={classes.removeBtn}
                        color="primary"
                        size="small"
                        onClick={onRemoveBtnClick}
                    >
                        {t("label.remove")}
                    </Button>
                </LocalThemeProviderV1>
            </TableCell>
        </TableRow>
    );
};
