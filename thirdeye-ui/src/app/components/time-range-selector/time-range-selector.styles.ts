import { makeStyles } from "@material-ui/core";

export const useTimeRangeSelectorStyles = makeStyles({
    timeRangeButton: {
        minWidth: "0px",
        padding: "6px",
    },
    timeRangeSelectorContainer: {
        overflow: "hidden",
        height: "530px",
    },
    timeRangeSelectorChildContainer: {
        padding: "16px",
    },
    timeRangeList: {
        height: "490px",
        width: "190px",
        overflow: "scroll",
    },
    timeRangeListItem: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    selectedTimeRange: {
        fontWeight: "bold",
    },
});
