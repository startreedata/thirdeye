import { LegendItem, LegendLabel, LegendOrdinal } from "@visx/legend";
import React, { FunctionComponent } from "react";
import { LegendProps } from "./legend.interfaces";

const LEGEND_CONTAINER_STYLE = {
    justifyContent: "space-evenly",
    display: "flex",
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
        <LegendOrdinal scale={colorScale}>
            {(labels) => (
                <div style={LEGEND_CONTAINER_STYLE}>
                    {labels.map((label, idx) => {
                        let color = label.value;

                        if (series[idx].color !== undefined) {
                            color = series[idx].color;
                        }

                        if (!series[idx].enabled) {
                            color = "#EEE";
                        }

                        return (
                            <LegendItem
                                key={`legend-item-${idx}`}
                                onClick={() => handleOnClick(idx)}
                            >
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
                                <LegendLabel align="left" margin="0 0 0 5px">
                                    {label.text}
                                </LegendLabel>
                            </LegendItem>
                        );
                    })}
                </div>
            )}
        </LegendOrdinal>
    );
};
