import { makeStyles } from "@material-ui/core";
import { Palette } from "../../../utils/material-ui-util/palette-util";

export const useTimeRangeSelectorCalendarToolbarStyles = makeStyles({
    dense: {
        minHeight: "36px",
    },
    selected: {
        color: Palette.COLOR_TEXT_DEFAULT,
    },
    month: {
        marginRight: "6px",
    },
    link: {
        marginLeft: "6px",
        marginRight: "6px",
    },
    rightAlign: {
        marginLeft: "auto",
    },
    meridiem: {
        marginLeft: "6px",
    },
});
