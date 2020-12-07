import { makeStyles } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui-util/dimension-util";

export const useTimeRangeSelectorStyles = makeStyles({
    timeRangeButton: {
        minWidth: "0px",
        padding: "6px",
    },
    selectedTimeRange: {
        fontWeight: "bold",
    },
    timeRangeSelectorContainer: {
        padding: Dimension.PADDING_POPOVER_DEFAULT,
    },
});
