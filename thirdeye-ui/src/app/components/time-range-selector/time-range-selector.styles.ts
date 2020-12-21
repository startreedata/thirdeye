import { makeStyles } from "@material-ui/core";

export const useTimeRangeSelectorStyles = makeStyles({
    timeRangeButton: {
        minWidth: "0px",
        padding: "6px",
    },
    timeRangeSelectorContainer: {
        overflow: "hidden",
        height: "495px",
    },
    timeRangeSelectorChildContainer: {
        padding: "16px",
    },
    timeRangeList: {
        height: "455px",
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
    calendarLabel: {
        paddingTop: "10px",
        paddingLeft: "25px",
    },
});
