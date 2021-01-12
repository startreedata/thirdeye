import { makeStyles } from "@material-ui/core";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useBreadcrumbsStyles = makeStyles({
    selectedLink: {
        color: Palette.COLOR_TEXT_DEFAULT,
        cursor: "default",
    },
});
