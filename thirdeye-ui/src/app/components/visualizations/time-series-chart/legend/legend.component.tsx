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
import { Grid } from "@material-ui/core";
import { LegendItem, LegendLabel, LegendOrdinal } from "@visx/legend";
import { ScaleOrdinal } from "d3-scale";
import React, { FunctionComponent } from "react";
import {
    LegendProps,
    Series,
    SeriesType,
} from "../time-series-chart.interfaces";
import { sortSeries } from "./legend.utils";

const LEGEND_CONTAINER_STYLE = {
    cursor: "pointer",
};
const RECT_HEIGHT_WIDTH = 20;
const SVG_ICON_VIRTUAL_BOUND = 50;

export const Legend: FunctionComponent<LegendProps> = ({
    series,
    onSeriesClick,
    colorScale,
}) => {
    const handleOnClick = (seriesData: Series): void => {
        const idx = series.findIndex((s) => s === seriesData);
        onSeriesClick && onSeriesClick(idx);
    };

    return (
        <LegendOrdinal<ScaleOrdinal<string, string, never>> scale={colorScale}>
            {() => (
                <Grid container justifyContent="center">
                    {sortSeries(series).map((seriesData) => {
                        let color = colorScale(seriesData.name as string);

                        /**
                         * colorScale is updated before the processed series
                         * in the parent so check for existence of series for index
                         *
                         * colorScale cannot be stored in state since its a complicated
                         * object with functions
                         */
                        if (seriesData.color !== undefined) {
                            color = seriesData.color;
                        }

                        /**
                         * colorScale is updated before the processed series
                         * in the parent so check for existence of series for index
                         */
                        if (!seriesData.enabled) {
                            color = "#EEE";
                        }

                        let legendIcon = (
                            <line
                                stroke={color}
                                strokeDasharray={seriesData.strokeDasharray}
                                strokeWidth={10}
                                x1="0"
                                x2={`${SVG_ICON_VIRTUAL_BOUND}`}
                                y1={`${SVG_ICON_VIRTUAL_BOUND / 2}`}
                                y2={`${SVG_ICON_VIRTUAL_BOUND / 2}`}
                            />
                        );

                        if (seriesData.type === SeriesType.AREA_CLOSED) {
                            legendIcon = (
                                <rect
                                    fill={color}
                                    height={SVG_ICON_VIRTUAL_BOUND}
                                    width={SVG_ICON_VIRTUAL_BOUND}
                                />
                            );
                        }

                        if (seriesData.legendIcon) {
                            legendIcon = seriesData.legendIcon(
                                SVG_ICON_VIRTUAL_BOUND,
                                color
                            );
                        }

                        return (
                            <Grid
                                item
                                key={`legend-item-${seriesData.name}`}
                                style={LEGEND_CONTAINER_STYLE}
                            >
                                <LegendItem
                                    onClick={() => handleOnClick(seriesData)}
                                >
                                    <svg
                                        height={RECT_HEIGHT_WIDTH}
                                        viewBox={`0 0 ${SVG_ICON_VIRTUAL_BOUND} ${SVG_ICON_VIRTUAL_BOUND}`}
                                        width={RECT_HEIGHT_WIDTH}
                                    >
                                        {legendIcon}
                                    </svg>
                                    <LegendLabel align="left" margin="0 5px">
                                        {seriesData.name}
                                    </LegendLabel>
                                </LegendItem>
                            </Grid>
                        );
                    })}
                </Grid>
            )}
        </LegendOrdinal>
    );
};
