import { makeStyles } from "@material-ui/core";

export const useTimeRangeSelectorStyles = makeStyles({
    timeRangeSelectorButton: {
        minWidth: "0px",
        padding: "6px",
    },
    timeRangeSelectorContainer: {
        height: "497px",
        width: "810px",
        overflow: "hidden",
    },
    timeRangeListContainer: {
        float: "left",
        height: "442px",
        width: "190px",
        overflow: "auto",
    },
    timeRangeListLabel: {
        paddingBottom: "0px",
        marginBottom: "-4px", // Minimize whitespace space between label and next list item
    },
    timeRangeListItem: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    selectedTimeRangeListItem: {
        fontWeight: "bold",
    },
    calendarsAndControlsContainer: {
        float: "right",
        height: "442px",
        width: "620px",
    },
    calendarsContainer: {
        marginBottom: "-15px", // Minimize whitespace between calendar and controls
    },
    calendarLabel: {
        paddingTop: "12px",
        paddingLeft: "25px",
        marginBottom: "-5px", // Minimize whitespace between calendar label and calendar
    },
    startTimeCalendar: {
        marginRight: "-8px", // Minimize whitespace between calendars
    },
    endTimeCalendar: {
        marginLeft: "-8px", // Minimize whitespace between calendars
    },
});
