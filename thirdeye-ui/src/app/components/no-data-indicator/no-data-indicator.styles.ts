import { makeStyles } from "@material-ui/core";
import { Border } from "../../utils/material-ui/border.util";

export const useNoDataIndicatorStyles = makeStyles((theme) => ({
    noDataIndicator: {
        height: "100%",
        width: "100%",
        display: "flex",
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    icon: {
        display: "flex",
    },
    iconWithText: {
        display: "flex",
        borderRight: Border.BORDER_DEFAULT,
        paddingRight: theme.spacing(2),
    },
    text: {
        paddingLeft: theme.spacing(2),
    },
}));
