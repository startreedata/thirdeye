import { makeStyles } from "@material-ui/core";
import { Palette } from "../../../utils/material-ui/palette.util";

export const useNameValueDisplayCardStyles = makeStyles((theme) => ({
    nameValueDisplayCard: {
        height: "100%",
        borderColor: Palette.COLOR_BORDER_DEFAULT,
    },
    nameValueDisplayCardContent: {
        "&:last-child": {
            paddingBottom: theme.spacing(2),
        },
    },
    list: {
        maxHeight: 90,
        overflowY: "auto",
    },
    listItem: {
        padding: 0,
    },
    listItemText: {
        marginTop: 2,
        marginBottom: 2,
    },
}));
