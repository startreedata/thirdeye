import { makeStyles } from "@material-ui/core";

export const useTimeRangeSelectorStyles = makeStyles({
    timeRangeButton: {
        minWidth: "0px",
        padding: "6px",
    },
    timeRangeSelectorContainer: {
        overflow: "hidden",
        height: "475px",
    },
    timeRangeSelectorChildContainer: {
        padding: "16px",
    },
    timeRangeList: {
        height: "420px",
        width: "190px",
        overflow: "scroll",
    },
    timeRangeListLabel: {
        marginBottom: "-8px", // Minimize whitespace space between label item and following list item
    },
    timeRangeListItem: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    selectedTimeRangeListItem: {
        fontWeight: "bold",
    },
    calendarLabel: {
        paddingTop: "10px",
        paddingLeft: "25px",
        marginBottom: "-6px", // Account for whitespace space between label and calendar
    },
});
