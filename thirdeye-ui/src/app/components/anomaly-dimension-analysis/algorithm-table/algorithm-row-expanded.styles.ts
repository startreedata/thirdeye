import { makeStyles } from "@material-ui/core";
import { Color } from "../../../utils/material-ui/color.util";

export const useAlgorithmRowExpandedStyles = makeStyles((theme) => ({
    /**
     * Make the row on top of the one with expanded content blend by removing
     * the bottom border
     */
    root: {
        "& > *": {
            borderBottom: "unset",
        },
    },
    expandedRowParent: {
        boxShadow: `${Color.BOX_SHADOW_BLACK} 0px 1px 2px 0px, ${Color.BOX_SHADOW_BLACK} 0px 1px 3px 1px;`,
        backgroundColor: Color.OFF_WHITE,
    },
    expandedRow: {
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
    },
    closedRow: {
        padding: 0,
    },
}));
