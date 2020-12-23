import { makeStyles } from "@material-ui/core";

export const useTimeRangeSelectorCalendarToolbarStyles = makeStyles({
    container: {
        marginBottom: "-12px", // Account for whitespace space toolbar and calendar
    },
    dense: {
        minHeight: "36px",
    },
    link: {
        marginRight: "8px",
        "&:last-of-type": {
            marginRight: "0px",
        },
    },
    rightAlign: {
        marginLeft: "auto",
    },
    selectedLink: {
        fontWeight: "bold",
    },
});
