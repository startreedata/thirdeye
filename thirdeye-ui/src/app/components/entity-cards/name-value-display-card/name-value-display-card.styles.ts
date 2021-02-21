import { makeStyles } from "@material-ui/core";
import { Palette } from "../../../utils/material-ui/palette.util";

export const useNameValueDisplayCardStyles = makeStyles((theme) => ({
    nameValueDisplayCard: {
        height: "100%",
        borderColor: Palette.COLOR_BORDER_LIGHT,
    },
    nameValueDisplayCardContent: {
        "&:last-child": {
            paddingBottom: theme.spacing(2),
        },
    },
    list: {
        maxHeight: "90px",
        overflowY: "auto",
    },
    listItem: {
        padding: "0px",
    },
    listItemText: {
        marginTop: "2px",
        marginBottom: "2px",
    },
}));
