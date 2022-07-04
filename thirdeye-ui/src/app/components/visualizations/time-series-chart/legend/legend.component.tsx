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
import { Grid } from "@material-ui/core";
import { LegendItem, LegendLabel, LegendOrdinal } from "@visx/legend";
import { ScaleOrdinal } from "d3-scale";
import React, { FunctionComponent } from "react";
import { LegendProps } from "../time-series-chart.interfaces";

const LEGEND_CONTAINER_STYLE = {
    cursor: "pointer",
};
const RECT_HEIGHT_WIDTH = 15;

export const Legend: FunctionComponent<LegendProps> = ({
    series,
    onSeriesClick,
    colorScale,
}) => {
    const handleOnClick = (idx: number): void => {
        onSeriesClick && onSeriesClick(idx);
    };

    return (
        <LegendOrdinal<ScaleOrdinal<string, string, never>> scale={colorScale}>
            {(labels) => (
                <Grid container justifyContent="center">
                    {labels.map((label, idx) => {
                        let color = label.value;

                        /**
                         * colorScale is updated before the processed series
                         * in the parent so check for existence of series for index
                         *
                         * colorScale cannot be stored in state since its a complicated
                         * object with functions
                         */
                        if (series[idx] && series[idx].color !== undefined) {
                            color = series[idx].color;
                        }

                        /**
                         * colorScale is updated before the processed series
                         * in the parent so check for existence of series for index
                         */
                        if (series[idx] && !series[idx].enabled) {
                            color = "#EEE";
                        }

                        return (
                            <Grid
                                item
                                key={`legend-item-${idx}`}
                                style={LEGEND_CONTAINER_STYLE}
                            >
                                <LegendItem onClick={() => handleOnClick(idx)}>
                                    <svg
                                        height={RECT_HEIGHT_WIDTH}
                                        width={RECT_HEIGHT_WIDTH}
                                    >
                                        <rect
                                            fill={color}
                                            height={RECT_HEIGHT_WIDTH}
                                            width={RECT_HEIGHT_WIDTH}
                                        />
                                    </svg>
                                    <LegendLabel align="left" margin="0 5px">
                                        {label.text}
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
