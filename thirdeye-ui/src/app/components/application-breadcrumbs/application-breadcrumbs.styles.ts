import { createStyles, makeStyles } from "@material-ui/core";
import { Color } from "../../utils/material-ui/color.util";

export const applicationBreadcrumbsStyles = makeStyles(
    createStyles({
        selected: {
            color: Color.BLACK,
        },
    })
);
