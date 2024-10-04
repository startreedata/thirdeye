/*
 * Copyright 2024 StarTree Inc
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
import React from "react";

// Components
import { LegendOrdinal, LegendItem, LegendLabel } from "@visx/legend";
import { scaleOrdinal } from "@visx/scale";

// Interfaces
import { LegendProps } from "./bar-graph.interfaces";

export const BarLegend = ({ labels }: LegendProps): JSX.Element => {
    const ordinalColorScale = scaleOrdinal({
        domain: labels.map((label) => label.text),
        range: labels.map((label) => label.value),
    });
    const legendGlyphSize = 20;
    const legendGlyphSize1 = 5;

    return (
        <LegendOrdinal
            labelFormat={(label) => `${label}`}
            scale={ordinalColorScale}
        >
            {(labels) => (
                <div style={{ display: "flex", flexDirection: "row" }}>
                    {labels.map((label, i) => (
                        <LegendItem key={`legend-quantile-${i}`} margin="0 5px">
                            <svg
                                height={legendGlyphSize1}
                                width={legendGlyphSize}
                            >
                                <rect
                                    fill={label.value}
                                    height={legendGlyphSize}
                                    width={legendGlyphSize}
                                />
                            </svg>
                            <LegendLabel align="left" margin="0 0 0 4px">
                                {label.text}
                            </LegendLabel>
                        </LegendItem>
                    ))}
                </div>
            )}
        </LegendOrdinal>
    );
};
