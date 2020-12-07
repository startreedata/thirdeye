import { makeStyles } from "@material-ui/core";
import { theme } from "../../utils/material-ui-util/theme-util";

export const cardStyles = makeStyles(() => {
    return {
        base: {
            boxShadow: "none",
            padding: theme.spacing(2),
            border: "1px solid #BDBDBD",
            margin: theme.spacing(2, 0),
            borderRadius: "8px",
        },
    };
});
