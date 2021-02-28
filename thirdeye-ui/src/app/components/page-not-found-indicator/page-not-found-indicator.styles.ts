import { makeStyles } from "@material-ui/core";
import { Border } from "../../utils/material-ui/border.util";

export const usePageNotFoundIndicatorStyles = makeStyles((theme) => ({
    pageNotFoundIndicator: {
        height: "100%",
        width: "100%",
        display: "flex",
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    errorCode: {
        borderRight: Border.BORDER_DEFAULT,
        paddingRight: theme.spacing(2),
    },
    errorMessage: {
        paddingLeft: theme.spacing(2),
    },
}));
