import { makeStyles } from "@material-ui/core";

export const useTimeRangeSelectorStyles = makeStyles({
    timeRangeSelectorButton: {
        minWidth: "0px",
        padding: "6px",
    },
    timeRangeSelectorContainer: {
        height: "491px",
        width: "810px",
        overflow: "hidden",
    },
    timeRangeListContainer: {
        float: "left",
        height: "426px",
        width: "190px",
        overflow: "auto",
    },
    calendarsAndControlsContainer: {
        float: "left",
        height: "426px",
        width: "620px",
    },
});
