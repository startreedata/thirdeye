import { sortBy } from "lodash";
import { Series } from "../time-series-chart.interfaces";

export const sortSeries = (series: Series[]): Series[] => {
    const noIndices: Series[] = [];
    let withIndices: Series[] = [];

    series.forEach((seriesData: Series) => {
        if (seriesData.legendIndex === undefined) {
            noIndices.push(seriesData);
        } else {
            withIndices.push(seriesData);
        }
    });

    withIndices = sortBy(withIndices, (item) => item.legendIndex);

    return [...noIndices, ...withIndices];
};
