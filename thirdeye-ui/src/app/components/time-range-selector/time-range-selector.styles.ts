import { makeStyles } from "@material-ui/core";

export const useTimeRangeSelectorStyles = makeStyles({
    timeRangeSelectorButton: {
        minWidth: "0px",
        padding: "6px",
    },
    timeRangeSelectorContainer: {
        height: "494px",
        width: "810px",
        overflow: "hidden",
    },
    timeRangeListContainer: {
        float: "left",
        height: "439px",
        width: "190px",
        overflow: "auto",
    },
    calendarsAndControlsContainer: {
        float: "left",
        height: "442px",
        width: "620px",
    },
    calendarLabel: {
        paddingTop: "12px",
        paddingLeft: "25px",
        marginBottom: "-5px", // Minimize whitespace between calendar label and calendar
    },
});
