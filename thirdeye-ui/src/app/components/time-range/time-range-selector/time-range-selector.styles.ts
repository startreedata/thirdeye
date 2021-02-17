import { makeStyles } from "@material-ui/core";

export const useTimeRangeSelectorStyles = makeStyles((theme) => ({
    button: {
        minWidth: "0px",
        padding: "6px",
    },
    timeRangeSelectorContents: {
        [theme.breakpoints.only("xs")]: {
            width: "342px",
        },
        [theme.breakpoints.only("sm")]: {
            height: "784px",
            width: "465px",
        },
        [theme.breakpoints.up("sm")]: {
            padding: "0px",
        },
        [theme.breakpoints.up("md")]: {
            height: "425px",
            width: "828px",
        },
        "&:last-child": {
            [theme.breakpoints.up("sm")]: {
                paddingBottom: "0px",
            },
        },
    },
    timeRangeList: {
        [theme.breakpoints.only("sm")]: {
            height: "100%",
        },
        [theme.breakpoints.up("md")]: {
            height: "425px",
            overflow: "auto",
        },
    },
    startTimeCalendarContainer: {
        [theme.breakpoints.up("md")]: {
            paddingRight: "0px !important",
        },
    },
    endTimeCalendarContainer: {
        [theme.breakpoints.up("md")]: {
            paddingLeft: "0px !important",
        },
    },
    startTimeCalendarLabel: {
        [theme.breakpoints.up("sm")]: {
            marginTop: "12px",
            marginLeft: "12px",
        },
    },
    endTimeCalendarLabel: {
        [theme.breakpoints.up("sm")]: {
            marginLeft: "12px",
        },
        [theme.breakpoints.up("md")]: {
            marginTop: "12px",
        },
    },
    calendar: {
        width: "310px",
    },
}));
