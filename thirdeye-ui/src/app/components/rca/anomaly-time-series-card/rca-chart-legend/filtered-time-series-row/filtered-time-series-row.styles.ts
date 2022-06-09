import { makeStyles } from "@material-ui/core";

export const useFilteredTimeSeriesRowStyles = makeStyles(() => ({
    assignedChartColorCell: {
        borderLeftStyle: "solid",
        borderLeftWidth: "5px",
    },
    removeBtn: {
        fontSize: "0.85em",
    },
}));
