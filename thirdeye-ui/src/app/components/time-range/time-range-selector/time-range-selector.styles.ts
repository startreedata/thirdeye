import { makeStyles } from "@material-ui/core";

const HEIGHT_TIME_RANGE_SELECTOR_CONTENTS = "395px";

export const useTimeRangeSelectorStyles = makeStyles({
    button: {
        minWidth: "0px",
        padding: "6px",
    },
    popoverPaper: {
        overflowX: "auto",
        overflowY: "auto",
    },
    timeRangeSelector: {
        width: "801px",
    },
    timeRangeSelectorContents: {
        height: HEIGHT_TIME_RANGE_SELECTOR_CONTENTS,
    },
    timeRangeList: {
        height: HEIGHT_TIME_RANGE_SELECTOR_CONTENTS,
        width: "180px",
        overflow: "auto",
    },
    calendar: {
        marginTop: "10px",
    },
});
