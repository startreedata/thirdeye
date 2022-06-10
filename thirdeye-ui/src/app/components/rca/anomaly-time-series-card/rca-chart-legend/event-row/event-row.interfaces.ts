import { ScaleOrdinal } from "d3-scale";
import { EventWithChartState } from "../../../../visualizations/time-series-chart/time-series-chart.interfaces";

export interface EventRowProps {
    event: EventWithChartState;
    colorScale: ScaleOrdinal<number, string, never>;
    onRemoveBtnClick: (event: EventWithChartState) => void;
    onCheckBoxClick: (event: EventWithChartState, newState: boolean) => void;
}
